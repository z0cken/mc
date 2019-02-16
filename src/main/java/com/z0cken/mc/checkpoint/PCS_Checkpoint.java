package com.z0cken.mc.checkpoint;

import com.google.common.io.ByteStreams;
import com.z0cken.mc.core.util.MessageBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

/** @author Flare */
public final class PCS_Checkpoint extends Plugin implements Listener {

    private static PCS_Checkpoint instance;
    private static MessageBuilder messageBuilder;

    static PCS_Checkpoint getInstance() {
        return instance;
    }

    private static final WeakHashMap<ProxiedPlayer, Persona> verified = new WeakHashMap<>();
    private static Configuration config;

    /*
     * TODO Async REST & SQL ?
     * TODO Admin Commands
     * TODO Give invites
     * TODO Detect server outages
     */

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        loadConfig();
        messageBuilder = new MessageBuilder().define("A", getConfig().getString("messages.accent-color"));

        getProxy().getPluginManager().registerCommand(this, new CommandVerify());
        getProxy().getPluginManager().registerCommand(this, new CommandInvite());
        getProxy().getPluginManager().registerCommand(this, new CommandAnon());
        getProxy().getPluginManager().registerListener(this,this);

        DatabaseHelper.setupTables();
        getProxy().getScheduler().schedule(this, RequestHelper::fetchMessages, 5 , config.getInt("bot.interval"), TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
        //Discards potential other unsaved invocations of Configuration#set
        //Prevents runtime changes by user to be overwritten
        loadConfig();

        config.set("bot.timestamp", RequestHelper.timestamp);

        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        instance = null;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        checkPlayer(event.getPlayer().getUniqueId(), false);
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        if(!verified.containsKey(event.getPlayer()) && !event.getTarget().getName().equals(config.getString("hub-name"))) {
            event.setTarget(getProxy().getServerInfo(config.getString("hub-name")));
            event.getPlayer().sendMessage(messageBuilder.build(config.getString("messages.verify.switch")));
        }
    }

    /*@EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        verified.remove(event.getPlayer());
    }*/

    void checkPlayer(UUID uuid, boolean verbose) {
        ProxiedPlayer player = getProxy().getPlayer(uuid);
        if(player == null) return;

        Persona persona = DatabaseHelper.getPersona(player.getUniqueId());

        if(persona == null) {
            if(DatabaseHelper.isGuest(uuid)) {
                verified.put(player, null);
                if(verbose) player.sendMessage(messageBuilder.build(PCS_Checkpoint.getConfig().getString("messages.invite.confirmed-guest")));
            }
        } else {
            long bannedUntil = persona.getBannedUntil();
            if(bannedUntil > 0 || bannedUntil == -1) {

                String msg;
                String time = null;
                if(bannedUntil == -1) msg = PCS_Checkpoint.getConfig().getString("messages.banned-permanent");
                else {
                    time = Instant.ofEpochMilli(bannedUntil*1000).atZone(ZoneId.of("UTC+01:00")).format(DateTimeFormatter.ofPattern("dd.MM.yy 'um' HH:mm"));
                    msg = PCS_Checkpoint.getConfig().getString("messages.banned");
                }

                player.disconnect(messageBuilder.define("TIME", time).build(msg));
            } else {
                verified.put(player, persona);
                if(verbose) {
                    player.sendMessage(messageBuilder.build(PCS_Checkpoint.getConfig().getString("messages.verify.success")));
                    player.sendMessage(messageBuilder.build(PCS_Checkpoint.getConfig().getString("messages.verify.info-anon")));
                }
            }
        }
    }

    static Persona getPersona(ProxiedPlayer player) {
        return verified.get(player);
    }

    void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");

        try {

            if (configFile.createNewFile()) {

                try (InputStream is = getResourceAsStream("config.yml");
                     OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Configuration getConfig() {
        return config;
    }


}
