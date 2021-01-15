package com.z0cken.mc.core.bungee;

import com.z0cken.mc.core.util.ConfigurationBridge;
import net.md_5.bungee.config.Configuration;

import java.util.List;

public class BungeeConfigurationBridge implements ConfigurationBridge {

    private Configuration configuration;

    BungeeConfigurationBridge(Configuration configuration) {
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
        return false;
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
