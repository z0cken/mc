package com.z0cken.mc.end;

import com.z0cken.mc.core.util.MessageBuilder;
import com.z0cken.mc.end.egg.EggListener;
import com.z0cken.mc.end.egg.MagicEgg;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class PCS_End extends JavaPlugin {

    private static PCS_End instance;
    public static PCS_End getInstance() {
        return instance;
    }

    private static CoreProtectAPI coreProtect;

    private End end;
    private MessageBuilder messageBuilder;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginCommand("end").setExecutor(new EndCommand());
        Bukkit.getPluginManager().registerEvents(new EggListener(), this);
        Bukkit.getPluginManager().registerEvents(new EyeListener(), this);

        getCoreProtect();
        if(coreProtect == null) {
            getLogger().severe("CoreProtect not present!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        end = new End(getConfig().getConfigurationSection("end"));
        messageBuilder = MessageBuilder.DEFAULT.define("PREFIX", getConfig().getString("messages.prefix"));

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        if(end.getPhase() != null) {
            end.getConfig().set("last-phase", end.getPhase().getType().name());
            end.getPhase().stop();
        }

        try {
            end.getElytraManager().save();
        } catch (IOException e) {
            e.printStackTrace();
        }

        end.rollback();
        saveConfig();

        MagicEgg.getEggs().forEach(MagicEgg::expire);

        instance = null;
    }

    public End getEnd() {
        return end;
    }

    public MessageBuilder getMessageBuilder() {
        return messageBuilder;
    }

    static CoreProtectAPI getCoreProtect() {
        if(coreProtect != null) return coreProtect;

        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("CoreProtect");

        // Check that CoreProtect is loaded
        if (plugin == null || !(plugin instanceof CoreProtect)) {
            return null;
        }

        // Check that the API is enabled
        CoreProtectAPI coreProtect = ((CoreProtect) plugin).getAPI();
        if (!coreProtect.isEnabled()) {
            return null;
        }

        // Check that a compatible version of the API is loaded
        if (coreProtect.APIVersion() < 6) {
            return null;
        }

        PCS_End.coreProtect = coreProtect;
        return coreProtect;
    }
}