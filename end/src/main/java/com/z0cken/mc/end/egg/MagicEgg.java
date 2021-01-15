package com.z0cken.mc.end.egg;

import com.z0cken.mc.end.PCS_End;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class MagicEgg implements Listener {

    private static final Set<MagicEgg> eggs = new HashSet<>();

    private final Player owner;
    private final Block egg;
    private final BossBar bossBar;
    private final AreaEffectCloud cloud;
    private final NamespacedKey barKey;

    public MagicEgg(Player owner, Block egg, boolean spawnCloud) {
        this.owner = owner;
        this.egg = egg;
        this.barKey = new NamespacedKey(PCS_End.getInstance(), Integer.toString(egg.hashCode()));
        this.bossBar = Bukkit.createBossBar(barKey, getType().getTitle() + " von " + getOwner().getName(), getType().getBarColor(), BarStyle.SOLID, BarFlag.DARKEN_SKY);

        if(spawnCloud) {
            cloud = (AreaEffectCloud) getEgg().getWorld().spawnEntity(getEgg().getLocation().add(0.5, 0, 0.5), EntityType.AREA_EFFECT_CLOUD);
            cloud.setReapplicationDelay(20);
            cloud.setRadiusPerTick(0);
            cloud.setRadiusOnUse(0);
            cloud.setDuration(getType().getDuration());
            cloud.setRadius(getType().getRange());
            cloud.setColor(getType().getColor());

            Bukkit.getPluginManager().registerEvents(this, PCS_End.getInstance());
        } else cloud = null;

        deploy();
        eggs.add(this);
    }

    @EventHandler
    public void onCloudApply(AreaEffectCloudApplyEvent event) {

    }

    @EventHandler
    public void onGravity(EntityChangeBlockEvent event) {
        if(event.getBlock().equals(egg) && event.getEntityType() == EntityType.FALLING_BLOCK && event.getTo() == Material.AIR) {
            event.setCancelled(true);
            event.getBlock().getState().update(false, false);
            event.getEntity().remove();
        }
    }

    @Nonnull
    public abstract MagicEggType getType();

    public abstract boolean onDeploy();
    public abstract void onExpire();

    abstract void activate();

    private boolean deploy() {
        if(!onDeploy()) return false;

        int period = 2;
        new BukkitRunnable() {
            int timePassed = 0;

            @Override
            public void run() {
                getBossBar().setProgress(Math.max(0.001, (getType().getDuration() - timePassed) / (double) getType().getDuration()));

                getBossBar().getPlayers().stream().filter(p -> !p.getWorld().equals(getEgg().getWorld()) || p.getLocation().distanceSquared(getEgg().getLocation()) > Math.pow(getType().getRange(), 2)).forEach(p -> getBossBar().removePlayer(p));

                Collection<Entity> entities = getEgg().getWorld().getNearbyEntities(getEgg().getLocation(), getType().getRange() * 1.5, 3, getType().getRange() * 1.5);
                entities.stream().filter(entity -> entity.getType() == EntityType.PLAYER).forEach(p -> getBossBar().addPlayer(((Player)p)));

                timePassed += period;
                if(timePassed >= getType().getDuration()) cancel();
            }
        }.runTaskTimer(PCS_End.getInstance(), 0, period);

        new BukkitRunnable() {
            @Override
            public void run() {
                expire();
            }
        }.runTaskLater(PCS_End.getInstance(), getType().getDuration());

        activate();
        return true;
    }

    public void expire() {
        onExpire();
        getBossBar().removeAll();
        Bukkit.removeBossBar(barKey);
        HandlerList.unregisterAll(this);
        eggs.remove(this);
        getEgg().setType(Material.AIR);
        if(getCloud() != null) getCloud().remove();
    }

    protected final Player getOwner() {
        return owner;
    }

    public final Block getEgg() {
        return egg;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public AreaEffectCloud getCloud() {
        return cloud;
    }

    protected final Collection<Entity> getEntities(Predicate<Entity> filter) {
        return egg.getWorld().getNearbyEntities(egg.getLocation(), getType().getRange(), getType().getRange(), getType().getRange()).stream().filter(filter).collect(Collectors.toSet());
    }

    public static Set<MagicEgg> getEggs() {
        return eggs;
    }
}
