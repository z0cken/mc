package com.z0cken.mc.end;

import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EyeListener implements Listener {

    private static final int DURATION = PCS_End.getInstance().getConfig().getInt("eye.duration"), RANGE = PCS_End.getInstance().getConfig().getInt("eye.range");

    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {

        //TODO Point to current boss mob or player
        if(event.getEntityType() == EntityType.ENDER_SIGNAL) {
            EnderSignal signal = (EnderSignal) event.getEntity();
            signal.setTargetLocation(signal.getLocation().add(0, 5, 0));
            signal.setDespawnTimer(DURATION);
            signal.getNearbyEntities(RANGE, RANGE, RANGE).stream().filter(e -> e instanceof LivingEntity).forEach(e -> ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, DURATION, 0)));
        }

    }

}
