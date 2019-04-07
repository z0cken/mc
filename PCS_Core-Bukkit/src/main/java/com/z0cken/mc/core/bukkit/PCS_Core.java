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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class PCS_Core extends JavaPlugin implements ICore, Listener {

    private static PCS_Core instance;
    public static PCS_Core getInstance() {
        return instance;
    }

    private static YamlConfiguration coreConfig;

    @Override
    public void onLoad() {
        instance = this;

        saveResource("core.yml", false);
        saveResource("hikari.properties", false);
        saveResource("main.properties", false);
        saveDefaultConfig();

        coreConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "core.yml"));
        coreConfig.setDefaults(YamlConfiguration.loadConfiguration(getTextResource("core.yml")));
        coreConfig.options().copyDefaults(true);
    }

    @Override
    public void onEnable() {
        ICore.super.init();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        ICore.super.shutdown();
        instance = null;
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
            case CORE: return new BukkitConfigurationBridge(coreConfig);
            case PLUGIN: return new BukkitConfigurationBridge(getConfig());
            default: throw new UnsupportedOperationException();
        }
    }

    @Override
    public Set<UUID> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).collect(Collectors.toSet());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        PersonaAPI.invalidate(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        //if(getConfig().getStringList("crucial-plugins").contains(event.getPlugin().getName())) stopServer("§c- Automatischer Shutdown -\n\nBitte benachrichtige ein Teammitglied!");
    }
}
