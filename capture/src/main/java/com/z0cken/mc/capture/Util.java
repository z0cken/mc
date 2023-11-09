package com.z0cken.mc.capture;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Util {

    private Util() {}

    static void pulse(Item item, Particle.DustOptions dustOptions) {
        final int interval = 1;
        final int rings = 16;
        final int ringParticles = 48;
        final double spacing = 0.1;

        new BukkitRunnable() {
            int i = 0;

            Location loc;
            double t = 0.5;
            public void run() {
                if(!item.isValid()) {
                    cancel();
                    return;
                }

                loc = item.getLocation();

                t += spacing;
                for (double theta = 0; theta <= 2*Math.PI; theta = theta + Math.PI / ringParticles){
                    double x = t*Math.cos(theta);
                    double z = t*Math.sin(theta);

                    Vector v = new Vector(x, 0, z);

                    loc.add(v);
                    loc.getWorld().spawnParticle(Particle.REDSTONE, loc, interval / 2, 0, 0, 0,1, dustOptions);
                    loc.subtract(v);
                    theta = theta + Math.PI/32;
                }

                if(++i == rings) {
                    i = 0;
                    t = 0.5;
                }
            }

        }.runTaskTimer(PCS_Capture.getInstance(), 0, interval);
    }

    static void sound(Item item) {
        final int interval = 100;
        new BukkitRunnable() {
            @Override
            public void run() {
                if(!item.isValid()) {
                    cancel();
                    return;
                }

                item.getWorld().playSound(item.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 3, 1);
            }
        }.runTaskTimer(PCS_Capture.getInstance(), 0, interval);
    }

    static void firework(Item item, Color color) {
        final int interval = 20 * PCS_Capture.getInstance().getConfig().getInt("firework-interval");
        new BukkitRunnable() {
            @Override
            public void run() {
                if(!item.isValid()) {
                    cancel();
                    return;
                }

                Firework fw = (Firework) item.getWorld().spawnEntity(item.getWorld().getHighestBlockAt(item.getLocation().getBlockX(), item.getLocation().getBlockZ()).getLocation().add(0, 20, 0), EntityType.FIREWORK);
                FireworkMeta fwm = fw.getFireworkMeta();
                fwm.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(color).trail(true).build());
                fw.setFireworkMeta(fwm);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        fw.detonate();
                    }
                }.runTaskLater(PCS_Capture.getInstance(), 1);
            }
        }.runTaskTimer(PCS_Capture.getInstance(), 0, interval);
    }

    static void resetPlayer(Player player) {
        player.getInventory().clear();
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        player.getActivePotionEffects().stream().map(PotionEffect::getType).forEach(player::removePotionEffect);
    }

}
