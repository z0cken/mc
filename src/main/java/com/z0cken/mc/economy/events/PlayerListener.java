package com.z0cken.mc.economy.events;

import com.z0cken.mc.economy.PCS_Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

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
}
