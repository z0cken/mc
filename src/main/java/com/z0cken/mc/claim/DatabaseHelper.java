package com.z0cken.mc.claim;

import com.z0cken.mc.core.Database;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import java.util.stream.Collectors;

class DatabaseHelper {

    private static final Database DATABASE = Database.MAIN;
    private static final Logger log = PCS_Claim.getInstance().getLogger();
    private static final Queue<Claim> queue = new ConcurrentLinkedQueue<>();

    static {
        setupTables();

        new BukkitRunnable() {
            @Override
            public void run() {
                push();
            }
        }.runTaskTimerAsynchronously(PCS_Claim.getInstance(), 100, Math.max(PCS_Claim.getInstance().getConfig().getInt("push-interval")*20, 100));
    }

    private DatabaseHelper() {}

    private static void setupTables() {
        try (Connection connection = DATABASE.getConnection();
             Statement statement = connection.createStatement()) {
            String prefix = "CREATE TABLE IF NOT EXISTS ";

            statement.addBatch(prefix + "claims (x INT NOT NULL, z INT NOT NULL, player CHAR(36) NOT NULL, block_x INT NOT NULL , block_y INT NOT NULL , block_z INT NOT NULL, material VARCHAR(50) NOT NULL, CONSTRAINT pk PRIMARY KEY (x, z));");
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void commit(Claim claim) {
        queue.add(claim);
    }

    static void push() {
        if(queue.isEmpty()) return;

        int add = 0, rem = 0;

        try (Connection connection = DATABASE.getConnection();
             PreparedStatement statementAdd = connection.prepareStatement("INSERT INTO claims VALUES(?, ?, ?, ?, ?, ?, ?);");
             PreparedStatement statementRem = connection.prepareStatement("DELETE FROM claims WHERE x = ? AND z = ?")) {

            while (!queue.isEmpty()) {
                Claim claim = queue.peek();

                boolean hasOwner = claim.getOwner() != null;

                PreparedStatement pstmt = hasOwner ? statementAdd : statementRem;
                pstmt.setInt(1, claim.getChunk().getX());
                pstmt.setInt(2, claim.getChunk().getZ());
                if (hasOwner) {
                    pstmt.setString(3, claim.getOwner().getUniqueId().toString());
                    pstmt.setInt(4, claim.getBaseBlock().getX());
                    pstmt.setInt(5, claim.getBaseBlock().getY());
                    pstmt.setInt(6, claim.getBaseBlock().getZ());
                    pstmt.setString(7, claim.getBaseMaterial().name());
                }

                pstmt.executeUpdate();
                if(hasOwner) add++; else rem++;
                queue.remove();
            }

            log.info("[PUSH] ADD " + add + " | " + rem + " REM");

        } catch (SQLException e) {
            e.printStackTrace();
            log.severe(">>> Failed to push queue - dumping remaining content <<<");
            queue.stream().map(claim -> ">>> " + claim.getName() + " -> " + (claim.getOwner() == null ? "null" : claim.getOwner().getUniqueId())).collect(Collectors.toList()).forEach(log::severe);
            queue.clear();
        }
    }

    /*
    public static void populate(ConcurrentHashMap<Chunk, Claim> claims, World world) throws SQLException {
        connect();

        try(ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM claims;")) {
            while (resultSet.next()) {
                final Chunk chunk = world.getChunkAt(resultSet.getInt(1), resultSet.getInt(2));
                final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString(3)));
                final Location baseLocation = new Location(world, resultSet.getInt(4), resultSet.getInt(5), resultSet.getInt(6));
                claims.put(chunk, new Claim(offlinePlayer, baseLocation));
            }
        }
    }*/

    static Claim getClaim(@Nonnull Chunk chunk) {

        try(Connection connection = DATABASE.getConnection();
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM claims WHERE x =" + chunk.getX() + " AND z = " + chunk.getZ() + ";")) {
            if(resultSet.next()) {
                Location location = new Location(chunk.getWorld(), resultSet.getInt(4), resultSet.getInt(5), resultSet.getInt(6));
                return new Claim(UUID.fromString(resultSet.getString(3)), location, Material.valueOf(resultSet.getString(7)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    static Map<ChunkCoordinate, Claim> getClaims(@Nonnull World world, @Nonnull Set<ChunkCoordinate> set) {

        HashMap<ChunkCoordinate, Claim> result = new HashMap<>();
        try(Connection connection = DATABASE.getConnection();
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM claims;")) {
            while(resultSet.next()) {
                ChunkCoordinate coordinate = new ChunkCoordinate(resultSet.getInt(1), resultSet.getInt(2));
                if(set.contains(coordinate)) {
                    Location location = new Location(world, resultSet.getInt(4), resultSet.getInt(5), resultSet.getInt(6));
                    result.put(coordinate, new Claim(UUID.fromString(resultSet.getString(3)), location, Material.valueOf(resultSet.getString(7))));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        result.forEach((chunkCoordinate, claim) -> System.out.println(chunkCoordinate + " : " + claim.getOwner().getUniqueId()));
        return result;
    }

    static Set<Claim> getClaims(@Nonnull World world, @Nonnull OfflinePlayer player) {

        Set<Claim> claims = new HashSet<>();
        try(Connection connection = DATABASE.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM claims WHERE player = ?;")) {
            pstmt.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = pstmt.executeQuery();

            while(resultSet.next()) {
                Location location = new Location(world, resultSet.getInt(4), resultSet.getInt(5), resultSet.getInt(6));
                claims.add(new Claim(player.getUniqueId(), location, Material.valueOf(resultSet.getString(7))));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return claims;
    }

    static Set<Claim> getAllClaims(@Nonnull World world) {

        Set<Claim> claims = new HashSet<>();
        try(Connection connection = DATABASE.getConnection();
            Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM claims;");

            while(resultSet.next()) {
                Location location = new Location(world, resultSet.getInt(4), resultSet.getInt(5), resultSet.getInt(6));
                claims.add(new Claim(UUID.fromString(resultSet.getString(3)), location, Material.valueOf(resultSet.getString(7))));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return claims;
    }

    static void updateMaterial(@Nonnull Claim claim, @Nonnull Material baseMaterial) {
        try(Connection connection = DATABASE.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("UPDATE claims SET material = ? WHERE x = ? AND z = ?;")) {
            pstmt.setString(1, baseMaterial.name());
            pstmt.setInt(2, claim.getChunkCoordinate().getX());
            pstmt.setInt(3, claim.getChunkCoordinate().getZ());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
