package com.z0cken.mc.essentials.modules;

import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;

public class ModuleVarious extends Module implements Listener {

    private boolean easySleep, prohibitCure, blockFarmDrops;

    ModuleVarious(String configPath) {
        super(configPath);
    }

    @Override
    protected void load() {
        easySleep = getConfig().getBoolean("easy-sleep");
        prohibitCure = getConfig().getBoolean("prohibit-cure");
        blockFarmDrops = getConfig().getBoolean("block-farm-drops");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBedEnter(PlayerBedEnterEvent event) {
        if(easySleep) {
            ((CraftPlayer)event.getPlayer()).getHandle().sleepTicks = 0;
            event.getPlayer().sendMessage("§f§l>_ §7Deine Wachzeit wurde zurückgesetzt!");
        }
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCure(CreatureSpawnEvent event) {
        if(prohibitCure && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CURED) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        if(!blockFarmDrops || event.getEntityType() == EntityType.PLAYER) return;
        switch (event.getEntity().getLastDamageCause().getCause()) {
            case CRAMMING:
            case FALL:
            case SUFFOCATION:
                event.getDrops().clear();
        }
    }

}
