package com.z0cken.mc.checkpoint;

import net.md_5.bungee.api.ChatColor;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** @author Flare */

public class PCS_Checkpoint extends Plugin implements Listener {

    private static PCS_Checkpoint instance;

    public static PCS_Checkpoint getInstance() {
        return instance;
    }

    private static final HashMap<ProxiedPlayer, Persona> verified = new HashMap<>();
    private static final Collection<ProxiedPlayer> guests = new ArrayList<>();
    private Configuration config;

    /*
     * TODO Dynamic UI
     * TODO Async REST & SQL ?
     * TODO Admin Commands
     */

    @Override
    public void onEnable() {
        instance = this;

        loadConfig();

        getProxy().getPluginManager().registerCommand(this, new CommandVerify());
        getProxy().getPluginManager().registerCommand(this, new CommandInvite());
        getProxy().getPluginManager().registerListener(this,this);

        new DatabaseHelper();

        getProxy().getScheduler().schedule(this, RequestHelper::checkVerifications, 0 , config.getInt("bot-interval"), TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
        //Discards potential other unsaved invocations of Configuration#set
        //Prevents runtime changes by user to be overwritten
        loadConfig();
        config.set("lastCheck", RequestHelper.lastCheck);

        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        checkPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        if(!(verified.containsKey(event.getPlayer()) || guests.contains(event.getPlayer())) && !event.getTarget().getName().equals("hub")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c§l>_ §7Bitte nutze §c§l/checkpoint §7um deinen Account zu bestätigen!");

        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        verified.remove(event.getPlayer());
        guests.remove(event.getPlayer());
    }

    void checkPlayer(UUID uuid) {
        ProxiedPlayer player = getProxy().getPlayer(uuid);
        if(player == null) return;

        Persona persona = DatabaseHelper.getPersona(uuid);

        if(persona == null) {
            if(DatabaseHelper.isGuest(uuid)) guests.add(player);
        } else {
            if(persona.isBanned()) {
                player.disconnect();
            } else {
                verified.put(player, persona);
            }
        }
    }

    Persona getPersona(ProxiedPlayer player) {
        return verified.get(player);
    }

    private void loadConfig() {
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Configuration getConfig() {
        return config;
    }


}
