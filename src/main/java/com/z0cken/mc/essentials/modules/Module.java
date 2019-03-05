package com.z0cken.mc.essentials.modules;

import com.google.common.base.Charsets;
import com.z0cken.mc.essentials.PCS_Essentials;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

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

        PCS_Essentials.getInstance().getLogger().info("Module '" + NAME + "' enabled");
    }

    private void loadConfig() {
        File file = new File(PCS_Essentials.getInstance().getDataFolder(), NAME + ".yml");
        config = YamlConfiguration.loadConfiguration(file);
        InputStream defConfigStream = PCS_Essentials.getInstance().getResource(file.getName());

        if (defConfigStream != null) {
            this.config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
        }
    }

    protected final void registerCommand(String command) {
        commands.add(command);
        if(this instanceof CommandExecutor) {
            PCS_Essentials.getInstance().getCommand(command).setExecutor((CommandExecutor) this);
        } else throw new RuntimeException("Could not register \"" + command + "\" - Module does not implement CommandExecutor");
    }

    public final void disable() {
        onDisable();
        if(this instanceof Listener) HandlerList.unregisterAll((Listener) this);
        if(this instanceof CommandExecutor) commands.forEach(cmd -> PCS_Essentials.getInstance().getCommand(cmd).setExecutor(null));
        tasks.forEach(BukkitTask::cancel);
        PCS_Essentials.getInstance().getLogger().info("Module '" + NAME + "' disabled");

    }

    public final void reload() {
        loadConfig();
        load();

        PCS_Essentials.getInstance().getLogger().info("Module '" + NAME + "' reloaded");
    }

    protected abstract void load();

    protected void onDisable() {}

    protected final YamlConfiguration getConfig() {
        return config;
    }

    protected static Logger getLogger() { return PCS_Essentials.getInstance().getLogger(); }

}
