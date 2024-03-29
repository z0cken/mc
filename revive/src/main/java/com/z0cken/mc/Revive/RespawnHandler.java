package com.z0cken.mc.Revive;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
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

    private static HashMap<UUID, RespawnPhase> playerRespawns = new HashMap<>();

    public void handlePlayerDeath(Player player, int expToDrop) {
        if (isRespawning(player)) {
            Bukkit.getLogger().log(Level.WARNING, "Player '" + player.getName() + " is already respawning...");
            return;
        }

        RespawnPhase respawnPhase = new RespawnPhase(player, expToDrop);
        playerRespawns.put(player.getUniqueId(), respawnPhase);
        respawnPhase.startRespawnPhase();
    }

    public void removeRespawn(Player player) {
        if (!isRespawning(player.getUniqueId())) {
            Bukkit.getLogger().log(Level.WARNING, "Tried to handle respawn even though '" + player.getName() + " is alive...");
            return;
        }

        playerRespawns.remove(player.getUniqueId());
    }

    public RespawnPhase getRespawnInstance(Entity player) {
        if (player.hasMetadata("NPC") || player instanceof ArmorStand) {
            Player target = Bukkit.getPlayer(player.getName());

            if (target != null) {
                RespawnPhase respawnPhase = getRespawnInstance(target);

                if(respawnPhase != null && (respawnPhase.getNPC().getEntity() == player || respawnPhase.getArmorStand() == player)) {
                    return respawnPhase;
                }
            }
        }

        return playerRespawns.get(player.getUniqueId());
    }

    public boolean hasNearbyDeadPlayer(Location location, double rangeSquared) {
        for (RespawnPhase respawnPhase : playerRespawns.values()) {
            if (respawnPhase.getPlayer().getLocation().distanceSquared(location) <= rangeSquared) {
                return true;
            }
        }

        return false;
    }

    public static HashMap<UUID, RespawnPhase> getPlayerRespawns() {
        return playerRespawns;
    }

    public boolean isRespawning(Entity player) {
        return getRespawnInstance(player) != null;
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

