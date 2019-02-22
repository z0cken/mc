package com.z0cken.mc.claim;

import org.bukkit.*;
import org.bukkit.block.Block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Claim {
    private final OfflinePlayer owner;
    private Location baseLocation;
    private Material baseMaterial;
    private ChunkCoordinate chunkCoordinate;

    Claim(@Nullable OfflinePlayer owner, @Nonnull Block baseBlock) {
        this.baseLocation = baseBlock.getLocation();
        this.owner = owner;
        this.baseMaterial = baseBlock.getType();
        this.chunkCoordinate = new ChunkCoordinate(baseBlock.getChunk());
    }

    Claim(@Nonnull OfflinePlayer owner, @Nonnull Location baseLocation, @Nonnull Material baseMaterial) {
        this.owner = owner;
        this.baseLocation = baseLocation;
        this.baseMaterial = baseMaterial;
        this.chunkCoordinate = new ChunkCoordinate((int) baseLocation.getX() >> 4, (int) baseLocation.getZ() >> 4);
    }

    public OfflinePlayer getOwner() {
        return owner;
    }

    public Chunk getChunk() {
        return baseLocation.getChunk();
    }

    public Block getBaseBlock() {
        return baseLocation.getBlock();
    }

    public Material getBaseMaterial() {
        return baseMaterial;
    }

    public ChunkCoordinate getChunkCoordinate() {
        return chunkCoordinate;
    }

    public World getWorld() {
        return baseLocation.getWorld();
    }

    public void updateBaseMaterial() {
        baseMaterial = getBaseBlock().getType();
        DatabaseHelper.updateMaterial(this, getBaseMaterial());
    }
}
