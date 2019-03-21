package com.z0cken.mc.claim;

import com.z0cken.mc.core.FriendsAPI;
import org.bukkit.*;
import org.bukkit.block.Block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.UUID;

@SuppressWarnings("unused")
public class Claim {
    private final Owner owner;
    private Location baseLocation;
    private Material baseMaterial;
    private ChunkCoordinate chunkCoordinate;

    Claim(@Nullable UUID uuid, @Nonnull Block baseBlock) {
        this.baseLocation = baseBlock.getLocation();
        this.owner = uuid == null ? null : new Owner(uuid);
        this.baseMaterial = baseBlock.getType();
        this.chunkCoordinate = new ChunkCoordinate(baseBlock.getChunk());
    }

    Claim(@Nonnull UUID uuid, @Nonnull Location baseLocation, @Nonnull Material baseMaterial) {
        this.owner = new Owner(uuid);
        this.baseLocation = baseLocation;
        this.baseMaterial = baseMaterial;
        this.chunkCoordinate = new ChunkCoordinate((int) baseLocation.getX() >> 4, (int) baseLocation.getZ() >> 4);
    }

    public Owner getOwner() { return owner; }

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
    }

    public boolean canBuild(@Nonnull OfflinePlayer player) {

        if(owner.isPlayer(player) || player.isOnline() && PCS_Claim.isOverriding(player.getPlayer())) return true;

        try {
            if(FriendsAPI.areFriends(player.getUniqueId(), owner.getUniqueId())) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !(obj instanceof Claim)) {
            return false;
        }
        Claim claim = (Claim) obj;
        return chunkCoordinate.equals(claim.getChunkCoordinate()) && (owner == null ? claim.getOwner() == null : owner.equals(claim.getOwner()));
    }

    @Override
    public int hashCode() {
        return owner.getUniqueId().hashCode() * chunkCoordinate.getX() * chunkCoordinate.getZ();
    }

    protected static class Owner {
        private UUID uuid;

        private Owner(UUID uuid) {
            this.uuid = uuid;
        }

        public UUID getUniqueId() {
            return uuid;
        }

        public OfflinePlayer getOfflinePlayer() {
            return Bukkit.getOfflinePlayer(uuid);
        }

        public String getName() {
            return getOfflinePlayer().getName();
        }

        public boolean isPlayer(OfflinePlayer player) {
            return player.getUniqueId().equals(uuid);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null || !(obj instanceof Owner)) {
                return false;
            }
            final Owner other = (Owner)obj;
            return this.getUniqueId() != null && other.getUniqueId() != null && this.getUniqueId().equals(other.getUniqueId());
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + ((this.getUniqueId() != null) ? this.getUniqueId().hashCode() : 0);
            return hash;
        }
    }
}
