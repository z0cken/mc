package com.z0cken.mc.core.bukkit;

import com.z0cken.mc.core.ICore;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public class PCS_Core extends JavaPlugin implements ICore {

    private static PCS_Core instance;

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        instance = this;
        saveResource("hikari.properties", false);
        ICore.super.init();
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

    public static PCS_Core getInstance() {
        return instance;
    }
}
