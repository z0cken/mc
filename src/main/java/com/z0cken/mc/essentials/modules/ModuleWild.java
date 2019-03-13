package com.z0cken.mc.essentials.modules;

import com.z0cken.mc.claim.PCS_Claim;
import com.z0cken.mc.core.util.MessageBuilder;
import com.z0cken.mc.essentials.PCS_Essentials;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ModuleWild extends Module implements CommandExecutor {

    private int DISTANCE_SQUARED;
    private int COOLDOWN;
    private int LIMIT;

    private Set<Material> VALID_MATERIALS = Set.of(
            Material.GRASS_BLOCK,
            Material.DIRT,
            Material.STONE,
            Material.GRAVEL,
            Material.SAND,
            Material.GRASS,
            Material.CLAY,
            Material.COARSE_DIRT,
            Material.RED_SAND,
            Material.TERRACOTTA,
            Material.YELLOW_TERRACOTTA,
            Material.SNOW,
            Material.ICE,
            Material.BLUE_ICE,
            Material.FROSTED_ICE,
            Material.PACKED_ICE,
            Material.FERN
    );

    private Map<Player, Integer> players = new HashMap<>();

    ModuleWild(String configPath) {
        super(configPath);
        registerCommand("wild");
    }

    @Override
    public boolean onCommand(CommandSender commandsender, Command command, String s, String[] args) {
        Player player = null;
        if(commandsender instanceof Player) player = (Player) commandsender;

        if(command.getName().equalsIgnoreCase("wild")) {

            if(args.length > 0 && args[0].equalsIgnoreCase("benchmark")) {
                World world;
                if(args.length > 3) world = Bukkit.getWorld(args[3]);
                else if(player != null) world = player.getWorld();
                else return false;

                if(world == null) commandsender.sendMessage("§c§l>_ §7World not found");
                else {
                    List<Long> timings = new ArrayList<>();
                    new BukkitRunnable() {
                        int iterations = Integer.parseInt(args[1]);
                        int failed = 0;
                        @Override
                        public void run() {
                            long start = System.nanoTime();
                            if(getRandomLocation(world, LIMIT) == null) failed++;
                            long end = System.nanoTime();
                            timings.add(end-start);
                            if(--iterations == 0) {
                                this.cancel();
                                commandsender.sendMessage("§7-----------[ §b/wild Benchmark Results §7]-----------");
                                commandsender.sendMessage("§7Iterations: §b" + timings.size() + "§7 | Interval: §b" + Integer.parseInt(args[2]) + " ticks");
                                commandsender.sendMessage("§7Average execution time: §b§l" + new BigDecimal(timings.stream().mapToLong(Long::longValue).average().getAsDouble() / 1000000).setScale(4, RoundingMode.HALF_UP).doubleValue() + " ms");
                                commandsender.sendMessage("§7Failed searches: " + (failed > 0 ? ChatColor.RED : ChatColor.AQUA) + failed);
                            }
                        }
                    }.runTaskTimer(PCS_Essentials.getInstance(), 0, Integer.parseInt(args[2]));
                }
                return true;
            }

            if(player != null) {

                if(player.getWorld().getEnvironment() != World.Environment.NORMAL) {
                    player.sendMessage(getConfig().getString("messages.error"));
                    return true;
                }

                Integer remaining = players.getOrDefault(player, null);
                if(remaining == null) {
                    final Location target = getRandomLocation(player.getWorld(), LIMIT);
                    if(target == null) player.sendMessage(getConfig().getString("messages.error"));

                    else {
                        player.spigot().sendMessage(MessageBuilder.DEFAULT.define("VALUE", Integer.toString((int) target.distance(player.getLocation()))).build(getConfig().getString("messages.success")));
                        player.teleport(target);
                        if(!player.hasPermission("pcs.essentials.wild.bypass")) players.put(player, COOLDOWN);
                    }
                } else player.spigot().sendMessage(MessageBuilder.DEFAULT.define("VALUE", remaining.toString()).build(getConfig().getString("messages.wait")));
            }
        }

        return true;
    }

    @Override
    protected void load() {
        DISTANCE_SQUARED = (int) Math.pow(getConfig().getDouble("min-distance"), 2.0D);
        COOLDOWN = getConfig().getInt("cooldown");
        LIMIT = getConfig().getInt("limit");

        tasks.add(new BukkitRunnable(){
            @Override public void run() {
                Iterator<Map.Entry<Player, Integer>> iterator = players.entrySet().iterator();
                while (iterator.hasNext()) {
                    final Map.Entry<Player, Integer> next = iterator.next();
                    next.setValue(next.getValue() - 1);
                    if(next.getValue() <= 0) iterator.remove();
                }
            }
        }.runTaskTimer(PCS_Essentials.getInstance(), 100, 20));
    }

    private Location getRandomLocation(World world, int limit) {
        if(limit <= 0) return null;
        Random r = new Random();
        final WorldBorder worldBorder = world.getWorldBorder();
        Location location = worldBorder.getCenter();
        final int max = (int) worldBorder.getSize() / 2;
        location.add(r.nextInt(max) - r.nextInt(max), 0, r.nextInt(max) - r.nextInt(max));
        location.setY(world.getMaxHeight() - 1);

        if(location.distanceSquared(worldBorder.getCenter()) < DISTANCE_SQUARED || location.getBlock().getBiome().name().endsWith("OCEAN")|| PCS_Claim.getClaim(location.getChunk()) != null) return getRandomLocation(world, --limit);

        Material currentType = location.getBlock().getType();
        while (currentType == Material.AIR || currentType.name().endsWith("LEAVES")) {
            location.add(0, -1, 0);
            currentType = location.getBlock().getType();
        }

        if(VALID_MATERIALS.contains(location.getBlock().getType()) && location.getBlock().getRelative(0, 2, 0).getType() == Material.AIR) return location.add(0, 1, 0);
        else return getRandomLocation(world, --limit);
    }
}
