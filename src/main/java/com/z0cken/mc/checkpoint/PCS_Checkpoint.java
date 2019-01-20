package com.z0cken.mc.checkpoint;

import com.google.common.io.ByteStreams;
import com.z0cken.mc.util.MessageBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** @author Flare */

public final class PCS_Checkpoint extends Plugin implements Listener {

    private static PCS_Checkpoint instance;
    private static MessageBuilder messageBuilder;

    static PCS_Checkpoint getInstance() {
        return instance;
    }

    private static final HashMap<ProxiedPlayer, Persona> verified = new HashMap<>();
    private static final Collection<ProxiedPlayer> guests = new ArrayList<>();
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

        DatabaseHelper.connect();

        getProxy().getScheduler().schedule(this, RequestHelper::checkVerifications, 0 , config.getInt("bot.interval"), TimeUnit.SECONDS);
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

        DatabaseHelper.disconnect();

        instance = null;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        checkPlayer(event.getPlayer().getUniqueId(), false);
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        if(!(verified.containsKey(event.getPlayer()) || guests.contains(event.getPlayer())) && !event.getTarget().getName().equals(config.getString("hub-name"))) {
            event.setTarget(getProxy().getServerInfo(config.getString("hub-name")));
            event.getPlayer().sendMessage(messageBuilder.build(config.getString("messages.verify.switch")));
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        verified.remove(event.getPlayer());
        guests.remove(event.getPlayer());
    }

    void checkPlayer(UUID uuid, boolean verbose) {
        ProxiedPlayer player = getProxy().getPlayer(uuid);
        if(player == null) return;

        Persona persona = DatabaseHelper.getPersona(player.getUniqueId());

        if(persona == null) {
            if(DatabaseHelper.isGuest(uuid)) {
                guests.add(player);
                if(verbose) player.sendMessage(messageBuilder.build(PCS_Checkpoint.getConfig().getString("messages.invite.confirmed-yes-guest")));
            }
        } else {
            if(persona.isBanned()) {
                //TODO Reason
                player.disconnect();
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
