package com.z0cken.mc.essentials.modules;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.z0cken.mc.core.Database;
import com.z0cken.mc.core.util.MessageBuilder;
import com.z0cken.mc.essentials.PCS_Essentials;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class ModuleDiscover extends Module implements Listener {

    private static final Database DATABASE = Database.MAIN;
    private static int INTERVAL, MAX_DISTANCE_SQ;
    private final Set<Place> places = new HashSet<>();

    public ModuleDiscover(String configPath) {
        super(configPath);
        setupTables();
        importPlaces();

        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    Location location = p.getLocation();
                    places.forEach(r -> {
                        if(r.getRegion().contains(BlockVector3.at(location.getX(), location.getY(), location.getZ()))) {
                                if(hasDiscovered(p, r.getRegion().getId())) return;
                                discover(p, r.getRegion().getId());
                                p.spigot().sendMessage(MessageBuilder.DEFAULT.define("NAME", r.getName()).build(getConfig().getString("messages.discovered")));
                        }
                    });
                });
            }
        }.runTaskTimerAsynchronously(PCS_Essentials.getInstance(), INTERVAL, INTERVAL));
    }

    @Override
    protected void load() {
        INTERVAL = Math.max(100, getConfig().getInt("interval") * 20);
        MAX_DISTANCE_SQ = (int) Math.pow(getConfig().getInt("max-distance"), 2);
    }

    private void importPlaces() {
        ConfigurationSection section = getConfig().getConfigurationSection("places");

        for(String world : section.getKeys(false)) {
            World w = Bukkit.getWorld(world);
            if(w == null) continue;

            for(String place : section.getConfigurationSection(world).getKeys(false)) {
                ProtectedRegion region = getRegion(w, place);
                if(region == null) continue;

                final ConfigurationSection childSection = section.getConfigurationSection(world).getConfigurationSection(place);
                Location portal = childSection.getSerializable("source", Location.class);
                Location target = childSection.getSerializable("target", Location.class);
                places.add(new Place(portal, target, region, childSection.getString("name")));
            }
        }
    }

    private ProtectedRegion getRegion(@Nonnull World world, @Nonnull String id) {
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(world));
        if(regionManager == null) return null;
        return regionManager.getRegion(id);
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        for(Place place : places) {
            if(place.isNearSource(event.getFrom())) {

                event.setCancelled(true);
                if(!hasDiscovered(event.getPlayer(), place.getRegion().getId())) event.getPlayer().spigot().sendMessage(MessageBuilder.DEFAULT.define("NAME", place.getName()).build(getConfig().getString("messages.undiscovered")));
                else event.getPlayer().teleport(place.getTarget());

            } else if(place.isNearTarget(event.getFrom())) {
                event.setCancelled(true);
                event.getPlayer().teleport(place.getSource());
            }
        }
    }

    void setupTables() {
        try(Connection connection = DATABASE.getConnection();
            Statement statement = connection.createStatement()) {

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS discover (player CHAR(36), place CHAR(40), CONSTRAINT pk PRIMARY KEY (player, place));");

        } catch (SQLException e) {
            e.printStackTrace();
            disable();
        }
    }

    boolean hasDiscovered(@Nonnull Player player, @Nonnull String value) {
        try (Connection connection = DATABASE.getConnection();
             PreparedStatement pstmt = connection.prepareStatement("SELECT COUNT(*) FROM discover WHERE player = ? AND place = ?")){

            pstmt.setString(1, player.getUniqueId().toString());
            pstmt.setString(2, value);

            ResultSet resultSet = pstmt.executeQuery();

            if (resultSet.next()) return resultSet.getInt(1) > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            disable();
        }

        return false;
    }

    void discover(Player player, String id) {
        try (Connection connection = DATABASE.getConnection();
             PreparedStatement pstmt = connection.prepareStatement("INSERT IGNORE INTO discover VALUES(?, ?)")){

            pstmt.setString(1, player.getUniqueId().toString());
            pstmt.setString(2, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            disable();
        }
    }

    private static class Place {
        Location source, target;
        ProtectedRegion region;
        String name;

        Place(@Nonnull Location source, @Nonnull Location target, @Nonnull ProtectedRegion region, @Nonnull String name) {
            this.source = source;
            this.target = target;
            this.region = region;
            this.name = name;
        }

        public ProtectedRegion getRegion() {
            return region;
        }

        public String getName() {
            return name;
        }

        public boolean isNearSource(Location location) {
            return source.distanceSquared(location) < MAX_DISTANCE_SQ;
        }

        public boolean isNearTarget(Location location) {
            return target.distanceSquared(location) < MAX_DISTANCE_SQ;
        }

        public Location getSource() {
            return source;
        }

        public Location getTarget() {
            return target;
        }
    }
}
