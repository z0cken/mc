package com.z0cken.mc.essentials.modules;

import com.z0cken.mc.essentials.PCS_Essentials;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("WeakerAccess")
public abstract class Module {

    private final String NAME;
    private final Collection<String> commands = new ArrayList<>();
    protected final Collection<BukkitTask> tasks = new ArrayList<>();

    private YamlConfiguration config;

    Module(String name) {
        this.NAME = name;
        loadConfig();
        load();

        if(this instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) this, PCS_Essentials.getInstance());
        }
    }

    private void loadConfig() {
        PCS_Essentials.getInstance().saveResource(NAME + ".yml", false);
        config = YamlConfiguration.loadConfiguration(new File(PCS_Essentials.getInstance().getDataFolder(), NAME + ".yml"));
    }

    protected final void registerCommand(String command) {
        commands.add(command);
        if(this instanceof CommandExecutor) {
            PCS_Essentials.getInstance().getCommand(command).setExecutor((CommandExecutor) this);
        } else throw new RuntimeException("Could not register \"" + command + "\" - Module does not implement CommandExecutor");
    }

    public final void disable() {
        if(this instanceof Listener) HandlerList.unregisterAll((Listener) this);
        if(this instanceof CommandExecutor) commands.forEach(cmd -> PCS_Essentials.getInstance().getCommand(cmd).setExecutor(null));
        tasks.forEach(BukkitTask::cancel);
    }

    public final void reload() {
        loadConfig();
        load();
    }

    protected abstract void load();

    protected final YamlConfiguration getConfig() {
        return config;
    }

}
