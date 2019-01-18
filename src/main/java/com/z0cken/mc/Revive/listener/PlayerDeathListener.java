package com.z0cken.mc.Revive.listener;

import com.z0cken.mc.Revive.RespawnHandler;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        event.setKeepInventory(true);
        event.setKeepLevel(true);

        //Restore health so he stays alive
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        RespawnHandler.getHandler().handlePlayerDeath(player);
    }
}
