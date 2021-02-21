package com.z0cken.mc.claim;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.Objects;

public class ChunkPosition {
    private final World world;
    private final int x, z;

    public ChunkPosition(World world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public ChunkPosition(Chunk chunk) {
        this.world = chunk.getWorld();
        this.x = chunk.getX();
        this.z = chunk.getZ();
    }

    public World getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkPosition that = (ChunkPosition) o;
        return x == that.x && z == that.z && world.equals(that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, z);
    }
}
