package com.z0cken.mc.metro;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.z0cken.mc.metro.spawn.EntityTemplate;
import com.z0cken.mc.metro.spawn.GroupTemplate;
import com.z0cken.mc.metro.spawn.SpawnProfile;
import com.z0cken.mc.progression.PCS_Progression;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class Metro {

    private static Metro instance;
    private final ProtectedRegion region;
    private int rate, interval;
    private World world;

    public static Metro getInstance() {
        if(instance == null) instance = new Metro(Bukkit.getWorld("world"), "metro");
        return instance;
    }

    private static final Logger log = PCS_Metro.getInstance().getLogger();
    private final Set<Player> players = Collections.synchronizedSet(new HashSet<>());
    private final Set<Player> excludedPlayers = new HashSet<>();
    private final Set<Station> stations;

    private List<MetroEffect> effects;
    private Map<String, EntityTemplate> entityTemplates;
    private Map<String, GroupTemplate> groupTemplates;
    private Map<String, SpawnProfile> profiles;

    private Metro(World world, String id) {
        this.world = world;

        region = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(world)).getRegion(id);
        if(region == null) throw new IllegalArgumentException("Region '" + id + "' in " + world.getName() + " not found!");

        rate = PCS_Metro.getInstance().getConfig().getInt("lapis.amount");
        interval = PCS_Metro.getInstance().getConfig().getInt("lapis.interval-hours");

        stations = loadStations();
        entityTemplates = loadEntityTemplates();
        groupTemplates = loadGroupTemplates();
        profiles = loadProfiles();
        effects = loadEffects();

        if(effects.size() < stations.size()) log.severe(String.format("There are %d stations but only %d effects", stations.size(), effects.size()));

        getAppropriateEffect().activate();

        final Date nextHour = getNextHour(interval);
        log.info("Next drain: " + nextHour);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                //Synchronize
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        log.info("Draining beacons...");
                        getActiveStations().forEach(station -> station.getBeacon().drain());
                    }
                }.runTask(PCS_Metro.getInstance());

                DatabaseHelper.decrementAll(rate);
            }
        }, nextHour, interval * 60 * 60 * 1000L);

        new BukkitRunnable() {
            @Override
            public void run() {
                players.clear();
                world.getPlayers().stream().filter(p -> contains(p.getLocation())).forEach(players::add);
            }
        }.runTaskTimerAsynchronously(PCS_Metro.getInstance(), 100, PCS_Metro.getInstance().getConfig().getInt("entry-check-interval") * 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                players.forEach(p -> PCS_Progression.progress(p, "metro_time", 5));
            }
        }.runTaskTimer(PCS_Metro.getInstance(), 6000, 6000);
    }

    public Set<Station> getStations() {
        return stations;
    }

    public Set<Station> getActiveStations() {
        return stations.stream().filter(Station::isActive).collect(Collectors.toSet());
    }

    public int getInterval() {
        return interval;
    }

    public int getRate() {
        return rate;
    }

    public Set<Player> getPlayersInside() {
        return Collections.unmodifiableSet(players);
    }

    public boolean contains(Location location) {
        if(!location.getWorld().equals(world)) return false;
        return region.contains((int) location.getX(), (int) location.getY(), (int) location.getZ());
    }

    public List<MetroEffect> getEffects() {
        return effects;
    }

    public MetroEffect getAppropriateEffect() {
        return effects.get(getActiveStations().size());
    }

    public SpawnProfile getProfile(String id) {
        return profiles.getOrDefault(id, null);
    }

    public EntityTemplate getEntityTemplate(String id) {
        return entityTemplates.getOrDefault(id, null);
    }

    public GroupTemplate getGroupTemplate(String id) {
        return groupTemplates.getOrDefault(id, null);
    }

    public void reload(){
        entityTemplates = loadEntityTemplates();
        groupTemplates = loadGroupTemplates();
        profiles = loadProfiles();
        effects = loadEffects();
    }

    private static Date getNextHour(int divisor) {
        Calendar c = new GregorianCalendar();
        c.setTime(new Date());
        c.add(Calendar.HOUR_OF_DAY, 1);

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int old = hour;
        while(hour % divisor != 0) hour++;
        c.add(Calendar.HOUR_OF_DAY, hour - old);

        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTime();
    }


    private Set<Station> loadStations() {
        final Set<Station> stationSet = new LinkedHashSet<>();
        Configuration effectsConfig = YamlConfiguration.loadConfiguration(new File(PCS_Metro.getInstance().getDataFolder(), "stations.yml"));
        log.info("Loading stations...");
        for(String s : effectsConfig.getKeys(false)) {
            Station station = Station.fromConfig(this, effectsConfig, s);
            log.info(String.format("> %s (%d)", station.getName(), station.getBeacon().getSupply()));
            stationSet.add(station);
        }
        return Collections.unmodifiableSet(stationSet);
    }

    private List<MetroEffect> loadEffects() {
        log.info("Loading effects...");
        final List<MetroEffect> list = new ArrayList<>();

        Configuration effectsConfig = YamlConfiguration.loadConfiguration(new File(PCS_Metro.getInstance().getDataFolder(), "effects.yml"));
        MetroEffect.applySettings( effectsConfig.getConfigurationSection("settings"));

        final ConfigurationSection section = effectsConfig.getConfigurationSection("effects");
        for(String s : section.getKeys(false)) {
            list.add(new MetroEffect(this, section.getConfigurationSection(s)));
        }

        return Collections.unmodifiableList(list);
    }

    private Map<String, EntityTemplate> loadEntityTemplates() {
        log.info("Loading entity templates...");
        final Map<String, EntityTemplate> entityTemplateMap = new HashMap<>();
        final File entityTemplateFile = new File(PCS_Metro.getInstance().getDataFolder(), "//templates");
        final File[] entityTemplateFiles = entityTemplateFile.listFiles();
        if(entityTemplateFiles != null) {
            Arrays.stream(entityTemplateFiles).filter(File::isFile).filter(f -> f.getName().toLowerCase().endsWith(".yml")).forEach(f -> {
                entityTemplateMap.put(FilenameUtils.removeExtension(f.getName()).toLowerCase(), new EntityTemplate(YamlConfiguration.loadConfiguration(f)));
                log.info(String.format("> %s", f.getName()));
            });
        } else log.warning(String.format("No entity templates found in %s", entityTemplateFile.getPath()));
        return Collections.unmodifiableMap(entityTemplateMap);
    }

    private Map<String, GroupTemplate> loadGroupTemplates() {
        log.info("Loading group templates...");
        final Map<String, GroupTemplate> groupTemplateMap = new HashMap<>();
        final File groupTemplateFile = new File(PCS_Metro.getInstance().getDataFolder(), "//groups");
        final File[] groupTemplateFiles = groupTemplateFile.listFiles();
        if(groupTemplateFiles != null) {
            Arrays.stream(groupTemplateFiles).filter(File::isFile).filter(f -> f.getName().toLowerCase().endsWith(".yml")).forEach(f -> {
                groupTemplateMap.put(FilenameUtils.removeExtension(f.getName()).toLowerCase(), new GroupTemplate(YamlConfiguration.loadConfiguration(f), entityTemplates));
                log.info(String.format("> %s", f.getName()));
            });
        } else log.warning(String.format("No group templates found in %s", groupTemplateFile.getPath()));
        return Collections.unmodifiableMap(groupTemplateMap);
    }

    private Map<String, SpawnProfile> loadProfiles() {
        log.info("Loading spawn profiles...");
        final Map<String, SpawnProfile> profileMap = new HashMap<>();
        final File profileFile = new File(PCS_Metro.getInstance().getDataFolder(), "//profiles");
        final File[] profileFiles = profileFile.listFiles();
        if(profileFiles != null) {
            Arrays.stream(profileFiles).filter(File::isFile).filter(f -> f.getName().toLowerCase().endsWith(".yml")).forEach(f -> {
                profileMap.put(FilenameUtils.removeExtension(f.getName()).toLowerCase(), new SpawnProfile(this, YamlConfiguration.loadConfiguration(f)));
                log.info(String.format("> %s", f.getName()));
            });
        } else log.warning(String.format("No spawn profiles found in %s", profileFile.getPath()));
        return Collections.unmodifiableMap(profileMap);
    }

    public World getWorld() {
        return world;
    }

    public boolean isExcluded(Player player) {
        return excludedPlayers.contains(player);
    }

    public void excludePlayer(Player player, boolean value) {
        if(value) {
            excludedPlayers.add(player);
            Metro.getInstance().getAppropriateEffect().getPotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        }
        else excludedPlayers.remove(player);
    }
}
