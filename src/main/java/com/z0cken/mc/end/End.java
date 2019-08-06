package com.z0cken.mc.end;

import com.z0cken.mc.end.phase.EndPhase;
import com.z0cken.mc.end.phase.PhaseType;
import com.z0cken.mc.progression.PCS_Progression;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class End implements Listener {

    private ConfigurationSection configSection;

    private final long respawnTime;
    private final World world;
    private final int mainRadius;

    private EndPhase phase;
    private DragonRespawnRunnable respawnRunnable;
    private Set<Player> mainPlayers = new HashSet<>();

    private File elytrasFile;

    //TODO Define rollback area
    End(ConfigurationSection section) {
        configSection = section;

        final String worldName = section.getString("world");
        World w = Bukkit.getWorld(worldName);
        world = w != null ? w : new ManagedEndCreator(worldName, section.getInt("max-elytra")).getWorld();

        this.respawnTime = section.getLong("respawn-time");
        this.mainRadius = section.getInt("main-radius");
        getWorld().setSpawnLocation(section.getSerializable("spawn", Location.class));

        elytrasFile = new File(PCS_End.getInstance().getDataFolder(), worldName + ".yml");

        final String lastPhase = section.getString("last-phase");
        new BukkitRunnable() {
            @Override
            public void run() {
                runPhase(lastPhase != null ? PhaseType.valueOf(lastPhase.toUpperCase()) : PhaseType.DRAGON);
            }
        }.runTaskLater(PCS_End.getInstance(), 40);

        new BukkitRunnable() {
            int mainDistanceSquared = (int) Math.pow(mainRadius, 2);
            @Override
            public void run() {
                mainPlayers.clear();

                for(Player player : getWorld().getPlayers()) {
                    if(player.getLocation().distanceSquared(new Location(getWorld(), 0, player.getLocation().getY(), 0)) <= mainDistanceSquared) {
                        mainPlayers.add(player);
                    } else {
                        if(player.isGliding()) {
                            player.removePotionEffect(PotionEffectType.SLOW_FALLING);
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, PCS_End.getInstance().getConfig().getInt("player-check-interval") + 1, 20, false, false));
                        }
                    }
                }
            }
        }.runTaskTimer(PCS_End.getInstance(), 5, PCS_End.getInstance().getConfig().getInt("player-check-interval"));

        new BukkitRunnable() {
            @Override
            public void run() {
                getWorld().getPlayers().forEach(p -> PCS_Progression.progress(p, "end_time", 5));
            }
        }.runTaskTimer(PCS_End.getInstance(), 6000, 6000);

        Bukkit.getPluginManager().registerEvents(new EndListener(this), PCS_End.getInstance());
    }

    public void runPhase(PhaseType nextPhase) {
        if(respawnRunnable != null) {
            respawnRunnable.cancel();
            respawnRunnable = null;
        }

        if(phase != null) {
            PCS_End.getInstance().getLogger().info("Stopping phase " + phase.getType().name());
            phase.stop();
        }

        if((phase == null || phase.getType() == PhaseType.DRAGON) && nextPhase != PhaseType.DRAGON) {
            respawnRunnable = new DragonRespawnRunnable(getWorld(), respawnTime, () -> runPhase(PhaseType.DRAGON));
            respawnRunnable.runTaskTimer(PCS_End.getInstance(), 20, 20);
            PCS_End.getInstance().getLogger().info("Running dragon respawn timer");
        }

        try { phase = nextPhase.make(this);
        } catch (Exception e) { e.printStackTrace(); }

        PCS_End.getInstance().getLogger().info("Starting phase " + phase.getType().name());
        phase.start();
    }

    public void save() {
        PCS_End.getInstance().getConfig().set("rollback", System.currentTimeMillis());
    }

    public void rollback() {
        final long cfgLong = PCS_End.getInstance().getConfig().getLong("rollback");
        if(cfgLong == 0) {
            save();
            return;
        }
        int time = (int) ((System.currentTimeMillis() - cfgLong) / 1000 + 5);
        PCS_End.getInstance().getLogger().info(String.format("Rolling back %d seconds", time));
        new BukkitRunnable() {
            @Override
            public void run() {
                PCS_End.getCoreProtect().performRollback(time, null, null, null, null, null, mainRadius, getWorld().getSpawnLocation());
                PCS_End.getInstance().getLogger().info("Rollback complete!");
            }
        }.runTaskAsynchronously(PCS_End.getInstance());
    }

    public World getWorld() {
        return world;
    }

    public EndPhase getPhase() {
        return phase;
    }

    public ConfigurationSection getConfig() { return configSection; }

    public Set<Player> getMainPlayers() {
        return Collections.unmodifiableSet(mainPlayers);
    }

    public DragonRespawnRunnable getRespawnRunnable() {
        return respawnRunnable;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(!event.getPlayer().getWorld().equals(getWorld())) return;

        //Stop crystals from being placed
        //TODO Alternative instanceof CraftSmartCrystal
        if(event.getItem() != null && event.getItem().getType() == Material.END_CRYSTAL) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if(event.getPlayer().getWorld().equals(getWorld())) {
            //Enter
        } else if(event.getFrom().equals(getWorld())) {
            //Leave
        }
    }

    public void tryFindElytra(Location location) {
        YamlConfiguration matches = YamlConfiguration.loadConfiguration(elytrasFile);

        for(String key : matches.getKeys(false)) {
            Location matchLoc = matches.getSerializable(key + ".location", Location.class);
            if(matchLoc.distanceSquared(location) < 1 && !matches.getBoolean(key + " .found")) {
                matches.set(key + ".found", true);
                try {
                    matches.save(elytrasFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int getAvailableElytra() {
        YamlConfiguration matches = YamlConfiguration.loadConfiguration(elytrasFile);

        int available = 0;
        for(String key : matches.getKeys(false)) {
            if(!matches.getBoolean(key + ".found")) available++;
        }

        return available;
    }
}
