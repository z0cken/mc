package com.z0cken.mc.core.bukkit;

import com.z0cken.mc.core.util.ConfigurationBridge;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class BukkitConfigurationBridge implements ConfigurationBridge {

    private FileConfiguration configuration;

    BukkitConfigurationBridge(FileConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public int getInt(String path) {
        return configuration.getInt(path);
    }

    @Override
    public double getDouble(String path) {
        return configuration.getDouble(path);
    }

    @Override
    public long getLong(String path) {
        return configuration.getLong(path);
    }

    @Override
    public boolean getBoolean(String path) {
        return configuration.getBoolean(path);
    }

    @Override
    public String getString(String path) {
        return configuration.getString(path);
    }

    @Override
    public List<String> getStringList(String path) {
        return configuration.getStringList(path);
    }
}
