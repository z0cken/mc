package com.z0cken.mc.progression;

import com.z0cken.mc.core.Database;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DatabaseHelper {

    private static final Database DATABASE = Database.MAIN;
    private static final Logger log = PCS_Progression.getInstance().getLogger();
    private static final Set<String> columns;

    private DatabaseHelper() {}

    static {
        setupTables();
        columns = getColumns();

        new BukkitRunnable() {
            @Override
            public void run() {
                push();
            }
        }.runTaskTimerAsynchronously(PCS_Progression.getInstance(), 100, Math.max(PCS_Progression.getInstance().getConfig().getInt("push-interval") * 20, 100));
    }

    private static void setupTables() {
        try (Connection connection = DATABASE.getConnection();
             Statement statement = connection.createStatement()) {
            String prefix = "CREATE TABLE IF NOT EXISTS ";

            statement.addBatch(prefix + "progression (player CHAR(36) PRIMARY KEY);");
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Set<String> getColumns() {
        Set<String> set = new HashSet<>();
        try (Connection connection = DATABASE.getConnection();
             ResultSet resultSet = connection.createStatement().executeQuery("SHOW COLUMNS FROM progression")) {

            while (resultSet.next()) set.add(resultSet.getString(1));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return set;
    }

    static void push() {

        List<String> content = new ArrayList<>();
        PCS_Progression.progressionData.forEach((key, value) -> content.addAll(value.entrySet().stream().map(entry -> key.getUniqueId() + " > " + entry.getKey() + " = " + entry.getValue()).collect(Collectors.toList())));

        try (Connection connection = DATABASE.getConnection();
            Statement statement = connection.createStatement()) {

            Iterator<Map.Entry<Player, Map<String , Integer>>> iterator = PCS_Progression.progressionData.entrySet().iterator();

            while(iterator.hasNext()) {
                Map.Entry<Player, Map<String, Integer>> entry = iterator.next();
                Player player = entry.getKey();

                for(Map.Entry<String, Integer> subEntry : entry.getValue().entrySet()) {
                    addColumnIfNotExists(subEntry.getKey());
                    String s = subEntry.getKey() + " = " + subEntry.getValue();
                    statement.addBatch("INSERT INTO progression SET " + s + ", player = '" + player.getUniqueId() + "' ON DUPLICATE KEY UPDATE " + s + ";");
                }

                if(!player.isOnline()) iterator.remove();
            }

            statement.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
            log.severe(">>> Failed to push progression data - dumping content <<<");
            content.forEach(log::severe);
        }
    }

    static void addColumnIfNotExists(String name) throws SQLException {
        if(columns.contains(name)) return;

        try (Connection connection = DATABASE.getConnection();
            Statement statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE progression ADD COLUMN " + name + " INT DEFAULT 0;");
        } catch (SQLException e) {
            throw e;
        }

        columns.add(name);
    }

    static Map<String, Integer> getProgression(Player player) {
        Map<String, Integer> map;

        map = PCS_Progression.progressionData.getOrDefault(player, null);

        if(map == null) map = Collections.synchronizedMap(new HashMap<>());
        else return map;

        try (Connection connection = DATABASE.getConnection();
             ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM progression WHERE player = '" + player.getUniqueId() + "' ;")) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            while (resultSet.next()) {
                for(int i = 2; i <= metaData.getColumnCount(); i++) {
                    Bukkit.broadcastMessage(metaData.getColumnName(i) + " : " + resultSet.getInt(i));
                    map.put(metaData.getColumnName(i), resultSet.getInt(i));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return map;
    }


}
