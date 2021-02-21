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

            statement.addBatch(prefix + "claims (world VARCHAR(64), x INT NOT NULL, z INT NOT NULL, player CHAR(36) NOT NULL, block_x INT NOT NULL , block_y INT NOT NULL , block_z INT NOT NULL, material VARCHAR(50) NOT NULL, CONSTRAINT pk PRIMARY KEY (world, x, z));");
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
             PreparedStatement statementAdd = connection.prepareStatement("INSERT INTO claims VALUES(?, ?, ?, ?, ?, ?, ?, ?);");
             PreparedStatement statementRem = connection.prepareStatement("DELETE FROM claims WHERE world = ? AND x = ? AND z = ?")) {

            while (!queue.isEmpty()) {
                Claim claim = queue.peek();

                boolean hasOwner = claim.getOwner() != null;

                PreparedStatement pstmt = hasOwner ? statementAdd : statementRem;
                int i = 1;
                pstmt.setString(i++, claim.getWorld().getName());
                pstmt.setInt(i++, claim.getChunk().getX());
                pstmt.setInt(i++, claim.getChunk().getZ());
                if (hasOwner) {
                    pstmt.setString(i++, claim.getOwner().getUniqueId().toString());
                    pstmt.setInt(i++, claim.getBaseBlock().getX());
                    pstmt.setInt(i++, claim.getBaseBlock().getY());
                    pstmt.setInt(i++, claim.getBaseBlock().getZ());
                    pstmt.setString(i++, claim.getBaseMaterial().name());
                }

                pstmt.executeUpdate();
                if(hasOwner) add++; else rem++;
                queue.remove();
            }

            log.info("[PUSH] ADD " + add + " | " + rem + " REM");

        } catch (SQLException e) {
            e.printStackTrace();
            log.severe(">>> Failed to push queue - dumping content <<<");
            queue.stream().map(claim -> ">>> " + claim.getName() + " -> " + (claim.getOwner() == null ? "null" : claim.getOwner().getUniqueId())).collect(Collectors.toList()).forEach(log::severe);
        }
    }

    static Claim getClaim(@Nonnull Chunk chunk) {

        try(Connection connection = DATABASE.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM claims WHERE world = ? AND x = ? AND z = ?;");) {

            pstmt.setString(1, chunk.getWorld().getName());
            pstmt.setInt(2, chunk.getX());
            pstmt.setInt(3, chunk.getZ());

            final ResultSet resultSet = pstmt.executeQuery();
            if(resultSet.next()) {
                Location location = new Location(chunk.getWorld(), resultSet.getInt(5), resultSet.getInt(6), resultSet.getInt(7));
                String materialName = resultSet.getString(8);

                Material baseMaterial;
                try {
                    baseMaterial = Material.valueOf(materialName);
                } catch (IllegalArgumentException e) {
                    PCS_Claim.getInstance().getLogger().warning(String.format("Invalid material %s for chunk: %s", materialName, location.getChunk()));
                    baseMaterial = Material.AIR;
                }
                return new Claim(UUID.fromString(resultSet.getString(4)), location, baseMaterial);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    static Map<ChunkPosition, Claim> getClaims(@Nonnull Set<ChunkPosition> set) {
        final Map<ChunkPosition, Claim> result = new HashMap<>();

        try(Connection connection = DATABASE.getConnection();
            final ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM claims;")) {
            while(resultSet.next()) {
                final ChunkPosition chunkPosition = new ChunkPosition(Bukkit.getWorld(resultSet.getString(1)), resultSet.getInt(2), resultSet.getInt(3));

                if(set.contains(chunkPosition)) {
                    Location location = new Location(chunkPosition.getWorld(), resultSet.getInt(5), resultSet.getInt(6), resultSet.getInt(7));
                    result.put(chunkPosition, new Claim(UUID.fromString(resultSet.getString(4)), location, Material.valueOf(resultSet.getString(8))));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        result.forEach((chunkPosition, claim) -> System.out.println(chunkPosition + " : " + claim.getOwner().getUniqueId()));
        return result;
    }

    static Set<Claim> getClaims(@Nonnull World world, @Nonnull OfflinePlayer player) {

        final Set<Claim> claims = new HashSet<>();
        try(Connection connection = DATABASE.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM claims WHERE player = ? AND world = ?;")) {
            pstmt.setString(1, player.getUniqueId().toString());
            pstmt.setString(2, world.getName());
            ResultSet resultSet = pstmt.executeQuery();

            while(resultSet.next()) {
                Location location = new Location(world, resultSet.getInt(5), resultSet.getInt(6), resultSet.getInt(7));
                claims.add(new Claim(player.getUniqueId(), location, Material.valueOf(resultSet.getString(8))));
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
            pstmt.setInt(2, claim.getChunkPosition().getX());
            pstmt.setInt(3, claim.getChunkPosition().getZ());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
