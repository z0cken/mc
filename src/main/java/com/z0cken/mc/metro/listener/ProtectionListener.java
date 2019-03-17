package com.z0cken.mc.metro.listener;

import com.z0cken.mc.metro.Metro;
import com.z0cken.mc.metro.PCS_Metro;
import org.bukkit.Bukkit;
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

        if(true) return;

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

        if(!player.hasPermission("pcs.metro.bypass") && (Metro.getInstance().getPlayersInside().contains(player) || Metro.getInstance().contains(event.getTo()))) {
            event.setCancelled(true);
            player.spigot().sendMessage(PCS_Metro.getInstance().getMessageBuilder().build(PCS_Metro.getInstance().getConfig().getString("messages.teleport")));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(true) return;
        final Player player = event.getPlayer();
        if(player.hasPermission("pcs.metro.bypass") || !Metro.getInstance().contains(player.getLocation())) return;

        if(quitPlayers.keySet().stream().noneMatch(quitPlayer -> quitPlayer.getUniqueId().equals(player.getUniqueId()))) {
            player.getInventory().clear();
            player.spigot().sendMessage(PCS_Metro.getInstance().getMessageBuilder().define("VALUE", Integer.toString(disconnectTimeout)).build(PCS_Metro.getInstance().getConfig().getString("messages.deathjoin")));
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.spigot().respawn();
                }
            }.runTaskLater(PCS_Metro.getInstance(), 3);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if(true) return;
        final Player player = event.getPlayer();
        if(player.hasPermission("pcs.metro.bypass") || !Metro.getInstance().contains(player.getLocation())) return;
        PCS_Metro.getInstance().getLogger().info(String.format("%s hat sich mit %d HP in der Metro ausgeloggt", player.getName(), (int) player.getHealth()));

        if(player.getHealth() < disconnectHealth) {
            player.setHealth(0);
            Bukkit.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(PCS_Metro.getInstance().getMessageBuilder().define("VALUE", Integer.toString(disconnectHealth)).define("PLAYER", player.getName()).build(PCS_Metro.getInstance().getConfig().getString("messages.deathquit"))));
        } else {
            quitPlayers.put(new QuitPlayer(player.getUniqueId(), player.getLocation(), player.getInventory().getContents()), Instant.now());
        }
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
