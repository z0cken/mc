package com.z0cken.mc.checkpoint;

import com.z0cken.mc.core.Database;
import com.z0cken.mc.core.persona.PersonaAPI;
import com.z0cken.mc.core.util.MessageBuilder;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

class DatabaseHelper {

    private static final Logger log = PCS_Checkpoint.getInstance().getLogger();

    private DatabaseHelper() {}

    static void setupTables() {

        try (Connection connection = Database.MAIN.getConnection();
             Statement statement = connection.createStatement()) {
            String prefix = "CREATE TABLE IF NOT EXISTS ";

            statement.addBatch(prefix + "pending (player CHAR(36) PRIMARY KEY, code CHAR(35) NOT NULL);");
            statement.addBatch(prefix + "verified (player CHAR(36) PRIMARY KEY, username VARCHAR(32) NOT NULL, invites INT DEFAULT 0, anonymized BOOLEAN DEFAULT FALSE);");
            statement.addBatch(prefix + "guests (guest CHAR(36) PRIMARY KEY, host CHAR(36) NOT NULL, invited INT(10) NOT NULL);");
            statement.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void verify(String message, String name) {

        UUID uuid;

        try (Connection connection = Database.MAIN.getConnection();
             PreparedStatement pstmt = connection.prepareStatement("SELECT player FROM pending WHERE code = ?")) {

            pstmt.setString(1, message);
            ResultSet resultSet = pstmt.executeQuery();

            if (resultSet.next()) uuid = UUID.fromString(resultSet.getString(1));
            else return;

        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        //Double verification alert
        if (isVerified(name)) {
            log.warning(String.format("%s tried to verify %s but is already linked!", name, uuid));
            return;
        }

        try (Connection connection = Database.MAIN.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT IGNORE INTO verified(player, username, invites) VALUES ('" + uuid + "', '" + name + "', 0);");
            statement.executeUpdate("DELETE FROM pending WHERE player = '" + uuid.toString() + "'");
            statement.executeUpdate("DELETE FROM guests WHERE guest = '" + uuid.toString() + "'");
            log.info(String.format("Successfully verified %s as %s", uuid, name));
        } catch (SQLException e) {
            e.printStackTrace();
            log.severe(String.format("A database error occurred while verifying %s", uuid));
        }

        PersonaAPI.invalidate(uuid);
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);

            PersonaAPI.getPersona(uuid).getBoardProfile().thenAcceptAsync(profile -> {
                if(player != null) {
                    final TextComponent mark = new TextComponent(profile.getMark().getTitle());
                    mark.setColor(profile.getMark().getColor());

                    MessageBuilder builder = MessageBuilder.DEFAULT.define("MARK", mark)
                            .define("AMOUNT", Integer.toString(profile.getMark().getStartInvites())).define("NAME", profile.getName());

                    for(String s : PCS_Checkpoint.getConfig().getStringList("messages.verify.success")) {
                        player.sendMessage(builder.build(s));
                    }
                }

                try {
                    giveInvites(uuid, profile.getMark().getStartInvites());
                } catch (SQLException e) {
                    e.printStackTrace();
                    log.severe(String.format("Failed to give %d invites to %s", profile.getMark().getStartInvites(), uuid));
                }
            });

        PCS_Checkpoint.getInstance().checkPlayer(uuid);
    }

    private static boolean isVerified(String name) {

        try (Connection connection = Database.MAIN.getConnection();
             ResultSet resultSet = connection.createStatement().executeQuery("SELECT player FROM verified WHERE username = '" + name + "'")) {
            if (resultSet.next()) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    static boolean isGuest(UUID uuid) {

        try (Connection connection = Database.MAIN.getConnection();
             ResultSet resultSet = connection.createStatement().executeQuery("SELECT guest FROM guests WHERE guest = '" + uuid.toString() + "'")) {
            if (resultSet.next()) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    static boolean isVerified(UUID uuid) {

        try (Connection connection = Database.MAIN.getConnection();
             ResultSet resultSet = connection.createStatement().executeQuery("SELECT player FROM verified WHERE player = '" + uuid.toString() + "'")) {
            if (resultSet.next()) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    static void invite(UUID guest, UUID host) {

        try (Connection connection = Database.MAIN.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT IGNORE INTO guests VALUES ('" + guest.toString() + "','" + host.toString() + "','" + (int) (System.currentTimeMillis() / 1000L) + "');");
            statement.executeUpdate("UPDATE verified SET invites = invites - 1 WHERE player = '" + host.toString() + "'");
            //setPrimaryGroup(Shadow.NAME.g, PCS_Checkpoint.getConfig().getString("invite-group"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static int getInvites(UUID uuid) {

        try (Connection connection = Database.MAIN.getConnection();
             ResultSet resultSet = connection.createStatement().executeQuery("SELECT invites FROM verified WHERE player = '" + uuid.toString() + "'")) {
            resultSet.next();
            return resultSet.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    static void giveInvites(UUID uuid, int invites) throws SQLException {

        try (Connection connection = Database.MAIN.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE verified SET invites = invites + " + invites + " WHERE player = '" + uuid.toString() + "'");
        }
    }

    static void insertPending(UUID uuid, String hash) {

        try (Connection connection = Database.MAIN.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT IGNORE INTO pending VALUES ('" + uuid.toString() + "', '" + hash + "');");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Map<UUID, Timestamp> getGuests(UUID uuid) throws SQLException {

        Map<UUID, Timestamp> guests = new HashMap<>();

        try (Connection connection = Database.MAIN.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM guests WHERE host = ?;")) {

            pstmt.setString(1, uuid.toString());
            ResultSet resultSet = pstmt.executeQuery();

            while (resultSet.next()) guests.put(UUID.fromString(resultSet.getString(1)), resultSet.getTimestamp(3));
        }

        return guests;
    }
}
