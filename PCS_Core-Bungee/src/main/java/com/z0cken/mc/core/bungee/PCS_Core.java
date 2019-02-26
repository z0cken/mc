package com.z0cken.mc.core.bungee;

import com.google.common.io.ByteStreams;
import com.z0cken.mc.core.ICore;
import com.z0cken.mc.core.persona.PersonaAPI;
import com.z0cken.mc.core.util.CoreTask;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.util.UUID;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class PCS_Core extends Plugin implements ICore, Listener {

    private static PCS_Core instance;
    private static Configuration config;

    public static PCS_Core getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        saveResource("core.yml", false);
        saveResource("hikari.properties", false);
        saveResource("main.properties", false);
        loadConfig();
    }

    @Override
    public void onEnable() {
        ICore.super.init();

        PersonaAPI.init(getConfig().getLong("persona-api.cache-interval"), getConfig().getLong("persona-api.update-interval"));

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

    @EventHandler
    public void onJoin(PostLoginEvent event) {
        PersonaAPI.cachePlayer(event.getPlayer().getUniqueId());
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

    void saveResource(String resourcePath, boolean replace) {
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

    void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");

        try {

            if (configFile.createNewFile()) {

                try (InputStream is = getResourceAsStream("config.yml");
                     OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void saveConfig() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Configuration getConfig() {
        return config;
    }
}
