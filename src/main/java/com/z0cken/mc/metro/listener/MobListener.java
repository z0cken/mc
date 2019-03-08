package com.z0cken.mc.metro.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.z0cken.mc.metro.Metro;
import com.z0cken.mc.metro.PCS_Metro;
import com.z0cken.mc.metro.SpawnProfile;
import com.z0cken.mc.metro.Station;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class MobListener implements Listener {

    private static boolean instantiated;

    public MobListener() {
        if(instantiated) throw new IllegalStateException(this.getClass().getName() + " cannot be instantiated twice!");
        instantiated = true;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        if(event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM && Metro.getInstance().contains(event.getLocation())) {
            entity.getScoreboardTags().add("metro");

            String flag = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(event.getLocation())).queryValue(null, PCS_Metro.STRING_FLAG);
            if(flag != null) {
                SpawnProfile profile = Metro.getInstance().getProfile(flag);
                if(profile != null) {
                    if(profile.spawnMob(entity.getType(), event.getLocation())) event.setCancelled(true);
                } else PCS_Metro.getInstance().getLogger().warning(String.format("Invalid spawn profile '%s' at %s", flag, event.getLocation().toString()));
            } else PCS_Metro.getInstance().getLogger().warning(String.format("Spawn profile missing for %s", event.getLocation().toString()));

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

    /*private Difficulty getDifficulty(Location location) {
        Difficulty difficulty = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().queryValue(BukkitAdapter.adapt(location), null, PCS_Metro.DIFFICULTY_FLAG);
        if(difficulty == null) return Difficulty.NORMAL;
        return difficulty;
    }*/
}