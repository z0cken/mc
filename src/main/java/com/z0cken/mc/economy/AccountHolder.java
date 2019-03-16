package com.z0cken.mc.economy;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AccountHolder {

    private UUID uuid;

    public AccountHolder(UUID uuid){
        this.uuid = uuid;
    }

    public String getName(){
        return getOfflinePlayer().getName();
    }

    public OfflinePlayer getOfflinePlayer(){
        return PCS_Economy.pcs_economy.getServer().getOfflinePlayer(uuid);
    }

    public Player getPlayer(){
        return PCS_Economy.pcs_economy.getServer().getPlayer(uuid);
    }

    public UUID getUUID(){
        return this.uuid;
    }

    public void sendMessage(String message){
        Player p = getPlayer();
        if(getPlayer() != null && p.isOnline()){
            p.sendMessage(message);
        }
    }
}
