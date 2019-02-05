package com.z0cken.mc.essentials.modules;

import com.z0cken.mc.essentials.PCS_Essentials;
import com.z0cken.mc.util.MessageBuilder;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class ModuleSnowball extends Module implements Listener {

    private static boolean instantiated = false;

    private HashMap<Player, Integer> ammo = new HashMap<>();
    private HashMap<Player, Integer> reloading = new HashMap<>();

    private int maxAmmo;
    private int reloadTime;

    public ModuleSnowball(String configPath) {
        super(configPath);
        if(instantiated) throw new IllegalStateException(getClass().getName() + " cannot be instantiated twice!");
        instantiated = true;

        this.load();

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
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        Entity hitEntity = event.getHitEntity();

        if(hitEntity != null && event.getEntity().getType() == EntityType.SNOWBALL) {
            if(hitEntity instanceof LivingEntity && !(hitEntity instanceof Monster)) {
                ((LivingEntity) hitEntity).addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 0));
                if(hitEntity instanceof Player) {
                    MessageBuilder builder = new MessageBuilder().define("PLAYER", ((Player)event.getEntity().getShooter()).getName());
                    ((Player)hitEntity).spigot().sendMessage(ChatMessageType.ACTION_BAR, builder.build(config.getString("messages.hit")));
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if(event.getAction() == Action.LEFT_CLICK_AIR && player.isSneaking() && event.getItem() == null) {
            launchSnowball(player);
        }
    }

    private void launchSnowball(Player player) {
        boolean hasPermission = player.hasPermission("test");
        if(ammo.get(player) > 0 || hasPermission) {

            if(ammo.get(player) == maxAmmo && !hasPermission) {
                reloading.put(player, reloadTime);
            }

            ammo.put(player, ammo.get(player)-1);

            player.launchProjectile(Snowball.class, player.getEyeLocation().getDirection());

            if(!hasPermission) {
                MessageBuilder builder = new MessageBuilder().define("VALUE", Integer.toString(ammo.get(player)));
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, builder.build(config.getString("messages.ammo")));
            }

        } else {
            int remaining = reloading.get(player);
            MessageBuilder builder = new MessageBuilder().define("VALUE", Integer.toString(remaining));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, builder.build(config.getString("messages.wait")));
        }
    }

    private void tick() {
        for(Map.Entry<Player, Integer> entry : reloading.entrySet()) {
            if(entry.getValue() > 0) {
                entry.setValue(entry.getValue() - 1);
            } else if(entry.getValue() == 0) {
                ammo.put(entry.getKey(), maxAmmo);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemSpawn(ItemSpawnEvent event) {
        if(event.getEntity().getItemStack().getType() == Material.SNOWBALL) event.setCancelled(true);
    }

    @Override
    public void load() {
        maxAmmo = config.getInt("maxAmmo");
        reloadTime = config.getInt("reloadTime");
    }
}
