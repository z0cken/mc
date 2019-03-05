package com.z0cken.mc.essentials.modules;

import com.z0cken.mc.claim.ChunkCoordinate;
import com.z0cken.mc.core.Database;
import com.z0cken.mc.core.bukkit.Menu;
import com.z0cken.mc.essentials.PCS_Essentials;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public final class ModuleErosion extends Module implements Listener, CommandExecutor {

    private static final Set<Material> MATERIALS = Set.of(
            Material.GOLD_ORE,
            Material.EMERALD_ORE,
            Material.DIAMOND_ORE,
            Material.LAPIS_ORE
    );

    private static final Queue<AbstractMap.SimpleEntry<ChunkCoordinate, Material>> eroded = new ConcurrentLinkedQueue<>();
    private static final Queue<AbstractMap.SimpleEntry<ChunkCoordinate, Material>> restored = new ConcurrentLinkedQueue<>();
    private static int pushInterval;
    private World world;

    ModuleErosion(String configPath) {
        super(configPath);
        registerCommand("erosion");
    }

    @Override
    protected void load() {
        try {
            DatabaseHelper.setupTables();
        } catch (SQLException e) {
            e.printStackTrace();
            disable();
        }

        pushInterval = getConfig().getInt("push-interval");
        world = Bukkit.getWorld(getConfig().getString("world"));
    }

    @Override
    protected void onDisable() {
        DatabaseHelper.push();
    }

    @Override
    public boolean onCommand(CommandSender commandsender, Command command, String s, String[] args) {
        if (command.getName().equalsIgnoreCase("erosion")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("query")) {
                    if(args.length > 1) {

                    } else {
                        Map<ChunkCoordinate, Map<Material, Integer>> result;
                        try {
                            result = DatabaseHelper.queryTop(9);
                        } catch (SQLException e) {
                            e.printStackTrace();
                            commandsender.sendMessage("Database Error!");
                            return true;
                        }

                        if(commandsender instanceof Player) {
                            Menu menu = new Menu(PCS_Essentials.getInstance(), 1, "Erosion");

                            List<ItemStack> list = new ArrayList<>();

                            result.forEach(((chunk, map) -> {
                                final int x = chunk.getX(), z = chunk.getZ();

                                Menu.Button button = new Menu.Button((menu1, button1, player, event) -> {
                                    player.teleport(new Location(world, x << 4, world.getHighestBlockYAt(x, z), z));
                                }, Material.BEDROCK, 1);

                                ItemMeta itemMeta = button.getItemMeta();
                                itemMeta.setDisplayName(ChatColor.RESET + "[" + x + "|" + z + "]");
                                itemMeta.setLore(map.entrySet().stream().map(entry -> ChatColor.RESET + entry.getKey().name() + " : " + entry.getValue()).collect(Collectors.toList()));
                                button.setItemMeta(itemMeta);
                                list.add(button);
                            }));

                            list.forEach(menu::addItem);
                            ((Player)commandsender).openInventory(menu);

                        }
                    }
                } else if (args[0].equalsIgnoreCase("restore")) {

                }
            } else {
                //Help
            }

        }

        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!event.getBlock().getWorld().equals(world) || !MATERIALS.contains(event.getBlock().getType())) return;
        eroded.add(new AbstractMap.SimpleEntry<>(new ChunkCoordinate(event.getBlock().getChunk()), event.getBlock().getType()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (!event.getBlockPlaced().getWorld().equals(world) || !MATERIALS.contains(event.getBlockPlaced().getType())) return;
        restored.add(new AbstractMap.SimpleEntry<>(new ChunkCoordinate(event.getBlock().getChunk()), event.getBlock().getType()));
    }

    private static class DatabaseHelper {
        private static final Database DATABASE = Database.MAIN;

        static {
            new BukkitRunnable() {
                @Override
                public void run() {
                    push();
                }
            }.runTaskTimerAsynchronously(PCS_Essentials.getInstance(), 60 * 20, Math.max(pushInterval * 20, 100));
        }

        private DatabaseHelper() {
        }

        static void setupTables() throws SQLException {
            try (Connection connection = DATABASE.getConnection();
                 Statement statement = connection.createStatement()) {

                statement.executeUpdate(  "CREATE TABLE IF NOT EXISTS erosion (x INT NOT NULL, z INT NOT NULL, CONSTRAINT pk PRIMARY KEY (x, z));");
                for(Material material : MATERIALS) {
                    statement.executeUpdate("ALTER TABLE erosion ADD COLUMN IF NOT EXISTS " + material.name().toLowerCase() + " INT DEFAULT 0;");
                }
            }
        }

        private static void push() {

            try (Connection connection = DATABASE.getConnection();
                 Statement erode = connection.createStatement();
                 Statement restore = connection.createStatement()) {

                while (!eroded.isEmpty()) {
                    AbstractMap.SimpleEntry<ChunkCoordinate, Material> entry = eroded.poll();
                    String s = entry.getValue().name().toLowerCase() + " = " + entry.getValue().name().toLowerCase() + " - 1";
                    erode.addBatch("INSERT INTO erosion SET " + s + ", x = " + entry.getKey().getX() + ", z = " + entry.getKey().getZ() + " ON DUPLICATE KEY UPDATE " + s + ";");
                }

                while (!restored.isEmpty()) {
                    AbstractMap.SimpleEntry<ChunkCoordinate, Material> entry = restored.poll();
                    String s = entry.getValue().name().toLowerCase() + " = " + entry.getValue().name().toLowerCase() + " + 1";
                    restore.addBatch("INSERT INTO erosion SET " + s + ", x = " + entry.getKey().getX() + ", z = " + entry.getKey().getZ() + " ON DUPLICATE KEY UPDATE " + s + ";");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                getLogger().severe(">>> Failed to push erosion queue <<<");
            }
        }


        static Map<ChunkCoordinate, Map<Material, Integer>> queryTop(int limit) throws SQLException {
            Map<ChunkCoordinate, Map<Material, Integer>> map = new LinkedHashMap<>();

            String materials = "";
            for(Material mat : MATERIALS) materials += mat.name() + "+";
            materials = materials.substring(0, materials.length() - 1);

            try (Connection connection = DATABASE.getConnection();
                 Statement statement = connection.createStatement()) {

                ResultSet resultSet = statement.executeQuery("SELECT *, " + materials + " AS total FROM erosion ORDER BY total LIMIT " + limit + ";");
                while (resultSet.next()) {
                    Map<Material, Integer> inner = new LinkedHashMap<>();
                    map.put(new ChunkCoordinate(resultSet.getInt(1), resultSet.getInt(2)), inner);
                    int count = resultSet.getMetaData().getColumnCount();
                    for(int i = 3; i <= count - 1; i++) {
                        inner.put(Material.valueOf(resultSet.getMetaData().getColumnName(i).toUpperCase()), resultSet.getInt(i));
                    }
                }
            }

            return map;
        }
    }
}
