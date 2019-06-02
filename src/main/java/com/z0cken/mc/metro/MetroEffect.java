package com.z0cken.mc.metro;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class MetroEffect implements Listener {

    private Metro metro;

    private Set<BukkitTask> tasks = new HashSet<>();

    private Difficulty difficulty;
    private double portalSuccess;
    private int moneyBonus;
    private Integer thunderTicks, rainTicks;
    private Set<PotionEffect> potionEffects = new HashSet<>();
    private Map<ProtectedRegion, Set<EntityType>> allowMonsters = new HashMap<>();
    private Set<String> flags = new HashSet<>();

    public MetroEffect(Metro metro, ConfigurationSection config) {
        this.metro = metro;
        difficulty = Difficulty.valueOf(config.getString("difficulty").toUpperCase());
        portalSuccess = config.contains("portal-success") ? config.getDouble("portal-success") : 1;

        if(config.contains("potions")) {
            final ConfigurationSection sec = config.getConfigurationSection("potions");
            for(String s : sec.getKeys(false)) potionEffects.add(new PotionEffect(PotionEffectType.getByName(s), Integer.MAX_VALUE, sec.getInt(s), false, false));
        }

        thunderTicks = config.contains("thunder-ticks") ? config.getInt("thunder-ticks") : null;
        rainTicks = config.contains("rain-ticks") ? config.getInt("rain-ticks") : null;
        moneyBonus = config.getInt("money-bonus");

        RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(Bukkit.getWorld(PCS_Metro.getInstance().getConfig().getString("world"))));
        if(rm != null) config.getStringList("allow-monsters").forEach(s -> {
                            ProtectedRegion region = rm.getRegion(s);
                            if(region == null) {
                                PCS_Metro.getInstance().getLogger().warning(String.format("Region '%s' not found", s));
                            } else allowMonsters.put(region, region.getFlag(Flags.DENY_SPAWN));
                        });

        flags.addAll(config.getStringList("flags"));

        Bukkit.getPluginManager().registerEvents(this, PCS_Metro.getInstance());
    }

    private static int blussiBoost, pigmenRange, moneyBonusInterval;

    public static void applySettings(ConfigurationSection section) {
        blussiBoost = section.getInt("blussi-boost");
        pigmenRange = section.getInt("pigmen-range");
        moneyBonusInterval = section.getInt("money-bonus-interval");
    }

    public void activate() {
        metro.getWorld().setDifficulty(difficulty);

        Bukkit.getOnlinePlayers().forEach(p -> Metro.getInstance().setExcluded(p, false));

        if (!potionEffects.isEmpty()) {
            tasks.add(new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        if (!p.hasPermission("pcs.metro.bypass") && !Metro.getInstance().isExcluded(p)) potionEffects.forEach(p::addPotionEffect);
                    });
                }
            }.runTaskTimer(PCS_Metro.getInstance(), 10*20, 10*20));
        }

        metro.getWorld().setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        if(thunderTicks != null) {
            if(thunderTicks == 0) {
                metro.getWorld().setThundering(true);
            } else if(thunderTicks == -1)  metro.getWorld().setThundering(false);
        } else if(rainTicks != null) {
            if(rainTicks == 0) {
                metro.getWorld().setStorm(true);
            } else if(rainTicks == -1)  metro.getWorld().setStorm(false);
        } else  metro.getWorld().setGameRule(GameRule.DO_WEATHER_CYCLE, true);

        if(moneyBonus != 0) {
            tasks.add(new BukkitRunnable() {
                @Override
                public void run() {
                    Economy eco = Bukkit.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        eco.depositPlayer(p, moneyBonus);
                        p.spigot().sendMessage(PCS_Metro.getInstance().getMessageBuilder().define("VALUE", Integer.toString(moneyBonus)).build(PCS_Metro.getInstance().getConfig().getString("messages.money")));
                    });
                }
            }.runTaskTimer(PCS_Metro.getInstance(), moneyBonusInterval * 20 * 60, moneyBonusInterval * 20 * 60));
        }

        allowMonsters.forEach((region, types) ->  region.setFlag(Flags.DENY_SPAWN, null));
    }


    public void deactivate() {
        tasks.forEach(BukkitTask::cancel);
        allowMonsters.forEach(((region, types) -> {
            region.setFlag(Flags.DENY_SPAWN, types);
        }));

        Bukkit.getOnlinePlayers().forEach(p -> {
            potionEffects.forEach(effect -> p.removePotionEffect(effect.getType()));
        });
    }

    public Set<PotionEffect> getPotionEffects() {
        return Collections.unmodifiableSet(potionEffects);
    }

    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

    public double getPortalSuccess() {
        return portalSuccess;
    }

    public Integer getRainTicks() {
        return rainTicks;
    }

    public Integer getThunderTicks() {
        return thunderTicks;
    }

    public static int getBlussiBoost() {
        return blussiBoost;
    }

    public static int getPigmenRange() {
        return pigmenRange;
    }

}
