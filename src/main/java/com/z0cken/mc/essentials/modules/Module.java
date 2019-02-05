package com.z0cken.mc.essentials.modules;

import com.z0cken.mc.essentials.PCS_Essentials;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("WeakerAccess")
public abstract class Module {

    protected final Collection<BukkitTask> tasks = new ArrayList<>();
    private final Collection<String> commands = new ArrayList<>();
    private final String configPath;
    protected ConfigurationSection config;

    Module(String configPath) {
        this.configPath = configPath;
        loadConfig();
        load();

        if(this instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) this, PCS_Essentials.getInstance());
        }
    }

    private void loadConfig() {
        config = PCS_Essentials.getInstance().getConfig().getConfigurationSection(configPath);
    }

    protected void registerCommand(String command) {
        commands.add(command);
        if(this instanceof CommandExecutor) {
            PCS_Essentials.getInstance().getCommand(command).setExecutor((CommandExecutor) this);
        } else throw new RuntimeException("Could not register \"" + command + "\" - Module does not implement CommandExecutor");
    }

    public void disable() {
        if(this instanceof Listener) HandlerList.unregisterAll((Listener) this);
        if(this instanceof CommandExecutor) commands.forEach(cmd -> PCS_Essentials.getInstance().getCommand(cmd).setExecutor(null));
        tasks.forEach(BukkitTask::cancel);
    }

    public void reload() {
        loadConfig();
        load();
    }

    protected abstract void load();

}
