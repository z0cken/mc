package com.z0cken.mc.essentials.modules;

import com.z0cken.mc.core.persona.Persona;
import com.z0cken.mc.core.persona.PersonaAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalSelector;
import net.citizensnpcs.api.event.NPCDamageByBlockEvent;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCDamageEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ModuleMinion extends Module implements Listener {

    private Map<Player, NPC> minions = new HashMap<>();
    private static double distanceMargin, distractionChance, maxDamage;
    private static int guardTeleport, guardSpawn;
    private static boolean ignoreProtection;

    ModuleMinion(String configPath) {
        super(configPath);
    }

    @Override
    protected void load() {
        distanceMargin = getConfig().getDouble("distance-margin");
        distractionChance = getConfig().getDouble("distraction-chance");
        ignoreProtection = getConfig().getBoolean("ignore-protection");
        maxDamage = getConfig().getDouble("max-damage");
        guardTeleport = getConfig().getInt("guard-teleport");
        guardSpawn = getConfig().getInt("guard-spawn");
    }

    @Override
    protected void onDisable() {
        minions.values().forEach(NPC::destroy);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.VILLAGER, player.getName());
        npc.addTrait(new MinionTrait(player));
        npc.setProtected(false);
        if(player.getGameMode() == GameMode.SURVIVAL) npc.spawn(player.getLocation());

        minions.put(player, npc);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final NPC npc = minions.get(event.getPlayer());
        npc.destroy();
        CitizensAPI.getNPCRegistry().deregister(npc);
        minions.remove(event.getPlayer());
    }

    @EventHandler
    public void onGamemode(PlayerGameModeChangeEvent event) {
        final Player player = event.getPlayer();

        if(event.getNewGameMode() == GameMode.SURVIVAL) minions.get(event.getPlayer()).spawn(player.getLocation());
        else minions.get(event.getPlayer()).despawn();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        minions.get(event.getEntity()).despawn();
        final EntityDamageEvent lastDamageCause = event.getEntity().getLastDamageCause();
        if(lastDamageCause instanceof EntityDamageByEntityEvent) {
            final Entity damager = ((EntityDamageByEntityEvent) lastDamageCause).getDamager();
            if(damager instanceof Villager && damager.getName().equals(event.getEntity().getName())) {
                event.setDeathMessage(ChatColor.YELLOW + event.getEntity().getName() + " wurde verbessert");
                try {
                    PersonaAPI.getPersona(event.getEntity().getUniqueId()).awardBadge(Persona.Badge.MINION);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        minions.get(event.getPlayer()).spawn(event.getRespawnLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        final NPC minion = minions.get(event.getPlayer());
        if(!minion.isSpawned()) return;
        minion.teleport(event.getTo(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        minion.getTrait(MinionTrait.class).guard(guardTeleport);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if(event.getTarget() instanceof Player && (new Random()).nextDouble() < distractionChance) {
            event.setTarget(minions.get(event.getTarget()).getEntity());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if(!CitizensAPI.getNPCRegistry().isNPC(event.getDamager())) return;
        MinionTrait minion = CitizensAPI.getNPCRegistry().getNPC(event.getDamager()).getTrait(MinionTrait.class);
        minion.getOwner().setHealth(minion.getOwner().getHealth() - event.getDamage());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNpcDamage(NPCDamageEvent event) {
        MinionTrait minion = event.getNPC().getTrait(MinionTrait.class);
        if(event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
            minion.getNPC().getEntity().teleport(minion.getOwner());
            return;
        }

        if(minion != null) {
            event.setDamage(0);

            if(event.getCause() == EntityDamageEvent.DamageCause.MAGIC) return;
            else if(event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
                event.setCancelled(true);
                event.getNPC().getEntity().teleport(minion.getOwner());
                return;
            }

            if (!ignoreProtection) {

                final EntityDamageEvent testEvent = new EntityDamageEvent(minion.getOwner(), event.getCause(), event.getDamage());
                Bukkit.getPluginManager().callEvent(testEvent);

                if (testEvent.isCancelled()) {
                    event.setCancelled(true);
                    return;
                }
            }

            minion.handleDamage(event.getDamage());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNpcDamageByEntity(NPCDamageByEntityEvent event) {
        MinionTrait minion = event.getNPC().getTrait(MinionTrait.class);
        if(minion != null) {

            if(!ignoreProtection) {

                final EntityDamageByEntityEvent testEvent = new EntityDamageByEntityEvent(event.getDamager(), minion.getOwner(), event.getCause(), event.getDamage());
                Bukkit.getPluginManager().callEvent(testEvent);

                if(testEvent.isCancelled()) {
                    event.setCancelled(true);
                    return;
                }
            }

            minion.handleDamage(event.getDamage());
            event.setDamage(0);

        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNpcDamageByBlock(NPCDamageByBlockEvent event) {
        MinionTrait minion = event.getNPC().getTrait(MinionTrait.class);
        if(minion != null) {
            if (!ignoreProtection) {

                final EntityDamageEvent testEvent = new EntityDamageEvent(minion.getOwner(), event.getCause(), event.getDamage());
                Bukkit.getPluginManager().callEvent(testEvent);

                if (testEvent.isCancelled()) {
                    event.setCancelled(true);
                    return;
                }
            }

            minion.handleDamage(event.getDamage());
            event.setDamage(0);
        }
    }

    static class MinionTrait extends Trait {
        private Player owner;
        private int guarded;

        MinionTrait(Player owner) {
            super("minion");
            this.owner = owner;
        }

        @Override
        public void onAttach() {
            getNPC().getDefaultGoalController().addGoal(new StareGoal(), 3);
            getNPC().getDefaultGoalController().addGoal(new FollowGoal(), 2);
        }

        @Override
        public void onSpawn() {
            Villager v = (Villager) npc.getEntity();
            v.setProfession(Villager.Profession.NITWIT);
            v.setCareer(Villager.Career.NITWIT);
            v.setBaby();
            v.setAgeLock(true);

            v.setCustomName(getOwner().getName());
            v.setCustomNameVisible(true);
            v.setCanPickupItems(false);

            guard(guardSpawn);
        }

        int reAttach = 120 * 20;

        @Override
        public void run() {
            if(!getNPC().isSpawned()) return;
            if(guarded > 0) guarded--;
            syncPotionEffects();
            getOwner().setFireTicks(Math.max(getNPC().getEntity().getFireTicks(), getOwner().getFireTicks()));

            if(reAttach-- == 0) {
                onAttach();
                reAttach = 120 * 20;
            }
        }

        Player getOwner() {
            return owner;
        }

        public void guard(int ticks) {
            guarded = Math.max(guarded, ticks);
        }

        private void handleDamage(double damage) {
            if(guarded > 0) return;
            damage = Math.min(damage, maxDamage);
            getOwner().damage(damage, getNPC().getEntity());
            /*
            new BukkitRunnable() {
                @Override
                public void run() {
                    EntityLiving minion = (EntityLiving) ((CraftEntity)getNPC().getEntity()).getHandle();
                    ((CraftEntity)getOwner()).getHandle().damageEntity(minion.cr(), (float) damage);
                }
            }.runTaskLater(PCS_Essentials.getInstance(), 1);*/
        }

        private void syncPotionEffects() {
            LivingEntity entity = (LivingEntity) getNPC().getEntity();
            entity.getActivePotionEffects().stream().filter(potionEffect -> !getOwner().hasPotionEffect(potionEffect.getType())).forEach(potionEffect -> entity.removePotionEffect(potionEffect.getType()));
            //getOwner().getActivePotionEffects().stream().filter(potionEffect -> !entity.hasPotionEffect(potionEffect.getType())).forEach(entity::addPotionEffect);
        }

        private void syncAttributes() {
            LivingEntity entity = (LivingEntity) getNPC().getEntity();
            for(Attribute attribute : Attribute.values()) {
                entity.getAttribute(attribute).setBaseValue(getOwner().getAttribute(attribute).getBaseValue());
                entity.getAttribute(attribute).getModifiers().forEach(getOwner().getAttribute(attribute)::removeModifier);
                getOwner().getAttribute(attribute).getModifiers().forEach(getOwner().getAttribute(attribute)::addModifier);
            }
        }

        class FollowGoal implements Goal {

            @Override
            public void reset() { }

            @Override
            public void run(GoalSelector goalSelector) {
                if(!shouldExecute(goalSelector)) {
                    goalSelector.finish();
                    return;
                }

                if(!getNPC().getNavigator().isNavigating()) {
                    getNPC().getNavigator().setTarget(owner, false);
                    getNPC().getNavigator().getLocalParameters().distanceMargin(distanceMargin);
                }
            }

            @Override
            public boolean shouldExecute(GoalSelector goalSelector) {
                return !getOwner().getWorld().equals(getNPC().getEntity().getWorld()) || getOwner().getLocation().distanceSquared(getNPC().getEntity().getLocation()) >= distanceMargin * distanceMargin;
            }
        }

        class StareGoal implements Goal {

            @Override
            public void reset() { }

            @Override
            public void run(GoalSelector goalSelector) {
                Util.faceEntity(getNPC().getEntity(), getOwner());
                goalSelector.finish();
            }

            @Override
            public boolean shouldExecute(GoalSelector goalSelector) {
                return true;
            }
        }
    }

}
