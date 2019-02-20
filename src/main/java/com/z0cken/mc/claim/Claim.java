package com.z0cken.mc.claim;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;

import javax.annotation.Nonnull;

public class Claim {
    private final Chunk chunk;
    private final OfflinePlayer player;
    private Location baseLocation;
    private Material baseMaterial;

    Claim(OfflinePlayer player, @Nonnull Block baseBlock) {
        this.chunk = baseLocation.getChunk();
        this.player = player;
        this.baseLocation = baseBlock.getLocation();
        this.baseMaterial = baseBlock.getType();
    }

    Claim(OfflinePlayer player, @Nonnull Location baseLocation) {
        this.chunk = baseLocation.getChunk();
        this.player = player;
        this.baseLocation = baseLocation;
        this.baseMaterial = baseLocation.getBlock().getType();
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public Block getBaseBlock() {
        return baseLocation.getBlock();
    }

    public Material getBaseMaterial() {
        if(baseMaterial == null || chunk.isLoaded()) baseMaterial = getBaseBlock().getType();
        return baseMaterial;
    }

    @Override
    public int hashCode() {
        return chunk.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return chunk.equals(obj);
    }
}
