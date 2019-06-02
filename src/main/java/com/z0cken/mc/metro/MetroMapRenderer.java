package com.z0cken.mc.metro;

import com.z0cken.mc.metro.util.NMSUtil;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.map.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class MetroMapRenderer extends MapRenderer {

    private static final File configFile = new File(PCS_Metro.getInstance().getDataFolder(), "maps.yml");
    private static final YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
    private static final File mapFolder = new File(PCS_Metro.getInstance().getDataFolder(), "maps");
    private static Map<String, MetroMapRenderer> renderers;

    private static Set<Integer> known = new HashSet<>();
    static {
        new BukkitRunnable() {
            @Override
            public void run() {
                NMSUtil.getLoadedMaps(Bukkit.getWorld("world")).forEach(mapView -> {
                    if(known.contains(mapView.getId())) return;
                    getBySubscriber(mapView.getId()).forEach(r -> r.subscribe(mapView));
                    known.add(mapView.getId());
                });
            }
        }.runTaskTimer(PCS_Metro.getInstance(), 200, 200);
    }

    private final ConfigurationSection section;

    private final String name;
    private final byte[] colors;

    private final MapView.Scale scale;
    private final int centerX, centerZ;
    private final boolean unlimitedTracking, transparent;

    private final MapCursorCollection cursors = new MapCursorCollection();
    private final Map<Integer, Boolean> subscribers = new HashMap<>();

    MetroMapRenderer(ConfigurationSection section, String key) throws IOException {
        super(section.getBoolean("contextual"));
        this.section = section;
        name = key;
        colors = Files.readAllBytes(new File(mapFolder, name).toPath());
        if(section.getBoolean("flip")) ArrayUtils.reverse(colors);

        scale = MapView.Scale.valueOf(section.getString("scale").toUpperCase());
        centerX = section.getInt("center-x");
        centerZ = section.getInt("center-z");
        unlimitedTracking = section.getBoolean("unlimited-tracking");
        transparent = section.getBoolean("transparent");

        final ConfigurationSection cursorSection = section.getConfigurationSection("cursors");
        if(cursorSection != null) {
            for(String s : cursorSection.getKeys(false)) {
                ConfigurationSection sec = cursorSection.getConfigurationSection(s);
                cursors.addCursor(new MapCursor(
                    (byte) sec.getInt("x"),
                    (byte) sec.getInt("z"),
                    (byte) sec.getInt("direction"),
                    MapCursor.Type.valueOf(sec.getString("type").toUpperCase()),
                    sec.getBoolean("visible"),
                    sec.getString("caption")
                ));
            }
        }

        section.getIntegerList("subscribers").forEach(i -> subscribers.put(i, false));

        final long refreshTime = section.getLong("refresh");
        if(refreshTime > 0L) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    refresh();
                }
            }.runTaskTimer(PCS_Metro.getInstance(), refreshTime, refreshTime);
        }
    }

    @Override
    public void render(MapView mapview, MapCanvas canvas, Player player) {
        if(subscribers.getOrDefault(mapview.getId(), false)) return;

        for (int x = 0; x < 128; ++x) {
            for (int y = 0; y < 128; ++y) canvas.setPixel(x, y, colors[y * 128 + x]);
        }

        mapview.setScale(scale);
        mapview.setCenterX(centerX);
        mapview.setCenterZ(centerZ);
        mapview.setUnlimitedTracking(unlimitedTracking);
        canvas.setCursors(cursors);

        subscribers.put(mapview.getId(), true);
    }

    public String getName() {
        return name;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterZ() {
        return centerZ;
    }

    public void subscribe(MapView mapView) {
        if(!transparent) mapView.getRenderers().forEach(mapView::removeRenderer);
        mapView.addRenderer(this);
        subscribers.put(mapView.getId(), false);
    }

    public boolean isSubscribed(int id) {
        return subscribers.containsKey(id);
    }

    public void refresh() {
        subscribers.replaceAll((integer, aBoolean) -> false);
    }

    private void save() {
        section.set("subscribers", new ArrayList<>(subscribers.keySet()));
    }

    public static void saveAll() {
        renderers.values().forEach(MetroMapRenderer::save);
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadFromConfig() {
        PCS_Metro.getInstance().getLogger().info("Loading maps...");
        final Map<String, MetroMapRenderer> map = new HashMap<>();
        for(String s : config.getKeys(false)) {
            MetroMapRenderer renderer;
            try { renderer = new MetroMapRenderer(config.getConfigurationSection(s), s);
            } catch (IOException e) {
                PCS_Metro.getInstance().getLogger().log(Level.WARNING, "Failed to load map file " + s, e);
                continue;
            }
            PCS_Metro.getInstance().getLogger().info(String.format("> %s (%d | %d)", renderer.getName(), renderer.getCenterX(), renderer.getCenterZ()));
            map.put(renderer.getName(), renderer);
        }

        renderers = Collections.unmodifiableMap(map);
    }

    public static MetroMapRenderer getByName(String name) {
        return renderers.getOrDefault(name, null);
    }

    public static Set<MetroMapRenderer> getBySubscriber(int mapId) {
        return renderers.values().stream().filter(map -> map.isSubscribed(mapId)).collect(Collectors.toSet());
    }
}
