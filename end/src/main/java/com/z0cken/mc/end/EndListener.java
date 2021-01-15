package com.z0cken.mc.end;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EndListener implements Listener {

    private final End end;

    public EndListener(End end) {
        this.end = end;
    }

    private static Location getSafeLocation(Location origin, int range) {
        for(int x = -range; x <= range; x++) {
            for(int z = -range; z <= range; z++) {
                for(int y = range; y >=-range; y--) {
                    final Block block = origin.clone().add(x, y, z).getBlock();
                    if(block.getType() != Material.AIR && block.getType().isSolid() && block.getRelative(BlockFace.UP).getType() == Material.AIR) return block.getLocation().add(0.5, 1, 0.5);
                }
            }
        }
        return null;
    }

    @EventHandler(ignoreCancelled = true)
    public void onGlide(EntityToggleGlideEvent event) {
        final Player player = (Player) event.getEntity();
        if(!player.getWorld().equals(end.getWorld()) || event.getEntityType() != EntityType.PLAYER) return;
        if(event.isGliding() && !end.getMainPlayers().contains(player)) {

            player.sendTitle(ChatColor.RED + "Stopp!", ChatColor.RED + "Elytren sind hier deaktiviert", 10, 70, 20);

            Location safeLocation = getSafeLocation(player.getLocation(), 10);
            if(safeLocation != null) {
                safeLocation.setYaw(player.getLocation().getYaw());
                player.teleport(safeLocation);
                event.setCancelled(true);
            } else player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20, 1));
        }
    }

    @EventHandler
    public void onFirework(PlayerInteractEvent event) {
        if(!event.getPlayer().getWorld().equals(end.getWorld())) return;

        if(!end.getMainPlayers().contains(event.getPlayer())
                && event.getAction().name().startsWith("R")
                && event.getItem() != null
                && event.getItem().getType() == Material.FIREWORK_ROCKET) {
            event.setUseItemInHand(Event.Result.DENY);
        }
    }

    @EventHandler
    public void onItemSwitch(PlayerItemHeldEvent event) {
        if(!event.getPlayer().getWorld().equals(end.getWorld())) return;
        final ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
        if(item == null) return;
        Material material = item.getType();
        if(!end.getMainPlayers().contains(event.getPlayer()) && material == Material.FIREWORK_ROCKET) {
            event.getPlayer().sendTitle(ChatColor.RED + "Stopp!", ChatColor.RED + "Elytren sind hier deaktiviert", 10, 70, 20);
        }
    }

    @EventHandler
    public void onElytraFind(PlayerInteractEntityEvent event) {
        if(!event.getPlayer().getWorld().equals(end.getWorld())) return;
        if(event.getRightClicked().getType() == EntityType.ITEM_FRAME) {
            ItemFrame itemFrame = (ItemFrame) event.getRightClicked();
            if(itemFrame.getItem().getType() == Material.ELYTRA) {
                end.getElytraManager().discover(itemFrame);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(!event.getPlayer().getWorld().equals(end.getWorld())) return;
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem() != null && event.getItem().getType() == Material.ITEM_FRAME) {
            event.setUseItemInHand(Event.Result.DENY);
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if(event.getEntityType() != EntityType.PLAYER || !event.getEntity().getWorld().equals(end.getWorld()) || event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        int fallRadius = end.getConfig().getInt("fall-radius");
        if(Math.abs(event.getEntity().getLocation().getX()) < fallRadius && Math.abs(event.getEntity().getLocation().getZ()) < fallRadius) event.setCancelled(true);
    }

    //TODO Why
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageByEntityEvent event) {
        if(event.getEntityType() == EntityType.PLAYER && event.getEntity().getWorld().equals(end.getWorld())) event.setCancelled(false);
    }

    @EventHandler
    public void onEntityTeleport(EntityTeleportEvent event) {
        if(event.getEntityType() == EntityType.ENDER_DRAGON && event.getFrom().getWorld().equals(end.getWorld())) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        final Location to = event.getTo();
        final World endWorld = end.getWorld();

        if(event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL && to.getWorld().equals(endWorld)) event.setTo(endWorld.getSpawnLocation());
        else if(event.getCause() == PlayerTeleportEvent.TeleportCause.END_GATEWAY && !endWorld.getWorldBorder().isInside(to)) endWorld.getWorldBorder().setSize(Math.max(to.getX(), to.getZ()) + 10);
    }

    @EventHandler
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (event.getEntityType() != EntityType.ENDER_DRAGON || !event.getEntity().getWorld().equals(end.getWorld()))
            return;
        if (!end.getMainPlayers().contains(event.getTarget())) event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if(Bukkit.getOnlinePlayers().size() == 0 /* Check Time*/) {
            int limit = ((int) end.getWorld().getWorldBorder().getSize() >> 4) + 16;
            end.getElytraManager().scan(end.getElytraManager().getScanner().sizeLimit(limit).skip(end.getElytraManager().getScannedLayers()));
        }
    }
}
