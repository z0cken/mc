package com.z0cken.mc.Revive.utils;

import net.minecraft.server.v1_13_R2.AxisAlignedBB;
import net.minecraft.server.v1_13_R2.EntityLiving;
import net.minecraft.server.v1_13_R2.IEntitySelector;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.function.Predicate;

public class TargetUtils {

    public static final Predicate<net.minecraft.server.v1_13_R2.Entity> targetPredicate = IEntitySelector.f.and(IEntitySelector.a.and(net.minecraft.server.v1_13_R2.Entity::isInteractable));

    public static LivingEntity getTargetEntity(LivingEntity entity) {
       return getTargetEntity(entity, 5);
    }

    public static LivingEntity getTargetEntity(LivingEntity entity, double rangeToGo) {
        double steps = .2;
        double boxSize = .2;

        Location location = entity.getEyeLocation();

        //Define hitbox
        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(location.getX(), location.getY(), location.getZ(), location.getX() + boxSize, location.getY() + boxSize, location.getZ() + boxSize);

        while(rangeToGo > 0) {
            //Search for stuff inside the hitbox
            List<net.minecraft.server.v1_13_R2.Entity> list = ((CraftWorld)location.getWorld()).getHandle().getEntities(((CraftEntity)entity).getHandle(), axisAlignedBB.d(steps, steps, steps), targetPredicate);

            for(net.minecraft.server.v1_13_R2.Entity entity1 : list) {
                if(entity1 instanceof EntityLiving) {
                    return (LivingEntity) entity1.getBukkitEntity();
                }
            }

            rangeToGo--;
        }

        //Alternative. Try later
        /*double dot = Integer.MIN_VALUE;
        LivingEntity currentTarget = null;
        for(Entity nearbyEntity : entity.getNearbyEntities(rangeToGo, rangeToGo, rangeToGo)) {
            if(nearbyEntity instanceof LivingEntity) {
                Location sourceEye = entity.getEyeLocation();
                Location targetEye = ((LivingEntity) nearbyEntity).getEyeLocation();

                if(sourceEye.distanceSquared(targetEye) > rangeToGo * rangeToGo) {
                    continue;
                }

                Vector toEntity = targetEye.toVector().subtract(sourceEye.toVector());
                double acc = toEntity.normalize().dot(sourceEye.getDirection());
                if(acc > 0.98D && acc > dot) {
                    currentTarget = (LivingEntity) nearbyEntity;
                    dot = acc;
                }
            }
        }

        return currentTarget;*/

        return null;
    }
}
