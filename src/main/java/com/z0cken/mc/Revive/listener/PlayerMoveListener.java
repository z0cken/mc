package com.z0cken.mc.Revive.listener;

import com.z0cken.mc.Revive.RespawnHandler;
import com.z0cken.mc.Revive.RespawnPhase;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class PlayerMoveListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        //Only check when we moved a whole block so we don't always check
        if (event.getFrom().getBlock().getLocation().equals(event.getTo().getBlock().getLocation())) {
            for (RespawnPhase respawnPhase : RespawnHandler.getHandler().getPlayerRespawns().values())
                if (respawnPhase.getPlayer() != event.getPlayer()) {
                    //Check for the right distance
                    if (respawnPhase.getPlayer().getLocation().distanceSquared(event.getPlayer().getLocation()) < RespawnHandler.BOSSBAR_RADIUS_BLOCKS_SQUARED) {
                        respawnPhase.tryShowBossBarTo(event.getPlayer());
                    } else {
                        //If we are out of range again remove boss bar
                        if(respawnPhase.getBossBar().getPlayers().contains(event.getPlayer())) {
                            respawnPhase.getBossBar().getPlayers().remove(event.getPlayer());
                        }
                    }
                }
        }

        if(event.getPlayer().getInventory().getItemInMainHand() != null && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING) {
            //TODO: Highlight target player

        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) {
            return;
        }

        //We want to cancel sneak attempts so he can't exit the spectator entity for the time being
        if (RespawnHandler.getHandler().isRespawning(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
