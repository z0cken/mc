package com.z0cken.mc.progression;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("WeakerAccess")
public class PCS_Progression extends JavaPlugin implements Listener {

    private static PCS_Progression instance;

    public static PCS_Progression getInstance() {
        return instance;
    }

    public PCS_Progression() {
        if(instance != null) throw new IllegalStateException(this.getClass().getName() + " cannot be instantiated twice!");
    }

    static final Map<Player, Map<String, Integer>> progressionData = new ConcurrentHashMap<>();

    @Override
    public void onLoad() {
        instance = this;
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        DatabaseHelper.push();

        instance = null;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        progressionData.putIfAbsent(event.getPlayer(), DatabaseHelper.getProgression(event.getPlayer()));
    }

    public static Integer getProgression(OfflinePlayer player, String name) {
        if(player.isOnline()) return progressionData.get(player.getPlayer()).getOrDefault(name, 0);
        else return DatabaseHelper.getProgression(player).getOrDefault(name, 0);
    }

    public static void progress(Player player, String name, int amount) {
        Map<String, Integer> map = progressionData.get(player);

        int current = map.getOrDefault(name, 0);
        map.put(name, current + amount);

        //Edge case where the thread pushing a disconnected player's data subsequently removes his key
        progressionData.putIfAbsent(player, map);
    }

    public static Map<OfflinePlayer, Integer> getLeaderboard(String column, int limit) throws SQLException {
        return DatabaseHelper.getLeaderboard(column, limit);
    }

    public static int getSum(String column) throws SQLException {
        return DatabaseHelper.getSum(column);
    }

}
