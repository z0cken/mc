package com.z0cken.mc.core;

import java.sql.*;
import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("unused")
public class FriendsAPI {

    private static final Database DATABASE = Database.MAIN;

    static void setupTables() {
        try(Connection connection = DATABASE.getConnection();
            Statement statement = connection.createStatement()) {

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS friends (p1 CHAR(36), p2 CHAR(36), created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, CONSTRAINT pk PRIMARY KEY (p1, p2));");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean areFriends(UUID first, UUID second) throws SQLException {
        Connection connection = DATABASE.getConnection();
        PreparedStatement pstmt = connection.prepareStatement("SELECT COUNT(*) FROM friends WHERE (p1 = ? AND p2 = ?) OR (p1 = ? AND p2 = ?)");

        pstmt.setString(1, first.toString());
        pstmt.setString(2, second.toString());
        pstmt.setString(3, second.toString());
        pstmt.setString(4, first.toString());

        ResultSet resultSet = pstmt.executeQuery();

        if (resultSet.first()) {
            boolean result = resultSet.getInt(1) == 0;

            try {
                connection.close();
                pstmt.close();
                resultSet.close();
            } catch (SQLException ignore) {}

            return result;
        }

        return false;
    }

    public static HashMap<UUID, Timestamp> getFriends(UUID uuid) throws SQLException {
        HashMap<UUID, Timestamp> friends = new HashMap<>();

        Connection connection = DATABASE.getConnection();
        PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM friends WHERE p1 = ? OR p2 = ?");
        pstmt.setString(1, uuid.toString());
        pstmt.setString(2, uuid.toString());
        ResultSet resultSet = pstmt.executeQuery();

        while (resultSet.next()) {
            int i = uuid.equals(UUID.fromString(resultSet.getString(1))) ? 2 : 1;
            friends.put(UUID.fromString(resultSet.getString(i)), resultSet.getTimestamp(3));
        }

        try {
            connection.close();
            pstmt.close();
            resultSet.close();
        } catch (SQLException ignore) {}

        return friends;
    }

    public static void makeFriends(UUID first, UUID second) throws SQLException {
        Connection connection = DATABASE.getConnection();
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO friends(p1, p2) VALUES (?, ?)");
        pstmt.setString(1, first.toString());
        pstmt.setString(2, second.toString());
        pstmt.executeUpdate();

        try {
            connection.close();
            pstmt.close();
        } catch (SQLException ignore) {}
    }

}
