package com.z0cken.mc.end;

import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class CrystalDefenseRunnable extends BukkitRunnable {

    private End end;
    private Collection<EnderCrystal> crystals;
    private final int warnRangeSquared, killRangeSquared;
    private final double warnDamage;

    public CrystalDefenseRunnable(End end, Collection<EnderCrystal> crystals, int warnRange, double warnDamage, int killRange) {
        if(warnRange < killRange) throw new IllegalArgumentException("Warning range can't be smaller than kill range");
        this.end = end;
        this.crystals = crystals;
        this.warnRangeSquared = warnRange * warnRange;
        this.warnDamage = warnDamage;
        this.killRangeSquared = killRange * killRange;
    }

    @Override
    public void run() {

        Set<Player> gliders = end.getMainPlayers().stream().filter(LivingEntity::isGliding).collect(Collectors.toSet());

        for (EnderCrystal crystal : crystals) {
            //TODO Replace with iterator
            if(!crystal.isValid()) continue;

            boolean match = false;
            for(Player p : gliders) {
                double distanceSquared = p.getLocation().distanceSquared(crystal.getLocation());
                if(distanceSquared <= warnRangeSquared) {
                    crystal.setBeamTarget(p.getLocation());
                    match = true;

                    if(distanceSquared <= killRangeSquared) {
                        p.damage(Integer.MAX_VALUE, crystal);
                        //TODO Msg
                    } else {
                        if(p.getNoDamageTicks() > 20) p.damage(warnDamage);
                    }

                    break;
                }
            }

            if(!match) crystal.setBeamTarget(null);
        }

        /*
        Iterator<EnderCrystal> iterator = crystals.iterator();
        while (iterator.hasNext()) {
            EnderCrystal crystal = iterator.next();
            if(crystal.isDead()) {
                iterator.remove();
                continue;
            }

            //TODO Optimize
            crystal.getNearbyEntities(warnRangeSquared, warnRangeSquared, warnRangeSquared).stream().filter(e -> e.getType() == EntityType.PLAYER).filter(e -> ((Player)e).isGliding()).forEach(e -> {
                Player p = (Player) e;

                if(crystal.getLocation().distanceSquared(p.getLocation()) <= killRangeSquared) p.damage(Integer.MAX_VALUE, crystal);
                else {
                    if(p.getNoDamageTicks() > 20) p.damage(warnDamage);
                    crystal.setBeamTarget(p.getLocation());
                }
            });
        }*/
    }

}
