package com.z0cken.mc.essentials.modules;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class ModuleNether extends Module implements Listener {
    ModuleNether(String configPath) {
        super(configPath);
    }

    @Override
    protected void load() {

    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if(event.getBlock().getType() == Material.SPAWNER && event.getBlock().getWorld().getEnvironment() == World.Environment.NETHER) event.setCancelled(true);
    }
}
