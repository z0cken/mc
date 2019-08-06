package com.z0cken.mc.end.phase;

import com.z0cken.mc.end.End;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class MaintenancePhase extends EndPhase implements Listener {

    MaintenancePhase(End end) {
        super(PhaseType.MAINTENANCE, end);
    }

    @Override
    public void start() {
        getEnd().getWorld().getPlayers().stream().filter(p -> !p.hasPermission("pcs.end.bypass")).forEach(p -> p.teleport(Bukkit.getWorld("world").getSpawnLocation()));
    }

    @Override
    public void stop() {
        super.stop();
        getEnd().save();
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if(event.getTo().getWorld().equals(getEnd().getWorld()) && !event.getPlayer().hasPermission("pcs.end.bypass")) {
            event.setCancelled(true);
        }
    }
}
