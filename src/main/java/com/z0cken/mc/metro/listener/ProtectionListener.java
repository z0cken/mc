package com.z0cken.mc.metro.listener;

import com.z0cken.mc.metro.Metro;
import com.z0cken.mc.metro.PCS_Metro;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class ProtectionListener implements Listener {

    private static boolean instantiated;

    public ProtectionListener() {
        if(instantiated) throw new IllegalStateException(this.getClass().getName() + " cannot be instantiated twice!");
        instantiated = true;

        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<QuitPlayer, Instant> > iterator = quitPlayers.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<QuitPlayer, Instant> entry = iterator.next();
                    if(Duration.between(Instant.now(), entry.getValue()).toMinutes() > disconnectTimeout) {
                        Arrays.stream(entry.getKey().getInventory()).forEach(is -> entry.getKey().getLocation().getWorld().dropItemNaturally(entry.getKey().getLocation(), is));
                        iterator.remove();
                        //TODO Broadcast
                    }
                }

                quitPlayers.forEach((player, instant) -> {

                });
            }
        }.runTaskTimer(PCS_Metro.getInstance(), 1, 1);
    }

    private Map<QuitPlayer, Instant> quitPlayers = new HashMap<>();
    private int disconnectTimeout = PCS_Metro.getInstance().getConfig().getInt("disconnect.timeout");
    private int disconnectHealth = PCS_Metro.getInstance().getConfig().getInt("disconnect.min-health");

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if(Metro.getInstance().getPlayersInside().contains(player) && !player.hasPermission("pcs.metro.bypass")) {
            event.setCancelled(true);
            //TODO msg
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(Metro.getInstance().contains(event.getPlayer().getLocation())) {
            if(quitPlayers.keySet().stream().noneMatch(quitPlayer -> quitPlayer.getUniqueId().equals(event.getPlayer().getUniqueId()))) {
                event.getPlayer().getInventory().clear();
                //TODO Send message
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        event.getPlayer().teleport(event.getPlayer().getWorld().getSpawnLocation());
                    }
                }.runTaskLater(PCS_Metro.getInstance(), 1);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        if(player.getHealth() < disconnectHealth) {
            player.setHealth(0);
            //TODO Broadcast
        } else quitPlayers.put(new QuitPlayer(player.getUniqueId(), player.getLocation(), player.getInventory().getContents()), Instant.now());
    }

    private static class QuitPlayer {
        private UUID uuid;
        private Location location;
        private ItemStack[] inventory;

        public QuitPlayer(UUID uuid, Location location, ItemStack[] inventory){
            this.uuid = uuid;
            this.location = location;
            this.inventory = inventory;
        }

        public UUID getUniqueId() {
            return uuid;
        }

        public Location getLocation() {
            return location;
        }

        public ItemStack[] getInventory() {
            return inventory;
        }
    }
}
