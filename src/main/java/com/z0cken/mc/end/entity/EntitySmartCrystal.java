package com.z0cken.mc.end.entity;

import com.mojang.datafixers.types.Type;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R2.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

public class EntitySmartCrystal extends Entity {

    private static EntityTypes TYPE = inject();

    private static final DataWatcherObject<Optional<BlockPosition>> b;
    private static final DataWatcherObject<Boolean> c;

    static {
        b = DataWatcher.a(EntityEnderCrystal.class, DataWatcherRegistry.m);
        c = DataWatcher.a(EntityEnderCrystal.class, DataWatcherRegistry.i);
    }

    private EntitySmartCrystal(World world) {
        super(TYPE, world);
        this.j = true;
        this.setSize(2.0F, 2.0F);
    }

    public EntitySmartCrystal(Location location) {
        this(((CraftWorld) location.getWorld()).getHandle());
        this.setPosition(location.getX(), location.getY(), location.getZ());
    }

    protected boolean playStepSound() {
        return false;
    }

    protected void x_() {
        this.getDataWatcher().register(b, Optional.empty());
        this.getDataWatcher().register(c, true);
    }

    public void tick() {
        this.lastX = this.locX;
        this.lastY = this.locY;
        this.lastZ = this.locZ;
        if (!this.world.isClientSide) {
            BlockPosition blockposition = new BlockPosition(this);
            if (this.world.worldProvider instanceof WorldProviderTheEnd && this.world.getType(blockposition).isAir() && !CraftEventFactory.callBlockIgniteEvent(this.world, blockposition, this).isCancelled()) {
                this.world.setTypeUpdate(blockposition, Blocks.FIRE.getBlockData());
            }
        }

    }

    protected void b(NBTTagCompound nbttagcompound) {
        if (this.getBeamTarget() != null) {
            nbttagcompound.set("BeamTarget", GameProfileSerializer.a(this.getBeamTarget()));
        }

        nbttagcompound.setBoolean("ShowBottom", this.isShowingBottom());
    }

    protected void a(NBTTagCompound nbttagcompound) {
        if (nbttagcompound.hasKeyOfType("BeamTarget", 10)) {
            this.setBeamTarget(GameProfileSerializer.c(nbttagcompound.getCompound("BeamTarget")));
        }

        if (nbttagcompound.hasKeyOfType("ShowBottom", 1)) {
            this.setShowingBottom(nbttagcompound.getBoolean("ShowBottom"));
        }

    }

    public boolean isInteractable() {
        return true;
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        if (this.isInvulnerable(damagesource)) {
            return false;
        } else if (damagesource.getEntity() instanceof EntityEnderDragon) {
            return false;
        } else {
            if (!this.dead && !this.world.isClientSide) {
                if (CraftEventFactory.handleNonLivingEntityDamageEvent(this, damagesource, (double)f)) {
                    return false;
                }

                this.die();
                if (!this.world.isClientSide) {
                    if (!damagesource.isExplosion()) {
                        ExplosionPrimeEvent event = new ExplosionPrimeEvent(this.getBukkitEntity(), 6.0F, true);
                        this.world.getServer().getPluginManager().callEvent(event);
                        if (event.isCancelled()) {
                            this.dead = false;
                            return false;
                        }

                        this.world.explode(this, this.locX, this.locY, this.locZ, event.getRadius(), event.getFire());
                    }

                }
            }

            return true;
        }
    }

    public void killEntity() {
        super.killEntity();
    }

    public void setBeamTarget(@Nullable BlockPosition blockposition) {
        this.getDataWatcher().set(b, Optional.ofNullable(blockposition));
    }

    @Nullable
    public BlockPosition getBeamTarget() {
        return (BlockPosition)((Optional)this.getDataWatcher().get(b)).orElse(null);
    }

    public void setShowingBottom(boolean flag) {
        this.getDataWatcher().set(c, flag);
    }

    public boolean isShowingBottom() {
        return this.getDataWatcher().get(c);
    }

    @Override
    public CraftEntity getBukkitEntity() {
        if (this.bukkitEntity == null) this.bukkitEntity = new CraftSmartCrystal(world.getServer(), this);
        return this.bukkitEntity;
    }

    public void spawn(CreatureSpawnEvent.SpawnReason reason) {
        world.addEntity(this, reason);
    }

    private static EntityTypes inject() {

        // get the server's datatypes (also referred to as "data fixers" by some)
        // I still don't know where 15190 came from exactly, when a few of us
        // put our heads together that's the number someone else came up with
        Map<Object, Type<?>> dataTypes = (Map<Object, Type<?>>) DataConverterRegistry.a().getSchema(15190).findChoiceType(DataConverterTypes.n).types();
        // inject the new custom entity (this registers the
        // name/id with the server so you can use it in things
        // like the vanilla /summon command)
        dataTypes.put("minecraft:" + "smart_crystal", dataTypes.get("minecraft:" + "ender_crystal"));
        // create and return an EntityTypes for the custom entity
        // store this somewhere so you can reference it later (like for spawning)
        return EntityTypes.a("smart_crystal", EntityTypes.a.a(EntitySmartCrystal.class, EntitySmartCrystal::new));
    }

}
