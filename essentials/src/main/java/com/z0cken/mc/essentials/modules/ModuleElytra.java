package com.z0cken.mc.essentials.modules;

import com.z0cken.mc.essentials.PCS_Essentials;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class ModuleElytra extends Module implements Listener {

    private World end;
    private int radius, checkInterval, searchRadius;

    ModuleElytra(String configPath) {
        super(configPath);
    }

    @Override
    protected void load() {
        radius = getConfig().getInt("fly-radius");
        checkInterval = getConfig().getInt("check-interval");
        searchRadius = getConfig().getInt("search-radius");

        final String worldName = getConfig().getString("end-world");
        if(worldName == null) disable();
        else end = Bukkit.getWorld(worldName);

        if(end == null) disable();
        else {
            tasks.add(new BukkitRunnable() {
                int radiusSquared = (int) Math.pow(radius, 2);
                @Override
                public void run() {
                    for(Player player : end.getPlayers()) {
                        if(!(player.getLocation().distanceSquared(new Location(end, 0, player.getLocation().getY(), 0)) > radiusSquared)) {
                            if(player.isGliding()) {
                                player.removePotionEffect(PotionEffectType.SLOW_FALLING);
                                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, checkInterval + 1, 20, false, false));
                            }
                        }
                    }
                }
            }.runTaskTimer(PCS_Essentials.getInstance(), 5, checkInterval));
        }
    }

    private static Location getSafeLocation(Location origin, int range) {
        for(int x = -range; x <= range; x++) {
            for(int z = -range; z <= range; z++) {
                for(int y = range; y >=-range; y--) {
                    final Block block = origin.clone().add(x, y, z).getBlock();
                    if(block.getType() != Material.AIR && block.getType().isSolid() && block.getRelative(BlockFace.UP).getType() == Material.AIR) return block.getLocation().add(0, 1, 0);
                }
            }
        }
        return null;
    }

    private boolean mayGlide(Player player) {
        return player.getLocation().distanceSquared(new Location(end, 0, 0, 0)) < Math.pow(radius, 2);
    }

    private static void sendBlockTitle(Player player) {
        player.sendTitle(ChatColor.RED + "Stopp!", ChatColor.RED + "Elytren sind hier deaktiviert", 10, 70, 20);
    }

    @EventHandler(ignoreCancelled = true)
    public void onGlide(EntityToggleGlideEvent event) {
        final Player player = (Player) event.getEntity();
        if(!player.getWorld().equals(end)) return;
        if(event.isGliding() && !mayGlide(player)) {

            sendBlockTitle(player);

            Location safeLocation = getSafeLocation(player.getLocation(), searchRadius);
            if(safeLocation != null) {
                safeLocation.setYaw(player.getLocation().getYaw());
                player.teleport(safeLocation);
                event.setCancelled(true);
                player.setGliding(false);
            } else player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20, 1));
        }
    }

    @EventHandler
    public void onFirework(PlayerInteractEvent event) {
        if(!event.getPlayer().getWorld().equals(end)) return;

        if(!mayGlide(event.getPlayer())
                && event.getAction().name().startsWith("R")
                && event.getItem() != null
                && event.getItem().getType() == Material.FIREWORK_ROCKET) {
            event.setUseItemInHand(Event.Result.DENY);
        }
    }

    @EventHandler
    public void onItemSwitch(PlayerItemHeldEvent event) {
        if(!event.getPlayer().getWorld().equals(end)) return;
        final ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
        if(item == null) return;
        Material material = item.getType();
        if(!mayGlide(event.getPlayer()) && material == Material.FIREWORK_ROCKET) sendBlockTitle(event.getPlayer());
    }
}
