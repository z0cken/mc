package com.z0cken.mc.raid.runnable;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class SoundSourceRunnable extends BukkitRunnable {

    private final Location location;
    private final String sound;
    private final int interval;
    private final float volume, pitch;

    private int i;

    public SoundSourceRunnable(Location location, String sound, int interval, float volume, float pitch) {
        this.location = location;
        this.sound = sound;
        this.interval = interval;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public void run() {
        if(i-- == 0) {
            i = interval;
            location.getWorld().playSound(location, sound, volume, pitch);
        }
    }
}
