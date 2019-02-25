package com.z0cken.mc.claim;

import com.z0cken.mc.core.FriendsAPI;
import org.bukkit.*;
import org.bukkit.block.Block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;

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

    public Location getBaseLocation() {
        return baseLocation;
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

    public String getName() {
        return "[" + chunkCoordinate.getX() + "|" + chunkCoordinate.getZ() + "]";
    }

    public void updateBaseMaterial() {
        baseMaterial = getBaseBlock().getType();
        DatabaseHelper.updateMaterial(this, getBaseMaterial());
        Bukkit.broadcastMessage("UPDATE: " + getBaseMaterial().name() + " : " + getBaseBlock().getLocation().toString());
    }

    public boolean canBuild(@Nonnull OfflinePlayer player) {

        if(player.equals(owner) || player.isOnline() && PCS_Claim.canOverride(player.getPlayer())) return true;

        try {
            if(FriendsAPI.areFriends(player.getUniqueId(), owner.getUniqueId())) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
