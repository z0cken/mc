package com.z0cken.mc.end;

import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class ManagedEndCreator{

    private World world;
    private int elytras;
    private File elytraFile;
    private YamlConfiguration matches;

    ManagedEndCreator(String name, int maxElytras) {
        PCS_End.getInstance().getLogger().info(String.format("Creating managed world '%s' with %d elytra!", name, maxElytras));

        elytraFile = new File(PCS_End.getInstance().getDataFolder(), name + ".yml");
        matches = YamlConfiguration.loadConfiguration(elytraFile);

        world = Bukkit.createWorld(new WorldCreator(name).environment(World.Environment.THE_END));

        System.setProperty("disable.watchdog", "true");
        new BukkitRunnable() {
            int i = 1;

            @Override
            public void run() {
                generateLayer(i++);
                if(elytras >= maxElytras) {
                    int size = (i+5) << 4;
                    world.getWorldBorder().setCenter(0, 0);
                    world.getWorldBorder().setSize(size);
                    PCS_End.getInstance().getLogger().info(elytras + " Elytra found in radius " + (i-1) + " for world " + world.getName());
                    PCS_End.getInstance().getLogger().info(String.format("%d Chunks generated - Border Size: %d", (int) Math.pow(i*4, 2), size));
                    System.setProperty("disable.watchdog", "false");

                    this.cancel();

                    try {
                        matches.save(elytraFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskTimer(PCS_End.getInstance(), 20, 5);
    }

    ManagedEndCreator(World world) {
        elytraFile = new File(PCS_End.getInstance().getDataFolder(), world.getName() + ".yml");
        elytraFile.delete();
        matches = YamlConfiguration.loadConfiguration(elytraFile);
    }

    public BukkitTask scan(int period) {
        System.setProperty("disable.watchdog", "true");
        return new BukkitRunnable() {
            int i = 1;
            int max = (int) world.getWorldBorder().getSize() >> 4;
            BukkitTask currentTask;

            @Override
            public void run() {
                if(currentTask == null || currentTask.isCancelled()) {
                    currentTask = generateLayerSlow(i++, period);
                    if(i > max) {
                        cancel();
                        try {
                            matches.save(elytraFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.runTaskTimer(PCS_End.getInstance(), 0, 5);
    }

    private BukkitTask generateLayerSlow(int i, int period) {
        PCS_End.getInstance().getLogger().info(String.format("Generating layer %d (%d Chunks)", i, i * 8 - 4));
        Vector position = new Vector(i, 0, i);

        return new BukkitRunnable() {
            int direction = 0;

            @Override
            public void run() {
                Direction dir = Direction.values()[direction];
                Chunk chunk = world.getChunkAt((int) position.getX(), (int) position.getZ());
                world.loadChunk(chunk);

                Arrays.stream(chunk.getEntities()).filter(e -> e.getType() == EntityType.ITEM_FRAME && ((ItemFrame)e).getItem().getType() == Material.ELYTRA).forEach(match -> {
                    elytras++;
                    matches.set(elytras + ".location", match.getLocation());
                    matches.set(elytras + ".found", false);

                    PCS_End.getInstance().getLogger().info(String.format("Found an Elytra in Chunk [%d | %d] (%d total)", chunk.getX(), chunk.getZ(), elytras));
                });

                world.unloadChunk(chunk);

                Vector next = position.clone().add(dir.getVector());
                if(Math.abs(next.getX()) <= i && Math.abs(next.getZ()) <= i) position.add(dir.getVector());
                else if(++direction > 3) cancel();
            }

        }.runTaskTimer(PCS_End.getInstance(), 0, period);
    }

    private void generateLayer(int i) {
        PCS_End.getInstance().getLogger().info(String.format("Generating layer %d (%d Chunks)", i, i * 8 - 4));
        Vector position = new Vector(i, 0, i);

        for(Direction dir : Direction.values()) {
            while(true) {
                Chunk chunk = world.getChunkAt((int) position.getX(), (int) position.getZ());
                world.loadChunk(chunk);

                Arrays.stream(chunk.getEntities()).filter(e -> e.getType() == EntityType.ITEM_FRAME && ((ItemFrame)e).getItem().getType() == Material.ELYTRA).forEach(match -> {
                    elytras++;
                    matches.set(elytras + ".location", match.getLocation());
                    matches.set(elytras + ".found", false);

                    PCS_End.getInstance().getLogger().info(String.format("Found an Elytra in Chunk [%d | %d] (%d total)", chunk.getX(), chunk.getZ(), elytras));
                });

                world.unloadChunk(chunk);

                Vector next = position.clone().add(dir.getVector());
                if(Math.abs(next.getX()) <= i && Math.abs(next.getZ()) <= i) position.add(dir.getVector());
                else break;
            }
        }
    }

    public World getWorld() {
        return world;
    }

    enum Direction {
        DOWN(0, -1), LEFT(-1, 0), UP(0, 1), RIGHT(1, 0);

        Vector vector;

        Direction(int x, int z) {
            vector = new Vector(x, 0, z);
        }

        public Vector getVector() {
            return vector.clone();
        }
    }

}
