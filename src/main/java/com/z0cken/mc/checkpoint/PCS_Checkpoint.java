package com.z0cken.mc.checkpoint;

import com.google.common.io.ByteStreams;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.z0cken.mc.core.persona.Persona;
import com.z0cken.mc.core.persona.PersonaAPI;
import com.z0cken.mc.core.util.MessageBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import org.apache.http.client.HttpResponseException;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** @author Flare */
public final class PCS_Checkpoint extends Plugin implements Listener {

    private static PCS_Checkpoint instance;
    private static MessageBuilder messageBuilder;

    static PCS_Checkpoint getInstance() {
        return instance;
    }

    private static Configuration config;
    private static ServerInfo hub;

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
        hub = getProxy().getServerInfo(config.getString("hub-name"));
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
        checkPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {

        if(!event.getTarget().equals(hub)) {

            Persona persona;

            try {
                persona = PersonaAPI.getPersona(event.getPlayer().getUniqueId());
            } catch (HttpResponseException | UnirestException | SQLException e) {
                e.printStackTrace();

                if(!event.getPlayer().getServer().getInfo().equals(hub)) event.setTarget(hub);
                event.getPlayer().sendMessage(MessageBuilder.DEFAULT.build(config.getString("messages.error")));
                return;
            }

            if(persona == null) {
                if(!event.getPlayer().getServer().getInfo().equals(hub)) event.setTarget(hub);
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
        } catch (SQLException | UnirestException | HttpResponseException e) {
            e.printStackTrace();
        }

        if(persona != null && persona.isBanned()) {
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
        }
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
