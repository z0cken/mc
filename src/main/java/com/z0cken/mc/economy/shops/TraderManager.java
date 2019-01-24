package com.z0cken.mc.economy.shops;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class TraderManager {
    private ArrayList<Trader> traders;

    public TraderManager(){
        this.traders = new ArrayList<Trader>();
    }

    public void addTrader(Trader trader){
        if(trader != null){
            if(traders.stream().filter(trader1 -> trader1.getTraderUUID().equals(trader.getTraderUUID())).findFirst().orElse(null) == null){
                traders.add(trader);
            }
        }
    }

    public boolean removeTrader(Trader trader){
        if(trader != null){
            if(traders.contains(trader)){
                traders.remove(trader);
                return true;
            }
            return false;
        }
        return false;
    }

    public boolean removeTrader(UUID uuid){
        if(uuid != null){
            Trader trader = traders.stream().filter(trader1 -> trader1.getTraderUUID().equals(uuid)).findFirst().orElse(null);
            if(trader != null){
                traders.remove(trader);
                return true;
            }
            return false;
        }
        return false;
    }

    public Trader getTrader(UUID uuid){
        if(uuid != null){
            return traders.stream().filter(trader -> trader.getTraderUUID().equals(uuid)).findFirst().orElse(null);
        }
        return null;
    }

    public ArrayList<UUID> getTradersFromPlayer(Player player){
        if(player != null){
            ArrayList<UUID> uuids = new ArrayList<>();
            traders.forEach(trader -> {
                if(trader.getOwnerUUID().equals(player.getUniqueId())){
                    uuids.add(trader.getTraderUUID());
                }
            });
            return uuids;
        }
        return null;
    }

    public Trader getTrader(int index){
        if(index < traders.size()){
            return traders.get(index);
        }
        return null;
    }
}
