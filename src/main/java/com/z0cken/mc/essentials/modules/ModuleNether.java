package com.z0cken.mc.essentials.modules;

import com.z0cken.mc.core.util.MessageBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class ModuleNether extends Module implements Listener {
    ModuleNether(String configPath) {
        super(configPath);
    }

    private static boolean travelToOverworld;
    private static BaseComponent[] denyMessage;
    private static Location netherSpawn;

    @Override
    protected void load() {
        travelToOverworld = getConfig().getBoolean("travel-to-overworld");
        final String msg = getConfig().getString("messages.travel-disabled");
        denyMessage = msg == null ? null : MessageBuilder.DEFAULT.build(msg);
        netherSpawn = getConfig().getSerializable("nether-spawn", Location.class);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if(event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            if(event.getTo().getWorld().getEnvironment() == World.Environment.NETHER) {
                if(netherSpawn != null) {
                    event.setTo(netherSpawn);
                } else {
                    event.getPlayer().spigot().sendMessage(denyMessage);
                    event.setCancelled(true);
                }
            } else {
                if(!travelToOverworld) {
                    event.setCancelled(true);
                    event.getPlayer().spigot().sendMessage(denyMessage);
                }
            }
        }

    }
}
