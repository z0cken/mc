package com.z0cken.mc.checkpoint;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import java.sql.*;
import java.util.UUID;

public class DatabaseHelper {

    private static Connection connection;

    DatabaseHelper() {
        try {
            Class.forName("com.mysql.jdbc.Driver");

            Configuration config = PCS_Checkpoint.getInstance().getConfig().getSection("database");

            String ip = config.getString("ip");
            String dbname = config.getString("name");
            String user = config.getString("user");
            String password = config.getString("password");

            String url = "jdbc:mysql://%s/%s?" + "user=%s&password=%s";
            connection = DriverManager.getConnection(String.format(url, ip, dbname, user, password));

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }


        try (Statement statement = connection.createStatement()) {
            String prefix = "CREATE TABLE IF NOT EXISTS ";
            statement.executeUpdate(prefix + "pending (player CHAR(36) PRIMARY KEY, code CHAR(32) NOT NULL);");
            statement.executeUpdate(prefix + "verified (player CHAR(36) PRIMARY KEY, username VARCHAR(32) NOT NULL, invites INT DEFAULT 0);");
            statement.executeUpdate(prefix + "guests (guest CHAR(36) PRIMARY KEY, host CHAR(36) NOT NULL);");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    static Persona getPersona(UUID uuid) {

        Persona persona = null;

        try(Statement statement = connection.createStatement();
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

        try(PreparedStatement pstmt = connection.prepareStatement("SELECT player FROM pending WHERE code = ?")) {

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
                PCS_Checkpoint.getInstance().getLogger().warning(name + "tried to verify " + uuid + " but is already linked!");
                return;
            }

            try(Statement statement = connection.createStatement()) {
                statement.executeUpdate("INSERT IGNORE INTO verified(player, username) VALUES ('" + uuid.toString() + "', '" + name + "');");
                statement.executeUpdate("DELETE FROM pending WHERE player = '" + uuid.toString() + "'");
                statement.executeUpdate("DELETE FROM guests WHERE guest = '" + uuid.toString() + "'");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            PCS_Checkpoint.getInstance().checkPlayer(uuid);
            ProxiedPlayer player = PCS_Checkpoint.getInstance().getProxy().getPlayer(uuid);
            if(player != null) {
                player.sendMessage(new Util.MessageBuilder().digest(PCS_Checkpoint.getInstance().getConfig().getString("messages.verify.success")));
            }
        }
    }

    private static boolean usernameExists(String name) {
        try(ResultSet resultSet = connection.createStatement().executeQuery("SELECT player FROM verified WHERE username = '" + name + "'")) {
            if(resultSet.next()) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isGuest(UUID uuid) {
        try(ResultSet resultSet = connection.createStatement().executeQuery("SELECT guest FROM guests WHERE guest = '" + uuid.toString() + "'")) {
            if(resultSet.next()) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isVerified(UUID uuid) {
        try(ResultSet resultSet = connection.createStatement().executeQuery("SELECT player FROM verified WHERE player = '" + uuid.toString() + "'")) {
            if(resultSet.next()) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    static void invite(UUID guest, UUID host) {

        try(Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT IGNORE INTO guests VALUES ('" + guest.toString() + "', '" + host.toString() + "');");
            statement.executeUpdate("UPDATE verified SET invites = invites - 1 WHERE player = '" + host.toString() + "'");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        PCS_Checkpoint.getInstance().checkPlayer(guest);

    }

    static int getInvites(UUID uuid) {

        try(ResultSet resultSet = connection.createStatement().executeQuery("SELECT invites FROM verified WHERE player = '" + uuid.toString() + "'")) {
            resultSet.next();
            return resultSet.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    static void insertPending(UUID uuid, String hash) {

        try(Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT IGNORE INTO pending VALUES ('" + uuid.toString() + "', '" + hash + "');");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
