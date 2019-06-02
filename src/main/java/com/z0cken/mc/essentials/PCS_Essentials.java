package com.z0cken.mc.essentials;

import com.z0cken.mc.essentials.modules.Module;
import com.z0cken.mc.essentials.modules.ModuleManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

/** @author Flare */
public class PCS_Essentials extends JavaPlugin {

    private static PCS_Essentials instance;

    public static PCS_Essentials getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        ModuleManager.loadModules();
    }

    @Override
    public void onDisable() {
        instance = null;
        ModuleManager.shutdown();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("pcse")) {
            if(args.length > 0) {
                if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
                    if (args.length > 1) {
                        Optional<Module> module = ModuleManager.getActiveModules().stream().filter(m -> args[1].equalsIgnoreCase(m.getName())).findAny();

                        if(module.isPresent()) {
                            module.get().reload();
                            sender.sendMessage(getConfig().getString("messages.module-reload-success"));
                        } else sender.sendMessage(getConfig().getString("messages.module-reload-fail"));

                    } else {
                        reload();
                        sender.sendMessage(getConfig().getString("messages.reload"));
                    }
                } else return false;
            } else return false;
        }
        return true;
    }

    private void reload() {
        reloadConfig();
        ModuleManager.loadModules();
    }

}
