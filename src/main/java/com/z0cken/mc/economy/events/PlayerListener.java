package com.z0cken.mc.economy.events;

import com.z0cken.mc.economy.PCS_Economy;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.z0cken.mc.economy.shops.*;

import java.sql.Connection;

public class PlayerListener implements Listener {

    private Connection conn;

    public PlayerListener(Connection conn){
        this.conn = conn;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player p = event.getPlayer();

        PCS_Economy.pcs_economy.getLogger().info("Player joined");
        PCS_Economy.pcs_economy.getLogger().info("JoinedBefore? " + p.hasPlayedBefore());
        if(!p.hasPlayedBefore()){
            PCS_Economy.pcs_economy.accountManager.createAccount(p);
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityInteract(PlayerInteractEntityEvent e){
        if(e.getPlayer() != null){
            if(e.getRightClicked() != null && e.getRightClicked() instanceof Villager){
                Villager v = (Villager)e.getRightClicked();
                Trader trader = PCS_Economy.pcs_economy.traderManager.getTrader(v.getUniqueId());
                if(trader != null) {
                    e.setCancelled(true);
                }
                return;
            }
            return;
        }
        return;
    }
}
