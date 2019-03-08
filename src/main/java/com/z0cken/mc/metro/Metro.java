package com.z0cken.mc.metro;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public final class Metro {

    private static Metro instance;
    private final ProtectedRegion region;

    public static Metro getInstance() {
        if(instance == null) instance = new Metro(Bukkit.getWorld("world"), "metro");
        return instance;
    }

    private static final Logger log = PCS_Metro.getInstance().getLogger();
    private final Set<Player> players = new LinkedHashSet<>();
    private final Set<Station> stations;

    private final Map<EntityType, Map<String, EntityTemplate>> entityTemplates;
    private final Map<String, SpawnProfile> profiles;

    private Metro(World world, String id) {
        region = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(world)).getRegion(id);
        if(region == null) throw new IllegalArgumentException("Region '" + id + "' in " + world.getName() + " not found!");

        FileConfiguration config = PCS_Metro.getInstance().getConfig();

        /* */
        final Set<Station> stationSet = new LinkedHashSet<>();
        final ConfigurationSection section = config.getConfigurationSection("stations");
        log.info("Loading stations...");
        for(String s : section.getKeys(false)) {
            Station station = Station.fromConfig(section, s);
            log.info("> " + station.getName() + " : " + station.getBeacon().getSupply());
            stationSet.add(station);
        }

        stations = Collections.unmodifiableSet(stationSet);

        /* */
        log.info("Loading entity templates...");
        final Map<EntityType, Map<String, EntityTemplate>> templateMap = new HashMap<>();
        final File templateFile = new File(PCS_Metro.getInstance().getDataFolder(), "/templates");
        final File[] templateFiles = templateFile.listFiles();
        if(templateFiles != null) {
            Arrays.stream(templateFiles).filter(File::isDirectory).forEach(directory -> {
                EntityType type = EntityType.valueOf(directory.getName().toUpperCase());
                Map<String, EntityTemplate> nestedMap = new HashMap<>();
                templateMap.put(type, nestedMap);

                File[] subFiles = directory.listFiles();
                if(subFiles == null) return;
                Arrays.stream(subFiles).filter(File::isFile).filter(f -> f.getName().toLowerCase().endsWith(".yml")).forEach(f -> {
                    nestedMap.put(FilenameUtils.removeExtension(f.getName().toLowerCase()), new EntityTemplate(YamlConfiguration.loadConfiguration(f)));
                    log.info(String.format("> %s : %s", directory.getName(), f.getName()));
                });
            });
        } else log.warning(String.format("No entity templates found in %s", templateFile.getPath()));
        entityTemplates = Collections.unmodifiableMap(templateMap);

        /* */
        log.info("Loading spawn profiles...");
        final Map<String, SpawnProfile> profileMap = new HashMap<>();
        final File profileFile = new File(PCS_Metro.getInstance().getDataFolder(), "/profiles");
        final File[] profileFiles = profileFile.listFiles();
        if(profileFiles != null) {
            Arrays.stream(profileFiles).filter(File::isFile).filter(f -> f.getName().toLowerCase().endsWith(".yml")).forEach(f -> {
                profileMap.put(FilenameUtils.removeExtension(f.getName()).toLowerCase(), new SpawnProfile(this, YamlConfiguration.loadConfiguration(f)));
                log.info(String.format("> %s", f.getName()));
            });
        } else log.warning(String.format("No spawn profiles found in %s", profileFile.getPath()));
        profiles = Collections.unmodifiableMap(profileMap);

        /* */
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                //Synchronize
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        log.info("Draining beacons...");
                        stations.forEach(station -> station.getBeacon().decrement());
                    }
                }.runTask(PCS_Metro.getInstance());

                DatabaseHelper.decrementAll();
            }
        }, /*getNextEvenHour(), 2 * 60 * 60 * 1000*/ 5000, 10000);

        /* */
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    if(p.getWorld().equals(world) && contains(p.getLocation())) players.add(p);
                    else players.remove(p);
                });
            }
        }.runTaskTimerAsynchronously(PCS_Metro.getInstance(), 100, PCS_Metro.getInstance().getConfig().getInt("entry-interval") * 20);
    }

    public Set<Station> getStations() {
        return stations;
    }

    public int getActiveStations() {
        return (int) stations.stream().filter(Station::isActive).count();
    }

    public Set<Player> getPlayersInside() {
        return Collections.unmodifiableSet(players);
    }

    public boolean contains(Location location) {
        return region.contains((int) location.getX(), (int) location.getY(), (int) location.getZ());
    }

    public SpawnProfile getProfile(String id) {
        return profiles.get(id);
    }

    public EntityTemplate getTemplate(EntityType type, String id) {
        return entityTemplates.get(type).get(id);
    }

    private static Date getNextEvenHour() {
        Calendar c = new GregorianCalendar();
        c.setTime(new Date());
        c.add(Calendar.HOUR, c.get(Calendar.HOUR) % 2 == 0 ? 2 : 1);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTime();
    }

}
