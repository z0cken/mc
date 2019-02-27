package com.z0cken.mc.metro.listener;

import com.z0cken.mc.metro.event.StationActivateEvent;
import com.z0cken.mc.metro.event.StationDeactivateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MetroListener implements Listener {

    private static boolean instantiated;

    public MetroListener() {
        if(instantiated) throw new IllegalStateException(this.getClass().getName() + " cannot be instantiated twice!");
        instantiated = true;
    }

    @EventHandler
    public void onActivate(StationActivateEvent event) {
        //TODO Broadcast message & sound
        //TODO Give XP
    }

    @EventHandler
    public void onDeactivate(StationDeactivateEvent event) {
        //TODO Broadcast message & sound
    }

}
