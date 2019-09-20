package com.z0cken.mc.raid.runnable;

import org.bukkit.Location;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

public class EndermenSpawnRunnable extends BukkitRunnable {
    private final Location spawnPoint;
    private final short radius;
    private final int minDelay;

    private transient Enderman lastEnderman;
    private transient int i;

    public EndermenSpawnRunnable(Location spawnPoint, short radius, int minDelay) {
        this.spawnPoint = spawnPoint;
        this.radius = radius;
        this.minDelay = minDelay;
    }

    @Override
    public void run() {
        i++;
        if(i < minDelay) return;
        if(lastEnderman != null && spawnPoint.distance(lastEnderman.getLocation()) < radius) return;

        lastEnderman = (Enderman) spawnPoint.getWorld().spawnEntity(spawnPoint, EntityType.ENDERMAN);
        i = 0;
    }
}
