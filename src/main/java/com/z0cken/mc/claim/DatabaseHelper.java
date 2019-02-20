package com.z0cken.mc.claim;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Logger;
import java.util.stream.IntStream;

class DatabaseHelper {

    static {
        new BukkitRunnable() {
            @Override
            public void run() {
                push();
            }
        }.runTaskTimerAsynchronously(PCS_Claim.getInstance(), 100, PCS_Claim.getInstance().getConfig().getInt("push-interval")*20);
    }

    private static Connection connection;
    private static final Logger log = PCS_Claim.getInstance().getLogger();
    private static final ConcurrentLinkedDeque<Claim> deque = new ConcurrentLinkedDeque<>();

    private DatabaseHelper() {}

    static void connect() {
        if(isConnected()) return;

        ConfigurationSection config = PCS_Claim.getInstance().getConfig().getConfigurationSection("database");

        String ip = config.getString("ip");
        String db = config.getString("db");
        String user = config.getString("user");
        String password = config.getString("password");

        String url = "jdbc:mysql://%s/%s?" + "user=%s&password=%s";

        try {
            connection = DriverManager.getConnection(String.format(url, ip, db, user, password));
            setupTables();
        } catch (SQLException e) {
            log.severe("Database Connection Failed");
            log.severe("SQLException: " + e.getMessage());
            log.severe("SQLState: " + e.getSQLState());
            log.severe("VendorError: " + e.getErrorCode());
            Bukkit.getServer().shutdown();
        }
    }

    private static void setupTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String prefix = "CREATE TABLE IF NOT EXISTS ";

            statement.addBatch(prefix + "claims (x INT NOT NULL, z INT NOT NULL, player CHAR(36) NOT NULL, block_x INT NOT NULL , block_y INT NOT NULL , block_z INT NOT NULL );");
            statement.executeBatch();
        }
    }

    static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static boolean isConnected() {
        boolean connected = false;
        try {
            connected = connection != null && connection.isValid(30);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connected;
    }

    static void commit(Claim claim) {
        deque.add(claim);
    }

    static void push() {
        connect();

        try (PreparedStatement statementAdd = connection.prepareStatement("INSERT INTO claims VALUES(?, ?, ?, ?, ?, ?);");
             PreparedStatement statementRem = connection.prepareStatement("DELETE FROM claims WHERE x = ? AND z = ?")) {

            while (!deque.isEmpty()) {
                Claim claim = deque.peek();

                boolean hasOwner = claim.getPlayer() != null;

                PreparedStatement pstmt = hasOwner ? statementAdd : statementRem;
                pstmt.setInt(1, claim.getChunk().getX());
                pstmt.setInt(2, claim.getChunk().getZ());
                if (hasOwner) {
                    pstmt.setString(3, claim.getPlayer().getUniqueId().toString());
                    pstmt.setInt(4, claim.getBaseBlock().getX());
                    pstmt.setInt(5, claim.getBaseBlock().getY());
                    pstmt.setInt(6, claim.getBaseBlock().getZ());
                }

                pstmt.addBatch();
                deque.pop();
            }

            int[] add = statementAdd.executeBatch();
            int[] rem = statementRem.executeBatch();

            log.fine("[PUSH] ADD " + IntStream.of(add).sum() + " | " + IntStream.of(rem).sum() + " REM");

        } catch (SQLException e) {
            log.severe(">>> Failed to push deque - dumping content <<<");
            while (!deque.isEmpty()) {
                Claim claim = deque.poll();
                log.warning(">>> [" + claim.getChunk().getX() + "|" + claim.getChunk().getZ() + "] -> " + (claim.getPlayer() == null ? "null" : claim.getPlayer().getUniqueId()));
            }
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
        connect();

        try(ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM claims WHERE x =" + chunk.getX() + " AND z = " + chunk.getZ() + ";")) {
            if(resultSet.next()) {
                Location location = new Location(chunk.getWorld(), resultSet.getInt(4), resultSet.getInt(5), resultSet.getInt(6));
                new Claim(Bukkit.getPlayer(UUID.fromString(resultSet.getString(3))), location);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
