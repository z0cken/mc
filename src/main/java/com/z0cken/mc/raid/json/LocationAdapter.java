package com.z0cken.mc.raid.json;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.IOException;

public class LocationAdapter extends TypeAdapter<Location> {

    @Override
    public void write(JsonWriter writer, Location location) throws IOException {
        writer.beginObject();
        writer.name("world").value(location.getWorld().getName());
        writer.name("x").value(location.getX());
        writer.name("y").value(location.getY());
        writer.name("z").value(location.getZ());
        writer.name("yaw").value(location.getYaw());
        writer.name("pitch").value(location.getPitch());
        writer.endObject();
    }

    @Override
    public Location read(JsonReader reader) throws IOException {
        String world = "world";
        double x = 0, y = 0, z = 0, yaw = 0, pitch = 0;
        String fieldname = null;

        reader.beginObject();
        while(reader.hasNext()) {
            JsonToken token = reader.peek();

            if (token.equals(JsonToken.NAME)) {
                fieldname = reader.nextName();
            }

            switch (fieldname) {
                case "world":
                    world = reader.nextString();
                    break;
                case "x":
                    x = reader.nextDouble();
                    break;
                case "y":
                    y = reader.nextDouble();
                    break;
                case "z":
                    z = reader.nextDouble();
                    break;
                case "yaw":
                    yaw = reader.nextDouble();
                    break;
                case "pitch":
                    pitch = reader.nextDouble();
                    break;
                default: reader.skipValue();
            }
        }

        reader.endObject();
        return new Location(Bukkit.getWorld(world), x, y, z, (float) yaw, (float) pitch);
    }
}
