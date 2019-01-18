package com.z0cken.mc.Revive.listener;

import com.z0cken.mc.Revive.RespawnHandler;
import com.z0cken.mc.Revive.utils.TargetUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            LivingEntity entity = TargetUtils.getTargetEntity(event.getPlayer());

            //instanceof handles null case too && check if the entity is respawning
            if(entity instanceof Player && RespawnHandler.getHandler().isRespawning(entity.getUniqueId())) {
                Player targetPlayer = (Player) entity;

                //TODO: Revive action
            }
        }
    }
}
