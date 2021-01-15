package com.z0cken.mc.end.entity;

import net.minecraft.server.v1_13_R2.BlockPosition;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;

public class CraftSmartCrystal extends CraftEntity implements EnderCrystal {

    public CraftSmartCrystal(CraftServer server, EntitySmartCrystal entity) {
        super(server, entity);
    }

    public boolean isShowingBottom() {
        return this.getHandle().isShowingBottom();
    }

    public void setShowingBottom(boolean showing) {
        this.getHandle().setShowingBottom(showing);
    }

    public Location getBeamTarget() {
        BlockPosition pos = this.getHandle().getBeamTarget();

        return pos == null ? null : new Location(this.getWorld(), (double) pos.getX(), (double) pos.getY(), (double) pos.getZ());
    }

    public void setBeamTarget(Location location) {
        if (location == null) {
            this.getHandle().setBeamTarget(null);
        } else {
            if (location.getWorld() != this.getWorld()) {
                throw new IllegalArgumentException("Cannot set beam target location to different world");
            }

            this.getHandle().setBeamTarget(new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        }

    }

    public EntitySmartCrystal getHandle() {
        return (EntitySmartCrystal) this.entity;
    }

    public String toString() {
        return "CraftSmartCrystal";
    }

    public EntityType getType() {
        return EntityType.ENDER_CRYSTAL;
    }

}
