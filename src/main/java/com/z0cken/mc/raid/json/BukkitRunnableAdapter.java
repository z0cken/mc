package com.z0cken.mc.raid.json;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;

public class BukkitRunnableAdapter extends TypeAdapter<BukkitRunnable> {
    @Override
    public void write(JsonWriter jsonwriter, BukkitRunnable object) throws IOException {

    }

    @Override
    public BukkitRunnable read(JsonReader jsonreader) throws IOException {
        return null;
    }
}
