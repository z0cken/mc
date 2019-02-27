package com.z0cken.mc.metro;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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

    private Metro(World world, String id) {
        region = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(world)).getRegion(id);
        if(region == null) throw new IllegalArgumentException("Region '" + id + "' in " + world.getName() + " not found!");

        //TODO Load stations
        FileConfiguration config = PCS_Metro.getInstance().getConfig();

        Set<Station> set = new LinkedHashSet<>();
        final ConfigurationSection section = config.getConfigurationSection("stations");
        for(String s : section.getKeys(false)) {
            log.info("Loading stations...");
            Station station = Station.fromConfig(section, s);
            log.info("> " + station.getName() + " : " + station.getBeacon().getSupply());
            set.add(station);
        }

        stations = Collections.unmodifiableSet(set);

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

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    if(contains(p.getLocation())) players.add(p);
                    else players.remove(p);
                });
            }
        }.runTaskTimerAsynchronously(PCS_Metro.getInstance(), 100, PCS_Metro.getInstance().getConfig().getInt("entry-interval") * 20);
    }

    public Set<Station> getStations() {
        return stations;
    }

    public Set<Player> getPlayersInside() {
        return Collections.unmodifiableSet(players);
    }

    public boolean contains(Location location) {
        return region.contains((int) location.getX(), (int) location.getY(), (int) location.getZ());
    }

    private static Date getNextEvenHour() {
        Calendar c = new GregorianCalendar();
        c.setTime(new Date());
        c.add(Calendar.HOUR, 1);
        c.add(Calendar.HOUR, c.get(Calendar.HOUR) % 2 == 0 ? 2 : 1);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTime();
    }

}
