package com.z0cken.mc.economy;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AccountHolder {

    private OfflinePlayer accountHolderOP;

    public AccountHolder(OfflinePlayer player){
        if(player != null)
            this.accountHolderOP = player;
    }

    public String getName(){
        return accountHolderOP.getName();
    }

    public OfflinePlayer getOfflinePlayer(){
        return this.accountHolderOP;
    }

    public Player getPlayer(){
        return this.accountHolderOP.getPlayer();
    }

    public UUID getUUID(){
        return accountHolderOP.getUniqueId();
    }

    public void sendMessage(String message){
        if(accountHolderOP.isOnline()){
            accountHolderOP.getPlayer().sendMessage(message);
        }
    }

}
