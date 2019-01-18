package com.z0cken.mc.Revive;

import com.z0cken.mc.Revive.listener.PlayerDeathListener;
import com.z0cken.mc.Revive.listener.PlayerInteractListener;
import com.z0cken.mc.Revive.listener.PlayerMoveListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Revive extends JavaPlugin {

    private static Revive revive;


    @Override
    public void onEnable() {
        this.revive = this;

        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(), this);
    }

    public static Revive getPlugin() {
        return revive;
    }
}
