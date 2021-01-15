package com.z0cken.mc.essentials.modules;

import com.z0cken.mc.claim.Claim;
import com.z0cken.mc.claim.PCS_Claim;
import com.z0cken.mc.core.util.MessageBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.PortalCreateEvent;

public class ModuleNether extends Module implements Listener {
    ModuleNether(String configPath) {
        super(configPath);
    }

    private static boolean travelToOverworld;
    private static BaseComponent[] denyMsgBlocked, denyMsgClaimed;
    private static Location netherSpawn;

    @Override
    protected void load() {
        travelToOverworld = getConfig().getBoolean("travel-to-overworld");
        final String travelDisabled = getConfig().getString("messages.travel-disabled");
        denyMsgBlocked = travelDisabled == null ? null : MessageBuilder.DEFAULT.build(travelDisabled);

        final String targetClaimed = getConfig().getString("messages.target-claimed");
        denyMsgClaimed = targetClaimed == null ? null : MessageBuilder.DEFAULT.build(targetClaimed);

        netherSpawn = getConfig().getSerializable("nether-spawn", Location.class);
        getLogger().info("Nether spawn set to " + netherSpawn);
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        if(event.getReason() == PortalCreateEvent.CreateReason.NETHER_PAIR && event.getWorld().getEnvironment() == World.Environment.NETHER) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if(event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            if(event.getFrom().getWorld().getEnvironment() == World.Environment.NORMAL) {
                if(netherSpawn != null) {
                    event.setTo(netherSpawn);
                } else {
                    event.setCancelled(true);
                    event.getPlayer().spigot().sendMessage(denyMsgBlocked);
                }
            } else if(event.getFrom().getWorld().getEnvironment() == World.Environment.NETHER) {
                if(!travelToOverworld) {
                    event.setCancelled(true);
                    event.getPlayer().spigot().sendMessage(denyMsgBlocked);
                } else {
                    final Location target = event.getTo().getWorld().getHighestBlockAt(event.getTo()).getLocation();
                    Claim claim = PCS_Claim.getClaim(target.getChunk());
                    if(claim == null || claim.canBuild(event.getPlayer())) {
                        event.setTo(target);
                    } else {
                        event.setCancelled(true);
                        event.getPlayer().spigot().sendMessage(denyMsgClaimed);
                    }
                }
            }
        }
    }
}
