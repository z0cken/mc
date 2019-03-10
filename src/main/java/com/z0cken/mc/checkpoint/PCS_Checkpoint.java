package com.z0cken.mc.checkpoint;

import com.google.common.io.ByteStreams;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.z0cken.mc.core.persona.Persona;
import com.z0cken.mc.core.persona.PersonaAPI;
import com.z0cken.mc.core.util.MessageBuilder;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LuckPermsApi;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import org.apache.http.client.HttpResponseException;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** @author Flare */
public final class PCS_Checkpoint extends Plugin implements Listener {

    private static PCS_Checkpoint instance;
    private static MessageBuilder messageBuilder;

    static PCS_Checkpoint getInstance() {
        return instance;
    }

    private static Optional<LuckPermsApi> luckPermsApi;
    private static Configuration config;
    private static ServerInfo hub;

    private static ScheduledTask messageTask;

    /*
     * TODO Async REST & SQL ?
     * TODO Admin Commands
     * TODO Give invites
     * TODO Detect server outages
     */

    @Override
    public void onLoad() {
        instance = this;
        loadConfig();
    }

    @Override
    public void onEnable() {
        messageBuilder = new MessageBuilder().define("A", getConfig().getString("messages.accent-color"));

        if(ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") == null) {
            getLogger().severe("LuckPerms not enabled - shutting down");
            getProxy().stop();
            return;
        } else {
            luckPermsApi = LuckPerms.getApiSafe();
        }

        getProxy().getPluginManager().registerCommand(this, new CommandCheckpoint());
        getProxy().getPluginManager().registerCommand(this, new CommandVerify());
        getProxy().getPluginManager().registerCommand(this, new CommandInvite());
        getProxy().getPluginManager().registerCommand(this, new CommandAnon());
        getProxy().getPluginManager().registerListener(this,this);

        DatabaseHelper.setupTables();
        messageTask = getProxy().getScheduler().schedule(this, RequestHelper::fetchMessages, 5 , config.getInt("bot.interval"), TimeUnit.SECONDS);
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
        checkPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        if(hub == null) hub = getProxy().getServerInfo(config.getString("hub-name"));

        if(!event.getTarget().equals(hub)) {

            Persona persona;

            try {
                persona = PersonaAPI.getPersona(event.getPlayer().getUniqueId());
            } catch (HttpResponseException | UnirestException | SQLException e) {
                e.printStackTrace();

                event.setCancelled(true);
                event.getPlayer().sendMessage(MessageBuilder.DEFAULT.build(config.getString("messages.error")));
                return;
            }

            if(persona == null) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(messageBuilder.build(config.getString("messages.verify.prompt")));
            }
        }
    }

    void checkPlayer(UUID uuid) {
        ProxiedPlayer player = getProxy().getPlayer(uuid);
        if(player == null) return;

        Persona persona = null;
        try {
            persona = PersonaAPI.getPersona(uuid);
        } catch (UnirestException | HttpResponseException e) {
            getLogger().severe(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(persona != null) {
            if(persona.isBanned()) {
                LocalDateTime bannedUntil = persona.getBannedUntil();

                String msg;
                String time = null;

                //TODO Separate date and time
                if(bannedUntil == null) msg = PCS_Checkpoint.getConfig().getString("messages.banned-permanent");
                else {
                    time = bannedUntil.format(DateTimeFormatter.ofPattern("dd.MM.yy 'um' HH:mm"));
                    msg = PCS_Checkpoint.getConfig().getString("messages.banned");
                }

                player.disconnect(messageBuilder.define("TIME", time).build(msg));
            } else {
                if(luckPermsApi.isPresent()) {
                    final String primaryGroup = luckPermsApi.get().getUserManager().getUser(player.getName()).getPrimaryGroup();
                    if (primaryGroup.equalsIgnoreCase("default")) {
                        if(!persona.isGuest()) ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), String.format("lpb user %s parent set %s", player.getName(), getConfig().getString("verify-group")));
                        else ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), String.format("lpb user %s parent set %s", player.getName(), getConfig().getString("invite-group")));
                    }
                }
            }
        }
    }

    void reload() {
        loadConfig();
        config.set("bot.timestamp", RequestHelper.timestamp);

        messageBuilder = new MessageBuilder().define("A", getConfig().getString("messages.accent-color"));

        RequestHelper.load();

        messageTask.cancel();
        messageTask = getProxy().getScheduler().schedule(this, RequestHelper::fetchMessages, 5 , config.getInt("bot.interval"), TimeUnit.SECONDS);

        CommandVerify.load();
        CommandInvite.load();
        CommandAnon.load();
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
