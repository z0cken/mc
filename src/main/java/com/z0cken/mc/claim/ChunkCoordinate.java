package com.z0cken.mc.claim;

import org.bukkit.Chunk;
import org.bukkit.Location;

public class ChunkCoordinate {

    private final int x;
    private final int z;

    public ChunkCoordinate(Chunk chunk) {
        this(chunk.getX(), chunk.getZ());
    }

    public ChunkCoordinate(int var0, int var1) {
        this.x = var0;
        this.z = var1;
    }

    public ChunkCoordinate(Location location) {
        this.x = (int) location.getX() >> 4;
        this.z = (int) location.getZ() >> 4;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public int hashCode() {
        int var0 = 1664525 * this.x + 1013904223;
        int var1 = 1664525 * (this.z ^ -559038737) + 1013904223;
        return var0 ^ var1;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof ChunkCoordinate)) {
            return false;
        } else {
            ChunkCoordinate var1 = (ChunkCoordinate)obj;
            return this.x == var1.x && this.z == var1.z;
        }
    }
}
