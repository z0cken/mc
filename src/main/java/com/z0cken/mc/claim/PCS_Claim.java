package com.z0cken.mc.claim;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/** @author Flare */
@SuppressWarnings("unused")
public final class PCS_Claim extends JavaPlugin {

    private static PCS_Claim instance;

    public static PCS_Claim getInstance() {
        return instance;
    }

    private static final ConcurrentHashMap<Chunk, OfflinePlayer> claims = new ConcurrentHashMap<>();

    public PCS_Claim() {
        if(instance != null) throw new IllegalStateException(this.getClass().getName() + " cannot be instantiated twice!");
    }

    @Override
    public void onLoad() {
        instance = this;
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public void onEnable() {
        DatabaseHelper.connect();

        try {
            DatabaseHelper.populate(claims, Bukkit.getWorld(getConfig().getString("main-world")));
        } catch (SQLException e) {
            Bukkit.getServer().shutdown();
        }

        Bukkit.getPluginManager().registerEvents(new ClaimListener(), this);
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(), this);
    }

    @Override
    public void onDisable() {
        DatabaseHelper.push();
        DatabaseHelper.disconnect();

        instance = null;
    }

    static void claim(Chunk chunk, Player player) {
        DatabaseHelper.commit(new AbstractMap.SimpleEntry<>(chunk, player));

        if(player == null) {
            claims.remove(chunk);
        } else {
            claims.put(chunk, player);
        }
    }

    static boolean canBuild(OfflinePlayer player, Chunk chunk) {
        if(claims.containsKey(chunk)) {
            OfflinePlayer owner = claims.get(chunk);

            if(player.isOnline() && player.getPlayer().hasPermission("pcs.claim.override")) return true;

            //TODO areFriends
            boolean friends = false;

            return owner.equals(player) || friends;
        }

        return true;
    }

    static OfflinePlayer getOwner(Chunk chunk) {
        return claims.getOrDefault(chunk, null);
    }

    static ArrayList<Chunk> getClaims(OfflinePlayer player) {
        ArrayList<Chunk> list = new ArrayList<>();
        claims.forEach((chunk, player1) -> { if(player.equals(player1)) list.add(chunk); });
        return list;
    }
}
