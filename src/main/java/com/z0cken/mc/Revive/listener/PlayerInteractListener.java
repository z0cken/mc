package com.z0cken.mc.Revive.listener;

import com.z0cken.mc.Revive.RespawnHandler;
import com.z0cken.mc.Revive.RespawnPhase;
import com.z0cken.mc.Revive.Revive;
import com.z0cken.mc.Revive.utils.PlayerUtils;
import com.z0cken.mc.Revive.utils.TargetUtils;
import com.z0cken.mc.core.util.MessageBuilder;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            if (PlayerUtils.isHolding(event.getPlayer(), Material.TOTEM_OF_UNDYING)) {
                LivingEntity entity = TargetUtils.getTargetEntity(event.getPlayer());


                //instanceof handles null case too && check if the entity is respawning
                if (entity != null && RespawnHandler.getHandler().isRespawning(entity)) {
                    PlayerUtils.removeFromHand(event.getPlayer(), Material.TOTEM_OF_UNDYING, 1);

                    RespawnPhase respawnPhase = RespawnHandler.getHandler().getRespawnInstance(entity);
                    respawnPhase.revivePlayer();

                    respawnPhase.getPlayer().spigot().sendMessage(new MessageBuilder().define("PLAYER", event.getPlayer().getName()).build(Revive.getPlugin().getConfig().getString("revive-toter")));
                    //respawnPhase.getPlayer().sendMessage(ChatColor.GRAY + event.getPlayer().getName() + ChatColor.GREEN + " hat dich wiederbelebt!");
                    event.getPlayer().spigot().sendMessage(new MessageBuilder().define("PLAYER", respawnPhase.getPlayer().getName()).build(Revive.getPlugin().getConfig().getString("revive-spieler")));
                    //event.getPlayer().sendMessage(ChatColor.GREEN + "Du hast erfolgreich " + ChatColor.GRAY + event.getPlayer().getName() + ChatColor.GREEN + " wiederbelebt!");
                }
            }
        }
    }
}
