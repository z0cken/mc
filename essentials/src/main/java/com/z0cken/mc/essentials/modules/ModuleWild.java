package com.z0cken.mc.essentials.modules;

import com.z0cken.mc.core.util.MessageBuilder;
import com.z0cken.mc.essentials.PCS_Essentials;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static org.bukkit.Material.*;

public class ModuleWild extends Module implements CommandExecutor {

    private int MIN_RAD_SQRD;
    private int COOLDOWN;
    private int LIMIT;

    private final Set<Material> NORMAL_MATERIALS = EnumSet.of(
            GRASS_BLOCK,
            DIRT,
            STONE,
            GRAVEL,
            SAND,
            GRASS,
            CLAY,
            COARSE_DIRT,
            RED_SAND,
            TERRACOTTA,
            YELLOW_TERRACOTTA,
            SNOW,
            ICE,
            BLUE_ICE,
            FROSTED_ICE,
            PACKED_ICE
    );

    private final Set<Material> NETHER_MATERIALS = EnumSet.of(
            NETHERRACK,
            NETHER_QUARTZ_ORE,
            BASALT,
            SOUL_SAND,
            SOUL_SOIL,
            WARPED_NYLIUM,
            WARPED_NYLIUM,
            BLACKSTONE,
            OBSIDIAN
    );

    private final Set<Material> IGNORED_MATERIALS = EnumSet.of(
            AIR,
            GRASS,
            FERN,
            ACACIA_LEAVES,
            BIRCH_LEAVES,
            DARK_OAK_LEAVES,
            JUNGLE_LEAVES,
            OAK_LEAVES,
            SPRUCE_LEAVES,
            BROWN_MUSHROOM,
            RED_MUSHROOM,
            CRIMSON_ROOTS,
            NETHER_SPROUTS
    );

    private final Map<UUID, Integer> cooldowns = new HashMap<>();

    ModuleWild(String configPath) {
        super(configPath);
        registerCommand("wild");
    }

    @Override
    public boolean onCommand(CommandSender commandsender, Command command, String s, String[] args) {
        Player player = null;
        if(commandsender instanceof Player) player = (Player) commandsender;

        if(command.getName().equalsIgnoreCase("wild")) {

            if(args.length > 0 && args[0].equalsIgnoreCase("benchmark") && (player == null || player.hasPermission("pcs.essentials.wild.benchmark"))) {
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

                Integer remaining = cooldowns.getOrDefault(player.getUniqueId(), null);
                if(remaining == null) {
                    final Location target = getRandomLocation(player.getWorld(), LIMIT);
                    if(target == null) player.sendMessage(getConfig().getString("messages.error"));

                    else {
                        player.spigot().sendMessage(MessageBuilder.DEFAULT.define("VALUE", Integer.toString((int) target.distance(player.getLocation()))).build(getConfig().getString("messages.success")));
                        player.teleport(target);
                        if(!player.hasPermission("pcs.essentials.wild.bypass")) cooldowns.put(player.getUniqueId(), COOLDOWN);
                    }
                } else player.spigot().sendMessage(MessageBuilder.DEFAULT.define("VALUE", remaining.toString()).build(getConfig().getString("messages.wait")));
            }
        }

        return true;
    }

    @Override
    protected void load() {
        MIN_RAD_SQRD = (int) Math.pow(getConfig().getDouble("min-distance"), 2.0D);
        COOLDOWN = getConfig().getInt("cooldown");
        LIMIT = getConfig().getInt("limit");

        tasks.add(new BukkitRunnable(){
            @Override public void run() {
                Iterator<Map.Entry<UUID, Integer>> iterator = cooldowns.entrySet().iterator();
                while (iterator.hasNext()) {
                    final Map.Entry<UUID, Integer> next = iterator.next();
                    next.setValue(next.getValue() - 1);
                    if(next.getValue() <= 0) iterator.remove();
                }
            }
        }.runTaskTimer(PCS_Essentials.getInstance(), 100, 20));
    }

    private Location getRandomLocation(World world, int limit) {
        if(limit <= 0) return null;
        Random r = new Random();
        WorldBorder worldBorder = world.getWorldBorder();
        int max = (int) worldBorder.getSize() / 2;
        World.Environment environment = world.getEnvironment();

        for (int i = 0; i < limit; i++) {
            Location location = worldBorder.getCenter();

            //Find random position beyond min distance that's not ocean
            int tests = 0;
            int x;
            int z;
            Biome biome;
            do {
                x = r.nextInt(max);
                z = r.nextInt(max);
                biome = location.clone().add(x, 0, z).getBlock().getBiome();
            } while (tests++ < 100 && (x * x + z * z < MIN_RAD_SQRD || (environment == World.Environment.NORMAL && biome.name().endsWith("OCEAN"))));

            location.add(x, 0, z);

            //Vertical search
            switch (environment) {
                case NORMAL: {
                    if(MIN_RAD_SQRD >= max * max * 2) return null; //Square circumscribed

                    location.setY(world.getMaxHeight() - 1);
                    Material currentType = location.getBlock().getType();
                    while (IGNORED_MATERIALS.contains(currentType) && location.getY() > 0) {
                        location.add(0, -1, 0);
                        currentType = location.getBlock().getType();
                    }

                    final Block block = location.getBlock();
                    if(NORMAL_MATERIALS.contains(block.getType()) && !block.getRelative(0, 2, 0).getType().isSolid()) return location.add(0.5, 1, 0.5);
                    break;
                }
                case NETHER: {
                    //Divide by 8^2
                    if(MIN_RAD_SQRD >> 6 >= max * max * 2) return null; //Square circumscribed

                    //Bottom up
                    location.setY(32);
                    Material currentType = location.getBlock().getType();
                    boolean ground = false, feet = false;

                    while (location.getY() < 128) {
                        if(NETHER_MATERIALS.contains(currentType)) {
                            ground = true;
                            feet = false;
                        } else if(ground && IGNORED_MATERIALS.contains(currentType)) {
                            if(feet) {
                                //Head clear
                                return location.add(0.5, -1, 0.5);
                            } else feet = true;
                        }

                        location.add(0, 1, 0);
                        currentType = location.getBlock().getType();
                    }
                    break;
                }
                default: {
                    return null;
                }
            }
        }

        return null;
    }
}
