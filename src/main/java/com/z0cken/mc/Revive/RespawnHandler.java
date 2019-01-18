package com.z0cken.mc.Revive;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class RespawnHandler {

    public final static int SECONDS_TILL_RESPAWN_AVAILABLE = 5;

    public final static int SECONDS_TILL_RESPAWN_FORCE = 60;

    public final static double BOSSBAR_RADIUS_BLOCKS_SQUARED = NumberConversions.square(10);

    private static RespawnHandler handler = new RespawnHandler();

    private static HashMap<UUID, RespawnPhase> playerRespawns = new HashMap<UUID, RespawnPhase>();

    public void handlePlayerDeath(Player player) {
        if(isRespawning(player)) {
            Bukkit.getLogger().log(Level.WARNING, "Player '" + player.getName() + " is already respawning...");
            return;
        }

        RespawnPhase respawnPhase = new RespawnPhase(player);
        playerRespawns.put(player.getUniqueId(), respawnPhase);
        respawnPhase.startRespawnPhase();
    }

    public void removeRespawn(Player player) {
        if(!isRespawning(player.getUniqueId())) {
            Bukkit.getLogger().log(Level.WARNING, "Tried to handle respawn even though '" + player.getName() + " is alive...");
            return;
        }

        playerRespawns.remove(player.getUniqueId());
    }

    public RespawnPhase getRespawnInstance(Player player) {
        if(!isRespawning(player.getUniqueId())) {
            Bukkit.getLogger().log(Level.WARNING, "Tried to handle respawn even though '" + player.getName() + " is alive...");
            return null;
        }

        return playerRespawns.get(player.getUniqueId());
    }

    public static HashMap<UUID, RespawnPhase> getPlayerRespawns() {
        return playerRespawns;
    }

    public boolean isRespawning(Player player) {
        return isRespawning(player.getUniqueId());
    }

    public boolean isRespawning(UUID uuid) {
        return playerRespawns.containsKey(uuid);
    }

    public static RespawnHandler getHandler() {
        return handler;
    }

    public static void setHandler(RespawnHandler handler) {
        RespawnHandler.handler = handler;
    }

}

