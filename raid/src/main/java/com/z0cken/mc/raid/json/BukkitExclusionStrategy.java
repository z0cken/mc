package com.z0cken.mc.raid.json;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

public class BukkitExclusionStrategy implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(FieldAttributes fieldattributes) {
        return false;
    }

    @Override
    public boolean shouldSkipClass(Class<?> oclass) {
        if(World.class.isAssignableFrom(oclass)
        || BukkitTask.class.isAssignableFrom(oclass)) return true;
        return false;
    }
}
