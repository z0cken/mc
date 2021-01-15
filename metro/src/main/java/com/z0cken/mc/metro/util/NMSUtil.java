package com.z0cken.mc.metro.util;

import net.minecraft.server.v1_16_R3.WorldMap;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.map.MapView;

import java.util.Set;
import java.util.stream.Collectors;

public class NMSUtil {

    private NMSUtil() {}

    public static Set<MapView> getLoadedMaps(World world) {
        return ((CraftWorld)world).getHandle().getMinecraftServer().getWorldServer(net.minecraft.server.v1_16_R3.World.OVERWORLD).getWorldPersistentData().data.values().stream().filter(pb -> pb instanceof WorldMap).map(pb -> ((WorldMap)pb).mapView).collect(Collectors.toSet());
    }

    public static MapView getMapIfLoaded(World world, short id) {
        WorldMap worldMap = (WorldMap) ((CraftWorld)world).getHandle().getMinecraftServer().getWorldServer(net.minecraft.server.v1_16_R3.World.OVERWORLD).getWorldPersistentData().data.get("map_" + id);
        return worldMap == null ? null : worldMap.mapView;
    }

}
