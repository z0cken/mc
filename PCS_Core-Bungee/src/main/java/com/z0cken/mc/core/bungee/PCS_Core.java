package com.z0cken.mc.core.bungee;

import com.google.common.io.ByteStreams;
import com.z0cken.mc.core.ICore;
import com.z0cken.mc.core.persona.PersonaAPI;
import com.z0cken.mc.core.util.ConfigurationBridge;
import com.z0cken.mc.core.util.ConfigurationType;
import com.z0cken.mc.core.util.CoreTask;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "Duplicates"})
public class PCS_Core extends Plugin implements ICore, Listener {

    private static PCS_Core instance;
    private static Configuration config, coreConfig;
    private static ConfigurationBridge configBridge, coreConfigBridge;

    public static PCS_Core getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        saveResource("core.yml", false);
        saveResource("hikari.properties", false);
        saveResource("main.properties", false);

        config = getConfiguration("config.yml");
        coreConfig = getConfiguration("core.yml");

        configBridge = new BungeeConfigurationBridge(config);
        coreConfigBridge = new BungeeConfigurationBridge(coreConfig);
    }

    @Override
    public void onEnable() {
        ICore.super.init();

        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerListener(this, new ShadowListener());
        getProxy().getPluginManager().registerCommand(this, new CommandFriend());
    }

    @Override
    public void onDisable() {
        ICore.super.shutdown();

        //saveConfig();

        instance = null;
    }

    @Override
    public void stopServer(String reason) {
        this.getProxy().stop(reason);
    }

    @Override
    public int schedule(CoreTask task) {
        if(task.getDelay() == null && task.getInterval() == null) return getProxy().getScheduler().runAsync(this, task).getId();

        long delay = task.getDelay() == null ? 0 : task.getDelay();
        if(task.getInterval() == null) return getProxy().getScheduler().schedule(this, task, delay, task.getTimeUnit()).getId();
        else return getProxy().getScheduler().schedule(this, task, delay, task.getInterval(), task.getTimeUnit()).getId();
    }

    @Override
    public void cancelTask(int id) {
        getProxy().getScheduler().cancel(id);
    }

    @Override
    public boolean isOnline(UUID uuid) {
        ProxiedPlayer player = getProxy().getPlayer(uuid);
        return player != null && player.isConnected();
    }

    @Override
    public Set<UUID> getOnlinePlayers() {
        return ProxyServer.getInstance().getPlayers().stream().map(ProxiedPlayer::getUniqueId).collect(Collectors.toSet());
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        PersonaAPI.invalidate(event.getPlayer().getUniqueId());
    }

    private void saveResource(String resourcePath, boolean replace) {
        if (resourcePath != null && !resourcePath.equals("")) {
            resourcePath = resourcePath.replace('\\', '/');
            InputStream in = this.getResourceAsStream(resourcePath);
            if (in == null) {
                throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + this.getFile());
            } else {
                File outFile = new File(this.getDataFolder(), resourcePath);
                int lastIndex = resourcePath.lastIndexOf(47);
                File outDir = new File(this.getDataFolder(), resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));
                if (!outDir.exists()) {
                    outDir.mkdirs();
                }

                try {
                    if(!outFile.exists() || replace) {
                        OutputStream out = new FileOutputStream(outFile);
                        byte[] buf = new byte[1024];

                        int len;
                        while((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }

                        out.close();
                        in.close();
                    }
                } catch (IOException var10) {
                    this.getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, var10);
                }

            }
        } else {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
    }

    private Configuration getConfiguration(String name) {
        File configFile = new File(getDataFolder(), name);

        try {

            if (configFile.createNewFile()) {

                try (InputStream is = getResourceAsStream(name);
                     OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            return ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    private void saveConfig(Configuration configuration, String name) {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, new File(getDataFolder(), name));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Configuration getConfig(ConfigurationType type) {
        switch (type) {
            case CORE: return coreConfig;
            case PLUGIN: return config;
            default: throw new IllegalArgumentException();
        }
    }

    @Override
    public ConfigurationBridge getConfigBridge(ConfigurationType type) {
        switch (type) {
            case CORE: return coreConfigBridge;
            case PLUGIN: return configBridge;
            default: throw new IllegalArgumentException();
        }
    }
}
