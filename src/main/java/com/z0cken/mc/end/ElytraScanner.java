package com.z0cken.mc.end;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ElytraScanner {

    private static final int PERIOD = 5;

    private final World world;
    private BukkitRunnable scanRunnable;

    private Set<ElytraManager.Elytra> matches = new HashSet<>();

    private int startingSize;
    private Integer sizeLimit, matchLimit;
    private Duration timeLimit;
    private List<Integer> exceptions;

    public ElytraScanner(@Nonnull World world) {
        this.world = world;
    }

    public ElytraScanner startingSize(int size) {
        if(isScanning()) throw new IllegalStateException("Scan in progress");
        if(size < 0) throw new IllegalArgumentException("Value must be positive");
        startingSize = size;
        return this;
    }

    public ElytraScanner matchLimit(int limit) {
        if(isScanning()) throw new IllegalStateException("Scan in progress");
        if(limit < 0) throw new IllegalArgumentException("Value must be positive");
        matchLimit = limit;
        return this;
    }

    public ElytraScanner sizeLimit(int limit) {
        if(isScanning()) throw new IllegalStateException("Scan in progress");
        if(limit < 0) throw new IllegalArgumentException("Value must be positive");
        sizeLimit = limit;
        return this;
    }

    public ElytraScanner timeLimit(long limit, @Nonnull TimeUnit unit) {
        if(isScanning()) throw new IllegalStateException("Scan in progress");
        if(limit < 0) throw new IllegalArgumentException("Value must be positive");
        timeLimit = Duration.ofSeconds(unit.toSeconds(limit));
        return this;
    }

    public ElytraScanner skip(List<Integer> list) {
        if(isScanning()) throw new IllegalStateException("Scan in progress");
        this.exceptions = list;
        return this;
    }

    public CompletableFuture<Result> scan() {
        if(isScanning()) throw new IllegalStateException("Scan in progress");
        else matches = new HashSet<>();

        System.setProperty("disable.watchdog", "true");

        CompletableFuture<Result> result = new CompletableFuture<>();

        //EASY TO BREAK (CONCURRENCY)
        scanRunnable = new BukkitRunnable() {
            volatile int i = startingSize;
            Instant instant = Instant.now();
            volatile CompletableFuture<Set<ElytraManager.Elytra>> currentGoal;

            @Override
            public synchronized void run() {
                if(isCancelled() || currentGoal != null && !currentGoal.isDone()) return;
                //System.out.println("run " + i + " | id " + this.getTaskId() + " | thread" + Thread.currentThread().getId());

                currentGoal = scanLayer(i++, PERIOD);
                currentGoal.thenAccept(set -> {
                    matches.addAll(set);
                    PCS_End.getInstance().getLogger().info(String.format("Layer %d -> Subtotal: %d | Total: %d", i, set.size(), matches.size()));
                });

                while (exceptions.contains(i)) i++;

                final Duration timeElapsed = getElapsedTime();
                if(sizeLimit != null && i-1 >= sizeLimit
                || matchLimit != null && matches.size() >= matchLimit
                || timeLimit != null && timeElapsed.compareTo(timeLimit) > 0) {
                    result.complete(new Result(timeElapsed, i-1));
                    cancel();
                }
            }

            @Override
            public synchronized void cancel() throws IllegalStateException {
                Bukkit.getScheduler().cancelTask(this.getTaskId());
                System.setProperty("disable.watchdog", "false");
                result.complete(new Result(getElapsedTime(), i-1));
            }

            Duration getElapsedTime() {
                return Duration.between(instant, Instant.now());
            }

        };
        scanRunnable.runTaskTimer(PCS_End.getInstance(), 10, 5);

        return result;
    }

    private CompletableFuture<Set<ElytraManager.Elytra>> scanLayer(int i, int period) {
        CompletableFuture<Set<ElytraManager.Elytra>> future = new CompletableFuture<>();
        Set<ElytraManager.Elytra> set = new HashSet<>();

        PCS_End.getInstance().getLogger().info(String.format("Scanning layer %d (%d Chunks)", i, i * 8 - 4));
        Vector position = new Vector(i, 0, i);

        new BukkitRunnable() {
            int direction = 0;

            @Override
            public void run() {
                ManagedEndCreator.Direction dir = ManagedEndCreator.Direction.values()[direction];
                Chunk chunk = world.getChunkAt((int) position.getX(), (int) position.getZ());
                world.loadChunk(chunk);

                Arrays.stream(chunk.getEntities()).filter(e -> e.getType() == EntityType.ITEM_FRAME && ((ItemFrame)e).getItem().getType() == Material.ELYTRA).forEach(match -> {
                    set.add(new ElytraManager.Elytra(match.getLocation(), match.getUniqueId(), false));
                    PCS_End.getInstance().getLogger().info(String.format("Layer %d: Found an Elytra in Chunk [%d | %d] (%d subtotal)", i, chunk.getX(), chunk.getZ(), set.size()));
                });

                world.unloadChunk(chunk);

                Vector next = position.clone().add(dir.getVector());
                if(Math.abs(next.getX()) <= i && Math.abs(next.getZ()) <= i) position.add(dir.getVector());
                else if(++direction > 3) {
                    cancel();
                    future.complete(set);
                }
            }

        }.runTaskTimer(PCS_End.getInstance(), 0, period);

        return future;
    }

    public void cancel() {
        if(!isScanning()) throw new IllegalStateException("No scan in progress");
        scanRunnable.cancel();
        scanRunnable = null;
    }

    public boolean isScanning() {
        if(scanRunnable == null) return true;
        return scanRunnable.isCancelled();
    }

    public World getWorld() {
        return world;
    }

    class Result {
        private Set<ElytraManager.Elytra> _matches;

        private int _startingSize;
        private Integer _sizeLimit, _matchLimit;
        private Duration _timeLimit;

        private Duration runtime;
        private int lastLayer;

        //In absolute values
        private Result(Duration runtime, int lastLayer) {
            this._matches = matches;
            this._startingSize = startingSize;
            this._sizeLimit = sizeLimit;
            this._matchLimit = matchLimit;
            this._timeLimit = timeLimit;
            this.runtime = runtime;
            this.lastLayer = lastLayer;
        }

        public Set<ElytraManager.Elytra> getMatches() {
            return _matches;
        }

        public int getStartingSize() {
            return _startingSize;
        }

        public Integer getSizeLimit() {
            return _sizeLimit;
        }

        public Integer getMatchLimit() {
            return _matchLimit;
        }

        public Duration getTimeLimit() {
            return _timeLimit;
        }

        public Duration getRuntime() {
            return runtime;
        }

        public int getLastLayer() {
            return lastLayer;
        }
    }
}