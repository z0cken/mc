package com.z0cken.mc.metro;

import com.z0cken.mc.core.Database;
import org.bukkit.Bukkit;

import java.sql.*;

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

            statement.addBatch(prefix + "metro_stations (station VARCHAR(40) PRIMARY KEY, supply INT DEFAULT 0 CHECK(supply >= 0 AND supply <= 84));");
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

    static void addSupply(String id, int amount) {
        try(Connection connection = DATABASE.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO metro_stations VALUES (?, ?) ON DUPLICATE KEY UPDATE supply = supply + ?")) {
            pstmt.setString(1, id);
            pstmt.setInt(2, amount);
            pstmt.setInt(3, amount);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(PCS_Metro.getInstance());
        }
    }

    static void decrementAll() {
        try(Connection connection = DATABASE.getConnection();
            Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE metro_stations SET supply = supply - 1 WHERE supply > 0");
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(PCS_Metro.getInstance());
        }
    }
}
