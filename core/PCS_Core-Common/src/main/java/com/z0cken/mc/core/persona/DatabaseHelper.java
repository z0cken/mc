package com.z0cken.mc.core.persona;

import com.z0cken.mc.core.CoreBridge;
import com.z0cken.mc.core.Database;

import javax.annotation.Nonnull;
import java.sql.*;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Logger;

final class DatabaseHelper {

    private static final Logger log = CoreBridge.getPlugin().getLogger();
    private static final Database DATABASE = Database.MAIN;

    static {
        setupTables();
    }

    private DatabaseHelper() {}

    private static void setupTables() {
        try (Connection connection = DATABASE.getConnection();
             Statement statement = connection.createStatement()) {
            String prefix = "CREATE TABLE IF NOT EXISTS ";

            statement.addBatch(prefix + "badges (player CHAR(36) NOT NULL, badge VARCHAR(50) NOT NULL, CONSTRAINT pk PRIMARY KEY (player, badge));");
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static String getHost(@Nonnull UUID uuid) throws SQLException {
        try(Connection connection = DATABASE.getConnection();
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT host FROM guests WHERE guest = '" + uuid.toString() + "'")) {
            if(resultSet.next()) return resultSet.getString(1);
        }

        return null;
    }

    static long getInvited(@Nonnull UUID uuid) throws SQLException {
        try(Connection connection = DATABASE.getConnection();
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT invited FROM guests WHERE guest = '" + uuid.toString() + "'")) {
            if(resultSet.next()) return resultSet.getLong(1) * 1000L;
        }

        return 0;
    }

    static String getUsername(@Nonnull UUID uuid) throws SQLException {
        try(Connection connection = DATABASE.getConnection();
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT username FROM verified WHERE player = '" + uuid.toString() + "'")) {
            if(resultSet.next()) return resultSet.getString(1);
        }

        return null;
    }

    static boolean isAnonymized(@Nonnull UUID uuid) throws SQLException {
        try(Connection connection = DATABASE.getConnection();
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT anonymized FROM verified WHERE player = '" + uuid.toString() + "'")) {
            if(resultSet.next()) return resultSet.getBoolean(1);
        }
        return true;
    }

    static void addBadge(@Nonnull UUID uuid, @Nonnull Persona.Badge badge) throws SQLException {
        try (Connection connection = DATABASE.getConnection();
             PreparedStatement pstmt = connection.prepareStatement("INSERT IGNORE INTO badges VALUES (?, ?)")) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, badge.name());
            pstmt.executeUpdate();
        }
    }

    static void removeBadge(@Nonnull UUID uuid, @Nonnull Persona.Badge badge) throws SQLException {
        try (Connection connection = DATABASE.getConnection();
             PreparedStatement pstmt = connection.prepareStatement("DELETE FROM badges WHERE player = ? AND badge = ?")) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, badge.name());
            pstmt.executeUpdate();
        }
    }

    static SortedSet<Persona.Badge> getBadges(@Nonnull UUID uuid) throws SQLException {
        SortedSet<Persona.Badge> set = new TreeSet<>();
        try(Connection connection = DATABASE.getConnection();
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT badge FROM badges WHERE player = '" + uuid.toString() + "'")) {
            while(resultSet.next()) {
                final String badge = resultSet.getString(1);
                try {
                    set.add(Persona.Badge.valueOf(badge));
                } catch (IllegalArgumentException e) {
                    CoreBridge.getPlugin().getLogger().warning("Encountered an unknown badge: " + badge);
                }
            }
        }

        return set;
    }

    public static void setAnonymized(UUID uuid, boolean anonymized) {
        try (Connection connection = Database.MAIN.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE verified SET anonymized = " + Boolean.toString(anonymized).toUpperCase() + " WHERE player = '" + uuid.toString() + "'");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
