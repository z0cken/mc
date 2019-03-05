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
        } catch (SQLException e) {
            throw e;
        }

        return null;
    }

    static long getInvited(@Nonnull UUID uuid) throws SQLException {
        try(Connection connection = DATABASE.getConnection();
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT invited FROM guests WHERE guest = '" + uuid.toString() + "'")) {
            if(resultSet.next()) return resultSet.getLong(1) * 1000L;
        } catch (SQLException e) {
            throw e;
        }

        return 0;
    }

    static String getUsername(@Nonnull UUID uuid) throws SQLException {
        try(Connection connection = DATABASE.getConnection();
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT username FROM verified WHERE player = '" + uuid.toString() + "'")) {
            if(resultSet.next()) return resultSet.getString(1);
        } catch (SQLException e) {
            throw e;
        }

        return null;
    }

    static boolean isAnonymized(@Nonnull UUID uuid) throws SQLException {
        try(Connection connection = DATABASE.getConnection();
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT anonymized FROM verified WHERE player = '" + uuid.toString() + "'")) {
            if(resultSet.next()) return resultSet.getBoolean(1);
        } catch (SQLException e) {
            throw e;
        }
        return true;
    }

    static void awardBadge(@Nonnull UUID uuid, @Nonnull Persona.Badge badge) throws SQLException {
        try (Connection connection = DATABASE.getConnection();
             PreparedStatement pstmt = connection.prepareStatement("INSERT IGNORE INTO badges VALUES (?, ?)")) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, badge.name());

        } catch (SQLException e) {
            throw e;
        }
    }

    static SortedSet<Persona.Badge> getBadges(@Nonnull UUID uuid) throws SQLException {
        SortedSet<Persona.Badge> set = new TreeSet<>();
        try(Connection connection = DATABASE.getConnection();
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM badges WHERE player = '" + uuid.toString() + "'")) {
            while(resultSet.next()) set.add(Persona.Badge.valueOf(resultSet.getString(1)));
        } catch (SQLException e) {
            throw e;
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
