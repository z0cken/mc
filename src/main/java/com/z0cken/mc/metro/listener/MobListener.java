package com.z0cken.mc.metro.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.z0cken.mc.metro.Metro;
import com.z0cken.mc.metro.PCS_Metro;
import com.z0cken.mc.metro.Station;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.loot.Lootable;

public class MobListener implements Listener {

    private static boolean instantiated;

    public MobListener() {
        if(instantiated) throw new IllegalStateException(this.getClass().getName() + " cannot be instantiated twice!");
        instantiated = true;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        if(entity instanceof Lootable && Metro.getInstance().contains(event.getLocation())) {
            //TODO Loot Table & Metro Tag
            entity.getScoreboardTags().add("metro");
            ((Lootable)entity).setLootTable(Bukkit.getLootTable(new NamespacedKey("metro", "entities/" + event.getEntityType().name().toLowerCase() + "/" + getDifficulty(entity.getLocation()).name().toLowerCase())));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTarget(EntityTargetEvent event) {
        if(event.getTarget() == null || !(event.getEntity() instanceof Monster)) return;
        Monster monster = (Monster) event.getEntity();

        if(monster.getScoreboardTags().contains("metro")) {
            if(Metro.getInstance().getStations().stream().filter(Station::isActive).anyMatch(s -> s.contains(event.getTarget().getLocation()))) event.setCancelled(true);
        }
    }

    private Difficulty getDifficulty(Location location) {
        Difficulty difficulty = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().queryValue(BukkitAdapter.adapt(location), null, PCS_Metro.DIFFICULTY_FLAG);
        if(difficulty == null) return Difficulty.NORMAL;
        return difficulty;
    }
}