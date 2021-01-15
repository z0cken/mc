package com.z0cken.mc.checkpoint;

import com.google.common.io.ByteStreams;
import com.mashape.unirest.http.Unirest;
import com.z0cken.mc.core.persona.BoardProfile;
import com.z0cken.mc.core.persona.Persona;
import com.z0cken.mc.core.persona.PersonaAPI;
import com.z0cken.mc.core.util.MessageBuilder;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LuckPermsApi;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;

import java.io.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** @author Flare */
public final class PCS_Checkpoint extends Plugin implements Listener {

    private static PCS_Checkpoint instance;

    static PCS_Checkpoint getInstance() {
        return instance;
    }

    private static LuckPermsApi luckPermsApi;

    private static Queue<ProxiedPlayer> queue = new LinkedList<>();
    private static Collection<Pattern> publicPatterns;
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
        luckPermsApi = LuckPerms.getApi();

        getProxy().getPluginManager().registerCommand(this, new CommandCheckpoint());
        getProxy().getPluginManager().registerCommand(this, new CommandVerify());
        getProxy().getPluginManager().registerCommand(this, new CommandInvite());
        getProxy().getPluginManager().registerCommand(this, new CommandAnon());
        getProxy().getPluginManager().registerCommand(this, new CommandTerms());
        getProxy().getPluginManager().registerListener(this,this);

        DatabaseHelper.setupTables();
    }

    @Override
    public void onDisable() {
        //Discards potential other unsaved invocations of Configuration#set
        //Prevents runtime changes by user to be overwritten
        loadConfig();
        config.set("bot.timestamp", RequestHelper.timestamp);
        config.set("bot.cookie", RequestHelper.cookie);

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
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        queue.remove(event.getPlayer());
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        if(event.getReason() == ServerConnectEvent.Reason.PLUGIN) return;
        final ProxiedPlayer player = event.getPlayer();

        if(!event.getTarget().equals(hub)) {

            Persona persona = PersonaAPI.getPersona(player.getUniqueId());
            boolean pub = publicPatterns.stream().anyMatch(p -> p.matcher(event.getTarget().getName()).find());

            if(!persona.isVerified() && !persona.isGuest() && !pub) {
                player.sendMessage(MessageBuilder.DEFAULT.build(config.getString("messages.verify.prompt")));
                if(player.getServer() == null) event.setTarget(hub);
                else event.setCancelled(true);
                return;
            } else if(config.getBoolean("require-terms") && !persona.hasAcceptedTerms()) {
                config.getStringList("messages.terms.prompt").forEach(s -> player.sendMessage(MessageBuilder.DEFAULT.build(s)));
                if(player.getServer() == null) event.setTarget(hub);
                else event.setCancelled(true);
                return;
            }

            if(event.getTarget().equals(main) && (main.getPlayers().size() >= mainSlots || !queue.isEmpty()) && !queue.contains(player) && !player.hasPermission("essentials.joinfullserver")) {
                queue.add(player);
                player.sendMessage(MessageBuilder.DEFAULT.build(config.getString("messages.queue.insert")));
                if(event.getReason() == ServerConnectEvent.Reason.JOIN_PROXY) event.setTarget(hub);
                else event.setCancelled(true);
            }
        }
    }

    void checkPlayer(UUID uuid) {
        ProxiedPlayer player = getProxy().getPlayer(uuid);
        if(player == null) return;

        final Persona persona = PersonaAPI.getPersona(uuid);
        if(persona.isVerified()) {
            persona.getBoardProfile().thenAcceptAsync(profile -> {

                if(profile.isBanned() && !player.hasPermission("pcs.checkpoint.bypass")) {
                    LocalDateTime bannedUntil = profile.getBannedUntil();

                    String msg;
                    String time = null;

                    //TODO Separate date and time
                    if(bannedUntil == null) msg = PCS_Checkpoint.getConfig().getString("messages.banned-permanent");
                    else {
                        time = bannedUntil.format(DateTimeFormatter.ofPattern("dd.MM.yy 'um' HH:mm"));
                        msg = PCS_Checkpoint.getConfig().getString("messages.banned");
                    }

                    player.disconnect(MessageBuilder.DEFAULT.define("TIME", time).build(msg));
                    return;
                }

                BoardProfile.Mark mark = profile.getMark();

                luckPermsApi.getUserManager().loadUser(uuid).thenAcceptAsync(user -> {
                    String add = null, remove = null;
                    switch (user.getPrimaryGroup()) {
                        case "default":
                            add = mark == BoardProfile.Mark.SPENDER ? "premium" : "member";
                            remove = "default";
                            break;

                        case "guest":
                            add = mark == BoardProfile.Mark.SPENDER ? "premium" : "member";
                            remove = "guest";
                            break;

                        case "member":
                            if(mark == BoardProfile.Mark.SPENDER) {
                                add = "premium";
                                remove = "member";
                                getLogger().info( profile.getName() + " hat sich pr0mium gekauft!");
                            }
                            break;

                        case "premium":
                            if(mark != BoardProfile.Mark.SPENDER) {
                                add = "member";
                                remove = "premium";
                            }
                            break;
                    }

                    //Careful, only works if always neither or both are assigned
                    if(add == null) return;
                    user.setPermission(luckPermsApi.getNodeFactory().makeGroupNode(add).build());
                    user.unsetPermission(luckPermsApi.getNodeFactory().makeGroupNode(remove).build());
                    luckPermsApi.getUserManager().saveUser(user);
                });
            });

        } else if(persona.isGuest()) {
            luckPermsApi.getUserManager().loadUser(uuid).thenAcceptAsync(user -> {
                if(user.getPrimaryGroup().equalsIgnoreCase("default")) {
                    user.setPermission(luckPermsApi.getNodeFactory().makeGroupNode("guest").build());
                    user.unsetPermission(luckPermsApi.getNodeFactory().makeGroupNode("default").build());
                    luckPermsApi.getUserManager().saveUser(user);
                }
            });
        }
    }

    private void processQueue() {
        if(queue.isEmpty()) return;
        int freeSlots = mainSlots - main.getPlayers().size();

        for(int i = 0; i < freeSlots; i++) queue.poll().connect(main);

        int i = 1;
        for(ProxiedPlayer player : queue) player.sendMessage(ChatMessageType.ACTION_BAR, MessageBuilder.DEFAULT.define("AMOUNT", Integer.toString(i++)).build(config.getString("messages.queue.notify")));
    }

    void load() {
        loadConfig();

        hub = getProxy().getServerInfo(config.getString("hub-name"));
        main = getProxy().getServerInfo(config.getString("main-name"));
        mainSlots = config.getInt("main-slots");
        publicPatterns = config.getStringList("public").stream().map(Pattern::compile).collect(Collectors.toSet());

        //Workaround for https://stackoverflow.com/q/52574050/12076840
        try {
            Unirest.setHttpClient(HttpClientBuilder.create().setSSLContext(SSLContexts.custom().useProtocol("TLSv1.2").build()).build());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

        RequestHelper.load();
        CommandVerify.load();
        CommandInvite.load();
        CommandAnon.load();

        //In case of reload, cancel existing tasks
        if(messageTask != null) messageTask.cancel();
        messageTask = getProxy().getScheduler().schedule(this, RequestHelper::fetchConversations, 5 , config.getInt("bot.interval"), TimeUnit.SECONDS);

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
