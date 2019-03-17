package com.z0cken.mc.metro;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MetroMapRenderer extends MapRenderer {

    private byte[] colors;

    public MetroMapRenderer(Path path) throws IOException {
        super(true);
        colors = Files.readAllBytes(path);
    }

    @Override
    public void render(MapView mapview, MapCanvas canvas, Player player) {
        for (int x = 0; x < 128; ++x) {
            for (int y = 0; y < 128; ++y) {
                canvas.setPixel(x, y, colors[y * 128 + x]);
            }
        }

        MapCursorCollection cursors = canvas.getCursors();
        while (cursors.size() > 0) cursors.removeCursor(cursors.getCursor(0));

    }
}
