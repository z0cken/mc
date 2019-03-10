package com.z0cken.mc.checkpoint;

import com.google.common.io.ByteStreams;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.z0cken.mc.core.persona.Persona;
import com.z0cken.mc.core.persona.PersonaAPI;
import com.z0cken.mc.core.util.MessageBuilder;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LuckPermsApi;
import net.md_5.bungee.api.ChatMessageType;
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
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
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
    private static Queue<ProxiedPlayer> queue = new LinkedList<>();
    private static Configuration config;
    private static ServerInfo hub;
    private static ServerInfo main;
    private static int mainSlots;

    private static ScheduledTask messageTask, queueTask;

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
        load();

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
        final ProxiedPlayer player = event.getPlayer();
        checkPlayer(player.getUniqueId());

        if(player.isConnected()) {
            if(player.getReconnectServer().equals(main) && main.getPlayers().size() >= mainSlots) {
                queue.add(player);
                player.connect(hub);
                player.sendMessage(MessageBuilder.DEFAULT.build(config.getString("messages.queue.insert")));
            }
        }
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        if(hub == null) return;
        final ProxiedPlayer player = event.getPlayer();

        if(event.getTarget().equals(main)) {

            Persona persona;

            try {
                persona = PersonaAPI.getPersona(player.getUniqueId());
            } catch (HttpResponseException | UnirestException | SQLException e) {
                e.printStackTrace();

                event.setCancelled(true);
                player.sendMessage(MessageBuilder.DEFAULT.build(config.getString("messages.error")));
                return;
            }

            if (persona == null) {
                event.setCancelled(true);
                player.sendMessage(messageBuilder.build(config.getString("messages.verify.prompt")));
                return;
            }

            if(main.getPlayers().size() >= mainSlots && !player.hasPermission("essentials.joinfullserver")) {
                event.setCancelled(true);
                queue.add(player);
                player.sendMessage(MessageBuilder.DEFAULT.build(config.getString("messages.queue.insert")));
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


    private void processQueue() {
        int freeSlots = mainSlots - main.getPlayers().size();

        for(int i = 0; i < freeSlots; i++) queue.poll().connect(main);

        int i = 1;
        for(ProxiedPlayer player : queue) {
            player.sendMessage(ChatMessageType.ACTION_BAR, MessageBuilder.DEFAULT.define("AMOUNT", Integer.toString(i)).build(config.getString("messages.queue.notify")));
            i++;
        }
    }

    void load() {
        loadConfig();
        config.set("bot.timestamp", RequestHelper.timestamp);

        hub = getProxy().getServerInfo(config.getString("hub-name"));
        main = getProxy().getServerInfo(config.getString("main-name"));
        mainSlots = config.getInt("main-slots");

        messageBuilder = new MessageBuilder().define("A", getConfig().getString("messages.accent-color"));

        RequestHelper.load();
        CommandVerify.load();
        CommandInvite.load();
        CommandAnon.load();

        if(messageTask != null) messageTask.cancel();
        messageTask = getProxy().getScheduler().schedule(this, RequestHelper::fetchMessages, 5 , config.getInt("bot.interval"), TimeUnit.SECONDS);

        if(queueTask != null) queueTask.cancel();
        queueTask = getProxy().getScheduler().schedule(this, this::processQueue, 5 , config.getInt("queue-interval"), TimeUnit.SECONDS);
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
