package com.z0cken.mc.core.persona;

import com.z0cken.mc.core.CoreBridge;
import com.z0cken.mc.core.Database;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

final class DatabaseHelper {

    private static final Logger log = CoreBridge.getPlugin().getLogger();
    private static final Database DATABASE = Database.MAIN;

    private DatabaseHelper() {}

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
}
