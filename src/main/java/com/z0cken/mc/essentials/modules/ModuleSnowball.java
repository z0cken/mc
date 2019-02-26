package com.z0cken.mc.essentials.modules;

import com.z0cken.mc.core.util.MessageBuilder;
import com.z0cken.mc.essentials.PCS_Essentials;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleSnowball extends Module implements Listener {

    private HashMap<Player, Integer> ammo = new HashMap<>();
    private HashMap<Player, Integer> reloading = new HashMap<>();
    private HashMap<Player, List<Long>> timer = new HashMap<>();

    private int maxAmmo;
    private int reloadTime;

    public ModuleSnowball(String configPath) {
        super(configPath);

        //TODO Make async
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(PCS_Essentials.getInstance(), 20, 20));

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        ammo.put(event.getPlayer(), maxAmmo);
        timer.put(event.getPlayer(), new ArrayList<>());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        ammo.remove(event.getPlayer());
        reloading.remove(event.getPlayer());
        timer.remove(event.getPlayer());
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        Entity hitEntity = event.getHitEntity();

        if(hitEntity != null && event.getEntity().getType() == EntityType.SNOWBALL) {
            if(hitEntity.equals(event.getEntity().getShooter())) return;
            if(hitEntity instanceof LivingEntity && !(hitEntity instanceof Monster)) {
                if(Math.random() < getConfig().getDouble("heal-chance")) ((LivingEntity) hitEntity).addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 1, 6));
                if(hitEntity instanceof Player) {
                    MessageBuilder builder = new MessageBuilder().define("PLAYER", ((Player)event.getEntity().getShooter()).getName());
                    ((Player)hitEntity).spigot().sendMessage(ChatMessageType.ACTION_BAR, builder.build(getConfig().getString("messages.hit")));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if(event.getDamager().getType() == EntityType.SNOWBALL) event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if(event.getAction() == Action.LEFT_CLICK_AIR && player.isSneaking() && event.getItem() == null) {
            launchSnowball(player);
        }
    }

    private void launchSnowball(Player player) {
        boolean hasPermission = player.hasPermission("pcs.essentials.snowball");
        if(ammo.get(player) > 0 || hasPermission) {

            if(!hasPermission) {

                final List<Long> times = timer.get(player);
                if(ammo.get(player) == maxAmmo) reloading.put(player, reloadTime);

                Long time = System.currentTimeMillis();
                times.add(time);
                if(times.size() >= 5) {
                    Long start = times.get(times.size() - 5);
                    Bukkit.broadcastMessage(time - start + "");
                    if(time - start < getConfig().getInt("kick-threshold")) player.kickPlayer("429 - RATE LIMIT REACHED");
                }

                ammo.put(player, ammo.get(player)-1);
                MessageBuilder builder = new MessageBuilder().define("VALUE", Integer.toString(ammo.get(player)));
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, builder.build(getConfig().getString("messages.ammo")));
            }

            player.launchProjectile(Snowball.class, player.getEyeLocation().getDirection());

        } else {
            int remaining = reloading.get(player);
            MessageBuilder builder = new MessageBuilder().define("VALUE", Integer.toString(remaining));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, builder.build(getConfig().getString("messages.wait")));
        }
    }

    private void tick() {
        for(Map.Entry<Player, Integer> entry : reloading.entrySet()) {
            if(entry.getValue() > 0) {
                entry.setValue(entry.getValue() - 1);
            } else if(entry.getValue() == 0) {
                ammo.put(entry.getKey(), maxAmmo);
                timer.get(entry.getKey()).clear();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemSpawn(ItemSpawnEvent event) {
        if(event.getEntity().getItemStack().getType() == Material.SNOWBALL) event.setCancelled(true);
    }

    @Override
    public void load() {
        maxAmmo = getConfig().getInt("max-ammo");
        reloadTime = getConfig().getInt("reload-time");
    }
}
