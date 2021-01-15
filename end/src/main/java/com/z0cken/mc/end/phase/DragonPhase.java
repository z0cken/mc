package com.z0cken.mc.end.phase;

import com.z0cken.mc.end.CrystalDefenseRunnable;
import com.z0cken.mc.end.End;
import com.z0cken.mc.end.PCS_End;
import com.z0cken.mc.end.egg.MagicEggType;
import com.z0cken.mc.progression.PCS_Progression;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.server.v1_16_R3.EnderDragonBattle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class DragonPhase extends EndPhase implements Listener {

    private EnderDragon dragon;
    private Map<Location, EnderCrystal> crystals = new HashMap<>();
    private Map<Player, Double> damage = new HashMap<>();

    DragonPhase(End end) {
        super(PhaseType.DRAGON, end);
    }

    @Override
    public void start() {
        clearDragons();
        getEnd().rollback();

        final EnderDragonBattle dragonBattle = ((CraftWorld) getEnd().getWorld()).getHandle().getDragonBattle();

        //Respawn dragon
        try {
            Method m = EnderDragonBattle.class.getDeclaredMethod("a", List.class);
            m.setAccessible(true);
            m.invoke(dragonBattle, Collections.emptyList());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        Bukkit.getPluginManager().registerEvents(this, PCS_End.getInstance());
    }

    @Override
    public void stop() {
        super.stop();
        crystals.values().forEach(Entity::remove);
        clearDragons();
        HandlerList.unregisterAll(this);
    }

    private void clearDragons() {
        getEnd().getWorld().getEntitiesByClass(EnderDragon.class).forEach(Entity::remove);
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {
        if(dragon == null && event.getEntityType() == EntityType.ENDER_DRAGON && event.getLocation().getWorld().equals(getEnd().getWorld())) {
            dragon = (EnderDragon) event.getEntity();
            dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(getEnd().getConfig().getInt("dragon-health"));
            getEnd().getWorld().getEntitiesByClass(EnderCrystal.class).forEach(c -> crystals.put(c.getLocation(), c));
            final FileConfiguration config = PCS_End.getInstance().getConfig();
            tasks.add(new CrystalDefenseRunnable(getEnd(), crystals.values(), config.getInt("crystal.warn-range"), config.getInt("crystal.warn-damage"), config.getInt("crystal.kill-range")).runTaskTimer(PCS_End.getInstance(), 0, 2));
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if(event.getEntityType() != EntityType.ENDER_DRAGON || !event.getEntity().equals(dragon)) return;
        double sum = damage.values().stream().mapToDouble(Double::doubleValue).sum();
        PCS_End.getInstance().getLogger().info(String.format("Dragon died - dropping %d XP", event.getDroppedExp()));

        damage.forEach((p, d) -> PCS_Progression.progress(p, "end_damage", d.intValue()));

        Optional<Map.Entry<Player, Double>> mvp = damage.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue));

        mvp.ifPresent(entry -> {
            entry.getKey().getInventory().addItem(MagicEggType.values()[(new Random()).nextInt(MagicEggType.values().length)].getItemStack());
            getEnd().getWorld().getPlayers().forEach(p -> p.spigot().sendMessage(PCS_End.getInstance().getMessageBuilder().build("{PREFIX} Bester Spieler: " + entry.getKey().getName() + " (" + entry.getValue().intValue() + " Schadenspunkte) ")));
        });

        //Give XP relative to damage
        double d = sum * 0.8D;
        damage.replaceAll((player, aDouble) -> {
            double award = (aDouble / d) * event.getDroppedExp();
            player.giveExp((int) award);
            //TODO Msg
            player.spigot().sendMessage(PCS_End.getInstance().getMessageBuilder().build("{PREFIX} Dein Schadensanteil beträgt" + award + " Prozent"));
            return award;
        });

        event.setDroppedExp(0);

        int time = 30;
        BaseComponent[] msg = PCS_End.getInstance().getMessageBuilder().define("VALUE", Integer.toString(time)).build(PCS_End.getInstance().getConfig().getString("messages.pvp-warn"));
        getEnd().getWorld().getPlayers().forEach(p -> p.spigot().sendMessage(msg));
        new BukkitRunnable() {
            @Override
            public void run() {
                runNextPhase();
            }
        }.runTaskLater(PCS_End.getInstance(), time * 20);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDragonDamage(EntityDamageByEntityEvent event) {
        if((event.getEntityType() != EntityType.ENDER_DRAGON && event.getEntityType() != EntityType.ENDER_CRYSTAL) || !event.getEntity().equals(dragon)) return;
        Player player;

        if(event.getDamager().getType() == EntityType.PLAYER) {
            player = (Player) event.getDamager();
        } else if(event.getDamager() instanceof Projectile) {
            ProjectileSource source = ((Projectile)event.getEntity()).getShooter();
            if(source instanceof Player) player = (Player) source;
            else return;
        } else return;

        if(event.getEntityType() == EntityType.ENDER_DRAGON) {
            double d = damage.getOrDefault(player, 0D);
            damage.put(player, d + event.getFinalDamage());
        } else {
            if(player.isGliding()) event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCrystalKill(EntityDamageByEntityEvent event) {
        if(crystals.containsValue(event.getEntity()) && event.getDamager().getType() == EntityType.PLAYER) {
            Player killer = (Player) event.getDamager();
            if(killer != null) PCS_Progression.progress(killer, "end_crystals", 1);
        }
    }
}
