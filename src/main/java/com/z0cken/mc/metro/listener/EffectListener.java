package com.z0cken.mc.metro.listener;

import com.z0cken.mc.metro.Metro;
import com.z0cken.mc.metro.MetroEffect;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PigZombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;
import java.util.stream.Collectors;

public class EffectListener implements Listener {

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if(event.getBlock().getType() == Material.NETHER_QUARTZ_ORE && Metro.getInstance().getAppropriateEffect().hasFlag("pigmen")) {
            final int range = MetroEffect.getPigmenRange();
            event.getPlayer().getNearbyEntities(range, range, range).stream().filter(entity -> entity.getType() == EntityType.PIG_ZOMBIE).forEach(entity -> {
                ((PigZombie)entity).setAngry(true);
                ((PigZombie)entity).setTarget(event.getPlayer());
            });
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Set<PotionEffectType> types = Metro.getInstance().getAppropriateEffect().getPotionEffects().stream().map(PotionEffect::getType).collect(Collectors.toSet());
        for(PotionEffect effect : event.getPlayer().getActivePotionEffects()) {
            if(types.contains(effect.getType()) || effect.getDuration() > 1000) event.getPlayer().removePotionEffect(effect.getType());
        }
    }

}
