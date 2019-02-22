package com.z0cken.mc.economy.shops;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class Trader {

    private String traderUUID;
    private int traderID;
    private String ownerUUID;
    private String traderName;
    private transient Entity entity;
    private boolean isAdminShop;
    private ArrayList<TradeItem> items;

    public Trader(int traderID, Entity entity, Player player, String traderName, boolean isAdminShop){
        this.traderID = traderID;
        this.traderUUID = entity.getUniqueId().toString();
        this.ownerUUID = player.getUniqueId().toString();
        this.traderName = traderName;
        this.entity = entity;
        this.isAdminShop = isAdminShop;
        this.items = new ArrayList<>();
    }

    public ArrayList<TradeItem> getTradeItems(){
        return this.items;
    }

    public boolean addTradeItem(TradeItem tradeItem){
        if(tradeItem != null){
            TradeItem item = items.stream().filter(iItem -> iItem.getMaterial() == tradeItem.getMaterial()).findFirst().orElse(null);
            if(item == null){
                items.add(tradeItem);
                return true;
            }
            return false;
        }
        return false;
    }

    public boolean removeTradeItem(TradeItem item){
        return removeTradeItem(item.getMaterial().name());
    }

    public boolean removeTradeItem(String itemName){
        if(itemName != null && itemName.isEmpty()){
            TradeItem item = items.stream().filter(iItem -> iItem.getMaterial().name().equals(itemName)).findFirst().orElse(null);
            if(item != null){
                items.remove(item);
                return true;
            }
            return false;
        }
        return false;
    }

    public String getTraderName(){
        return traderName != null ? traderName : "Shop";
    }

    public void increaseItemAmount(String tradeItemName, int amount){
        if(amount > 0){
            TradeItem item = items.stream().filter(iItem -> iItem.getMaterial().name().equals(tradeItemName)).findFirst().orElse(null);
            if(item != null){
                item.setAmount(item.getAmount() + amount);
            }
        }
    }

    public void decreaseItemAmount(String tradeItemName, int amount){
        if(amount > 0){
            TradeItem item = items.stream().filter(iItem -> iItem.getMaterial().name().equals(tradeItemName)).findFirst().orElse(null);
            if(item != null){
                item.setAmount(item.getAmount() - amount);
            }
        }
    }

    public int getItemAmount(Material mat){
        if(mat != null){
            TradeItem item = items.stream().filter(iItem -> iItem.getMaterial().equals(mat)).findFirst().orElse(null);
            if(item != null){
                return item.getAmount();
            }
        }
        return 0;
    }

    public int getItemAmount(String tradeItemName){
        TradeItem item = items.stream().filter(iItem -> iItem.getMaterial().name().equals(tradeItemName)).findFirst().orElse(null);
        if(item != null){
            return item.getAmount();
        }
        return 0;
    }

    public void setTraderName(String traderName){
        if(traderName != null){
            this.traderName = traderName;
        }
    }

    public boolean isAdminShop(){
        return this.isAdminShop;
    }

    public int getTraderID() {return this.traderID;}

    public UUID getOwnerUUID(){
        return UUID.fromString(this.ownerUUID);
    }

    public UUID getTraderUUID(){
        return UUID.fromString(this.traderUUID);
    }

    public Entity getEntity(){
        return this.entity;
    }
}
