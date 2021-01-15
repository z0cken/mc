package com.z0cken.mc.end;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ItemFrame;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ElytraManager {

    private final World world;
    private final List<Integer> scannedLayers;

    private final File matchFile;
    private final YamlConfiguration matchConfig;

    private final ElytraScanner scanner;
    private final Set<Elytra> elytras = new HashSet<>();

    public ElytraManager(World world) {
        this.world = world;
        this.matchFile = new File(PCS_End.getInstance().getDataFolder(), world.getUID() + ".yml");
        this.matchConfig = YamlConfiguration.loadConfiguration(matchFile);

        if(matchConfig.contains("elytras")) {
            final ConfigurationSection section = matchConfig.getConfigurationSection("elytras");
            for(String id : section.getKeys(false)) {
                final ConfigurationSection subSection = section.getConfigurationSection(id);
                elytras.add(new Elytra(subSection.getSerializable("location", Location.class), UUID.fromString(id), subSection.getBoolean("discovered")));
            }
        }

        scanner = new ElytraScanner(world);
        scannedLayers = matchConfig.getIntegerList("scanned-layers");
    }

    //In chunks
    public CompletableFuture<ElytraScanner.Result> scan(ElytraScanner scanner) {
        //TODO this is retarded
        if(!scanner.equals(this.scanner)) throw new IllegalArgumentException("Please pass the scanner of this manager");

        CompletableFuture<ElytraScanner.Result> result = scanner.scan();
        result.thenAccept(r -> {
            elytras.addAll(r.getMatches());
            IntStream.rangeClosed(r.getStartingSize(), r.getLastLayer()).forEach(scannedLayers::add);
        });

        return result;
    }

    public ElytraScanner getScanner() {
        return scanner;
    }

    public void save() throws IOException {
        matchConfig.set("scanned-layers", scannedLayers);

        ConfigurationSection section = matchConfig.getConfigurationSection("elytras");
        for(Elytra e : elytras) {
            section.set(e.getFrameId() + ".location", e.getLocation());
            section.set(e.getFrameId() + ".discovered", e.isDiscovered() );
        }

        matchConfig.save(matchFile);
    }

    public Set<Elytra> getElytras() {
        return elytras;
    }

    public Set<Elytra> getAvailableElytras() {
        double borderSize = world.getWorldBorder().getSize();
        return elytras.stream().filter(elytra -> elytra.getBorderSize() < borderSize).collect(Collectors.toSet());
    }

    public List<Integer> getScannedLayers() {
        return scannedLayers;
    }

    //expansionRate in seconds/block
    public void expandForElytras(int elytras, double expansionRate) {
        WorldBorder border = world.getWorldBorder();

        //TODO Reverse comparator?
        Optional<Elytra> e = this.elytras.stream().filter(elytra -> elytra.getBorderSize() > border.getSize()).sorted(Comparator.comparingInt(Elytra::getBorderSize)).skip(elytras - 1).findFirst();
        if(e.isPresent()) {
            int targetSize = e.get().getBorderSize() + 5;
            long duration = (long) ((targetSize - border.getSize()) * expansionRate);
            border.setSize(targetSize, duration);
        } else {
            throw new IllegalStateException("Insufficient Elytras tracked - scan required!");
        }
    }

    public void discover(ItemFrame frame) {
        for(Elytra elytra : elytras) {
            if(elytra.getFrameId().equals(frame.getUniqueId())) {
                elytra.discovered = true;
                return;
            }
        }
        PCS_End.getInstance().getLogger().warning(String.format("Elytra found at %s was not tracked", frame.getLocation().toString()));
    }

    static class Elytra {

        private final Location location;
        private final UUID frameId;
        private boolean discovered;

        public Elytra(Location location, UUID frameId, boolean discovered) {
            this.location = location;
            this.frameId = frameId;
            this.discovered = discovered;
        }

        public Location getLocation() {
            return location;
        }

        public UUID getFrameId() {
            return frameId;
        }

        public boolean isDiscovered() {
            return discovered;
        }

        public int getBorderSize() {
            return (int) Math.max(location.getX(), location.getZ());
        }
    }
}
