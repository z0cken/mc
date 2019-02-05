package com.z0cken.mc.essentials;

import com.z0cken.mc.essentials.modules.ModuleManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

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
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("pcs") && args.length > 0) {
                if(args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
                    if(args.length > 2) {

                    } else {
                        reload();
                    }
                }
            }
        return true;
    }

    private void reload() {
        reloadConfig();
        ModuleManager.loadModules();
    }

}
