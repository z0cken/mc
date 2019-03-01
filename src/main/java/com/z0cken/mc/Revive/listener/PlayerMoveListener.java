package com.z0cken.mc.Revive.listener;

import com.z0cken.mc.Revive.RespawnHandler;
import com.z0cken.mc.Revive.RespawnPhase;
import com.z0cken.mc.Revive.Revive;
import com.z0cken.mc.Revive.utils.PacketUtils;
import com.z0cken.mc.Revive.utils.PlayerUtils;
import com.z0cken.mc.Revive.utils.TargetUtils;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class PlayerMoveListener implements Listener {

    private HashMap<UUID, RespawnPhase> highlightedPlayer = new HashMap<>();

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
                        if (respawnPhase.getExternalBossBar().getPlayers().contains(event.getPlayer())) {
                            respawnPhase.getExternalBossBar().getPlayers().remove(event.getPlayer());
                        }
                    }
                }
        }

        if (PlayerUtils.isHolding(event.getPlayer(), Material.TOTEM_OF_UNDYING)) {
            //We don't need to scan the environment if there is no nearby dead player
            if (!RespawnHandler.getHandler().hasNearbyDeadPlayer(event.getTo(), 100)) {
                removeHighlightPlayer(event.getPlayer());
                return;
            }

            LivingEntity targetEntity = TargetUtils.getTargetEntity(event.getPlayer());
            if (targetEntity != null && RespawnHandler.getHandler().isRespawning(targetEntity)) {
                highlightPlayer(event.getPlayer(), RespawnHandler.getHandler().getRespawnInstance(targetEntity));
            } else {
                //Remove highlight, because we aren't targetting anything valid
                removeHighlightPlayer(event.getPlayer());
            }
        } else {
            removeHighlightPlayer(event.getPlayer());
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

    private void highlightPlayer(Player viewer, RespawnPhase target) {
        if (highlightedPlayer.containsKey(viewer.getUniqueId())) {
            if(highlightedPlayer.get(viewer.getUniqueId()) != target) {
                //Remove previous entries
                removeHighlightPlayer(viewer, highlightedPlayer.get(viewer.getUniqueId()));
            } else {
                //We are already handling that player
                return;
            }
        }

        highlightedPlayer.put(viewer.getUniqueId(), target);

        //We need to update it constantly even if the player is not moving
        new BukkitRunnable() {
            @Override
            public void run() {
                if (highlightedPlayer.get(viewer.getUniqueId()) == target) {
                    PacketUtils.setGlowing(viewer, (Player) target.getNPC().getEntity(), true);
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Revive.getPlugin(), 0L, 1L);
    }

    private void removeHighlightPlayer(Player viewer) {
        if (highlightedPlayer.containsKey(viewer.getUniqueId())) {
            removeHighlightPlayer(viewer, highlightedPlayer.get(viewer.getUniqueId()));
        }
    }

    private void removeHighlightPlayer(Player viewer, RespawnPhase target) {
        highlightedPlayer.remove(viewer.getUniqueId(), target);

        PacketUtils.setGlowing(viewer, (Player) target.getNPC().getEntity(), false);
    }
}
