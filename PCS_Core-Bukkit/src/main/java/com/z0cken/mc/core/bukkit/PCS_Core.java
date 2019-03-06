package com.z0cken.mc.core.bukkit;

import com.z0cken.mc.core.ICore;
import com.z0cken.mc.core.persona.PersonaAPI;
import com.z0cken.mc.core.util.ConfigurationBridge;
import com.z0cken.mc.core.util.ConfigurationType;
import com.z0cken.mc.core.util.CoreTask;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.util.UUID;

@SuppressWarnings("unused")
public class PCS_Core extends JavaPlugin implements ICore, Listener {

    private static PCS_Core instance;
    private static YamlConfiguration coreConfig;
    private static ConfigurationBridge coreConfigBridge;

    @Override
    public void onLoad() {
        instance = this;

        saveResource("core.yml", false);
        saveResource("hikari.properties", false);
        saveResource("main.properties", false);
        saveDefaultConfig();

        coreConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "core.yml"));
        coreConfigBridge = new BukkitConfigurationBridge(coreConfig);
    }

    @Override
    public void onEnable() {
        ICore.super.init();

        PersonaAPI.init(coreConfig.getLong("persona-api.cache-interval"), coreConfig.getLong("persona-api.update-interval"));

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        ICore.super.shutdown();
        instance = null;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        PersonaAPI.updateCachedPersona(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        //if(getConfig().getStringList("crucial-plugins").contains(event.getPlugin().getName())) stopServer("§c- Automatischer Shutdown -\n\nBitte benachrichtige ein Teammitglied!");
    }

    @Override
    public void stopServer(String reason) {
        Bukkit.getOnlinePlayers().forEach(p -> p.kickPlayer(reason));
        Bukkit.getServer().shutdown();
    }

    @Override
    public int schedule(CoreTask task) {
        BukkitScheduler scheduler = Bukkit.getScheduler();

        long delay = task.getDelay() == null ? 0 : task.getDelay();
        delay = task.getTimeUnit().toSeconds(delay) * 20;

        if(task.getInterval() == null) {
            if(delay == 0) return task.isAsync() ? scheduler.runTaskAsynchronously(this, task).getTaskId() : scheduler.runTask(this, task).getTaskId();
            else return task.isAsync() ? scheduler.runTaskLaterAsynchronously(this, task, delay).getTaskId() : scheduler.runTaskLater(this, task, delay).getTaskId();
        } else {
            long interval = task.getTimeUnit().toSeconds(task.getInterval()) * 20;
            return task.isAsync() ? scheduler.runTaskTimerAsynchronously(this, task, delay, interval).getTaskId() : scheduler.runTaskTimer(this, task, delay, interval).getTaskId();
        }
    }

    @Override
    public void cancelTask(int id) {
        Bukkit.getScheduler().cancelTask(id);
    }

    @Override
    public boolean isOnline(UUID uuid) {
        Player player = getServer().getPlayer(uuid);
        return player != null && player.isOnline();
    }

    @Override
    public ConfigurationBridge getConfigBridge(ConfigurationType type) {
        switch (type) {
            case CORE: return coreConfigBridge;
            case PLUGIN: return null;
            default: return null;
        }
    }

    @Override
    public String getPlayerPrefix(UUID uuid) {
        //TODO Implement
        return "";
    }

    public static PCS_Core getInstance() {
        return instance;
    }
}
