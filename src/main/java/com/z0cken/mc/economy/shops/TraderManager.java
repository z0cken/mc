package com.z0cken.mc.economy.shops;

import com.z0cken.mc.economy.PCS_Economy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class TraderManager {
    private ArrayList<Trader> traders;

    public TraderManager(){
        this.traders = new ArrayList<>();
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
                Entity entity = PCS_Economy.pcs_economy.getServer().getEntity(trader.getTraderUUID());
                if(entity != null){
                    entity.remove();
                }
                traders.remove(trader);
                return true;
            }
        }
        return false;
    }

    public boolean removeTrader(UUID uuid){
        if(uuid != null){
            Trader trader = traders.stream().filter(trader1 -> trader1.getTraderUUID().equals(uuid)).findFirst().orElse(null);
            return removeTrader(trader);
        }
        return false;
    }

    public Trader getTrader(UUID uuid){
        if(uuid != null){
            return traders.stream().filter(trader -> trader.getTraderUUID().equals(uuid)).findFirst().orElse(null);
        }
        return null;
    }

    public Trader getTrader(int traderID){
        return traders.stream().filter(trader -> trader.getTraderID() == traderID).findFirst().orElse(null);
    }

    public int generateTraderID(){
        boolean found = false;
        int traderID = traders.size() + 1;
        while(!found){
            int lPreTraderID = traderID;
            if(traders.stream().filter(t -> t.getTraderID() == lPreTraderID).findFirst().orElse(null) != null){
                traderID++;
            }else{
                found = true;
            }
        }
        return traderID;
    }

    public ArrayList<Trader> getTraders(){
        return this.traders;
    }

    public Trader getTrader(String traderName){
        return traders.stream().filter(trader -> trader.getTraderName().equals(traderName)).findFirst().orElse(null);
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
}
