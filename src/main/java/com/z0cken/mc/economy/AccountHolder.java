package com.z0cken.mc.economy;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AccountHolder {

    public OfflinePlayer accountHolderOP;
    public String accountHolderPN;
    public Player accountHolderP;
    public UUID accountHolderUUID;

    public AccountHolder(OfflinePlayer player){
        if(player != null)
            this.accountHolderOP = player;
            this.accountHolderPN = player.getName();
    }

    public AccountHolder(String playerName){
        if(playerName != null)
            this.accountHolderPN = playerName;
    }

    public String getName(){
        return accountHolderPN;
    }

    public void sendMessage(String message){
        if(accountHolderOP.isOnline()){
            accountHolderOP.getPlayer().sendMessage(message);
        }
    }

    public String getId(){
        return accountHolderOP.getUniqueId().toString();
    }

    public UUID getUUID(){
        return accountHolderOP.getUniqueId();
    }
}
