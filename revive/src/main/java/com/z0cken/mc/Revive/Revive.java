package com.z0cken.mc.Revive;

import com.z0cken.mc.Revive.command.RespawnCommand;
import com.z0cken.mc.Revive.listener.PlayerDeathListener;
import com.z0cken.mc.Revive.listener.PlayerInteractListener;
import com.z0cken.mc.Revive.listener.PlayerMoveListener;
import com.z0cken.mc.Revive.utils.NMSBridge;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Revive extends JavaPlugin {

    private static Revive revive;
    private NMSBridge nmsBridge;

    @Override
    public void onEnable() {
        this.revive = this;

        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(), this);

        this.getCommand("respawn").setExecutor(new RespawnCommand());

        try {
            this.nmsBridge = new NMSBridge("v1_16_R3");
        } catch (Exception e) {
            System.err.println("NMS version hat sich geändert. Plugin update?");
            nmsBridge = null;
            e.printStackTrace();
        }

        this.saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        for(RespawnPhase respawnPhase : RespawnHandler.getPlayerRespawns().values()) {
            respawnPhase.respawnPlayer();
        }
    }

    public FileConfiguration getBukkitConfig() {
        return this.getConfig();
    }

    public NMSBridge getNmsBridge() {
        return nmsBridge;
    }

    public static Revive getPlugin() {
        return revive;
    }
}
