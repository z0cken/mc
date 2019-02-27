package com.z0cken.mc.metro.listener;

import com.z0cken.mc.metro.Metro;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class ProtectionListener implements Listener {

    private static boolean instantiated;

    public ProtectionListener() {
        if(instantiated) throw new IllegalStateException(this.getClass().getName() + " cannot be instantiated twice!");
        instantiated = true;
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if(Metro.getInstance().getPlayersInside().contains(player) && !player.hasPermission("pcs.metro.bypass")) {
            event.setCancelled(true);
            //TODO msg
        }

    }
}
