package com.z0cken.mc.raid.runnable;

import com.z0cken.mc.raid.PCS_Raid;
import com.z0cken.mc.raid.Util;
import com.z0cken.mc.raid.event.EndermanCaptureEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Enderman;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.Iterator;

public class EndermanCaptureRunnable extends BukkitRunnable {
    private final Collection<Enderman> endermen;
    private final Location captureLocation = PCS_Raid.getInstance().getConfig().getSerializable("capture.location", Location.class);
    private final double captureRadius = PCS_Raid.getInstance().getConfig().getDouble("capture.radius");

    public EndermanCaptureRunnable(Collection<Enderman> endermen) {
        this.endermen = endermen;
    }

    @Override
    public void run() {
        Iterator<Enderman> it = endermen.iterator();
        while (it.hasNext()) {
            Enderman enderman = it.next();
            if(captureLocation.getWorld() != enderman.getWorld()) return;
            if(Util.getHorizontalDistanceSq(enderman.getLocation(), captureLocation) < Math.pow(captureRadius, 2)) {
                Bukkit.getPluginManager().callEvent(new EndermanCaptureEvent(enderman));
                it.remove();
            }
        }
    }

}
