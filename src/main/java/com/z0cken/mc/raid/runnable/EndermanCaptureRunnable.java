package com.z0cken.mc.raid.runnable;

import com.z0cken.mc.raid.PCS_Raid;
import com.z0cken.mc.raid.Util;
import com.z0cken.mc.raid.event.EndermanCaptureEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class EndermanCaptureRunnable extends BukkitRunnable {
    private transient Map<Enderman, Player> endermen;
    private final double radius;
    private final Location location;

    public EndermanCaptureRunnable(Location location, double radius) {
        this.location = location;
        this.radius = radius;
    }

    @Override
    public void run() {
        if(endermen == null) endermen = PCS_Raid.getRaid().endermen;
        Iterator<Enderman> it = endermen.keySet().iterator();
        while (it.hasNext()) {
            Enderman enderman = it.next();
            if(location.getWorld() != enderman.getWorld()) return;
            if(Util.getHorizontalDistanceSq(enderman.getLocation(), location) < Math.pow(radius, 2)) {
                Bukkit.getPluginManager().callEvent(new EndermanCaptureEvent(enderman));
                it.remove();
            }
        }
    }

}
