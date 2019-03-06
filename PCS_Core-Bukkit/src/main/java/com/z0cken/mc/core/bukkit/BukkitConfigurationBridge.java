package com.z0cken.mc.core.bukkit;

import com.z0cken.mc.core.util.ConfigurationBridge;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public class BukkitConfigurationBridge extends ConfigurationBridge {

    private YamlConfiguration configuration;

    BukkitConfigurationBridge(YamlConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public int getInt(String path) {
        return configuration.getInt(path);
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
