package com.z0cken.mc.metro;

import com.z0cken.mc.core.Database;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class DatabaseHelper {
    private static final Database DATABASE = Database.MAIN;

    static {
        setupTables();
    }

    private DatabaseHelper() {}

    static void setupTables() {
        try (Connection connection = DATABASE.getConnection();
             Statement statement = connection.createStatement()) {
            String prefix = "CREATE TABLE IF NOT EXISTS ";

            statement.addBatch(prefix + "metro_stations (station VARCHAR(40) PRIMARY KEY, supply INT DEFAULT 0);");
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(PCS_Metro.getInstance());
        }
    }

    static int getSupply(String id) {

        try(Connection connection = DATABASE.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("SELECT supply FROM metro_stations WHERE station = ?;")) {
            pstmt.setString(1, id);
            ResultSet resultSet = pstmt.executeQuery();
            if(resultSet.next()) return  resultSet.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(PCS_Metro.getInstance());
        }

        return 0;
    }

    static void setSupply(String id, int amount) {
        try(Connection connection = DATABASE.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO metro_stations VALUES (?, ?) ON DUPLICATE KEY UPDATE supply = ?")) {
            pstmt.setString(1, id);
            pstmt.setInt(2, amount);
            pstmt.setInt(3, amount);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(PCS_Metro.getInstance());
        }
    }

    static void decrementAll(int amount) {
        try(Connection connection = DATABASE.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("UPDATE metro_stations SET supply = supply - ? WHERE supply >= ?")) {
            pstmt.setInt(1, amount);
            pstmt.setInt(2, amount);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            PCS_Metro.getInstance().getLogger().severe("Failed to decrement entire supply by " + amount);
            Bukkit.getPluginManager().disablePlugin(PCS_Metro.getInstance());
        }
    }

    static Map<OfflinePlayer, Integer> getLeaderboard(int limit) throws SQLException {
        Map<OfflinePlayer, Integer> map = new LinkedHashMap<>();

        try (Connection connection = DATABASE.getConnection();
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery("SELECT * FROM metro_players ORDER BY xp LIMIT " + limit + ";");
            while (resultSet.next())
                map.put(Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString(1))), resultSet.getInt(2));
        }

        return map;
    }
}
