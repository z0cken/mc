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
    private ChunkPosition chunkPosition;

    Claim(@Nullable UUID uuid, @Nonnull Block baseBlock) {
        this.baseLocation = baseBlock.getLocation();
        this.owner = uuid == null ? null : new Owner(uuid);
        this.baseMaterial = baseBlock.getType();
        this.chunkPosition = new ChunkPosition(baseBlock.getChunk());
    }

    Claim(@Nonnull UUID uuid, @Nonnull Location baseLocation, @Nonnull Material baseMaterial) {
        this.owner = new Owner(uuid);
        this.baseLocation = baseLocation;
        this.baseMaterial = baseMaterial;
        this.chunkPosition = new ChunkPosition(baseLocation.getWorld(), (int) baseLocation.getX() >> 4, (int) baseLocation.getZ() >> 4);
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

    public void setBaseLocation(Location location) {
        baseLocation = location;
        updateBaseMaterial();
    }

    public Material getBaseMaterial() {
        return baseMaterial;
    }

    public ChunkPosition getChunkPosition() {
        return chunkPosition;
    }

    public World getWorld() {
        return baseLocation.getWorld();
    }

    public String getName() {
        return String.format("[%s | %d | %d]", chunkPosition.getWorld().getEnvironment().name().toLowerCase(), chunkPosition.getX(), chunkPosition.getZ());
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
        if (!(obj instanceof Claim)) {
            return false;
        }
        final Claim claim = (Claim) obj;
        return chunkPosition.equals(claim.getChunkPosition()) && (owner == null ? claim.getOwner() == null : owner.equals(claim.getOwner()));
    }

    @Override
    public int hashCode() {
        return owner.getUniqueId().hashCode() * chunkPosition.getX() * chunkPosition.getZ();
    }

    protected static class Owner {
        private final UUID uuid;

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
            if (!(obj instanceof Owner)) {
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
