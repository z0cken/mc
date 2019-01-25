package com.z0cken.mc.checkpoint;

import com.z0cken.mc.core.Database;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Logger;

class DatabaseHelper {

    private static final Logger log = PCS_Checkpoint.getInstance().getLogger();

    private DatabaseHelper() {}

    static void setupTables() {

        try(Connection connection = Database.MAIN.getConnection();
            Statement statement = connection.createStatement()) {
            String prefix = "CREATE TABLE IF NOT EXISTS ";

            statement.addBatch(prefix + "pending (player CHAR(36) PRIMARY KEY, code CHAR(32) NOT NULL);");
            statement.addBatch(prefix + "verified (player CHAR(36) PRIMARY KEY, username VARCHAR(32) NOT NULL, invites INT DEFAULT 0, anonymous BOOLEAN DEFAULT FALSE);");
            statement.addBatch(prefix + "guests (guest CHAR(36) PRIMARY KEY, host CHAR(36) NOT NULL, invited INT(10) NOT NULL);");
            statement.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static Persona getPersona(UUID uuid) {
        Persona persona = null;

        try(Connection connection = Database.MAIN.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT username FROM verified WHERE player = '" + uuid.toString() + "'")) {
            if(resultSet.next()) persona = new Persona(resultSet.getString(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return persona;
    }

    static void verify(String message, String name) {

        boolean valid = false;
        UUID uuid = null;

        try(Connection connection = Database.MAIN.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("SELECT player FROM pending WHERE code = ?")) {

            pstmt.setString(1, message);
            ResultSet resultSet = pstmt.executeQuery();

            valid = resultSet.next();
            if(valid) uuid = UUID.fromString(resultSet.getString(1));

            resultSet.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(valid) {
            //Double verification alert
            if(usernameExists(name)) {
                log.warning(name + "tried to verify " + uuid + " but is already linked!");
                return;
            }

            try(Connection connection = Database.MAIN.getConnection();
                Statement statement = connection.createStatement()) {
                statement.executeUpdate("INSERT IGNORE INTO verified(player, username) VALUES ('" + uuid.toString() + "', '" + name + "');");
                statement.executeUpdate("DELETE FROM pending WHERE player = '" + uuid.toString() + "'");
                statement.executeUpdate("DELETE FROM guests WHERE guest = '" + uuid.toString() + "'");
            } catch (SQLException e) {
                e.printStackTrace();
            }

            PCS_Checkpoint.getInstance().checkPlayer(uuid, true);
        }
    }

    private static boolean usernameExists(String name) {

        try(Connection connection = Database.MAIN.getConnection();
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT player FROM verified WHERE username = '" + name + "'")) {
            if(resultSet.next()) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    static boolean isGuest(UUID uuid) {

        try(Connection connection = Database.MAIN.getConnection();
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT guest FROM guests WHERE guest = '" + uuid.toString() + "'")) {
            if(resultSet.next()) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    static boolean isVerified(UUID uuid) {

        try(Connection connection = Database.MAIN.getConnection();
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT player FROM verified WHERE player = '" + uuid.toString() + "'")) {
            if(resultSet.next()) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    static void invite(UUID guest, UUID host) {

        try(Connection connection = Database.MAIN.getConnection();
            Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT IGNORE INTO guests VALUES ('" + guest.toString() + "','" + host.toString() + "','" + (int) (System.currentTimeMillis() / 1000L) + "');");
            statement.executeUpdate("UPDATE verified SET invites = invites - 1 WHERE player = '" + host.toString() + "'");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        PCS_Checkpoint.getInstance().checkPlayer(guest, true);

    }

    static int getInvites(UUID uuid) {

        try(Connection connection = Database.MAIN.getConnection();
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT invites FROM verified WHERE player = '" + uuid.toString() + "'")) {
            resultSet.next();
            return resultSet.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    static void giveInvites(UUID uuid, int invites) {

        try(Connection connection = Database.MAIN.getConnection();
            Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE verified SET invites = invites + " + invites + " WHERE player = '" + uuid.toString() + "'");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void insertPending(UUID uuid, String hash) {

        try(Connection connection = Database.MAIN.getConnection();
            Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT IGNORE INTO pending VALUES ('" + uuid.toString() + "', '" + hash + "');");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static boolean isAnonymous(UUID uuid) {

        try(Connection connection = Database.MAIN.getConnection();
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT anonymous FROM verified WHERE player = '" + uuid.toString() + "'")) {
            if(resultSet.next()) return resultSet.getBoolean(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    static boolean setAnonymous(UUID uuid, boolean anonymous) {

        try(Connection connection = Database.MAIN.getConnection();
            Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE verified SET anonymous = " + Boolean.toString(anonymous).toUpperCase() + " WHERE player = '" + uuid.toString() + "'");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

}
