package com.z0cken.mc.economy.shops;

import org.bukkit.Material;

public class InventoryMeta {
    private Trader trader;
    private TradeInventoryType type;
    private Material mat;

    public InventoryMeta(Trader trader, TradeInventoryType type, Material mat){
        this.trader = trader;
        this.type = type;
        this.mat = mat;
    }

    public InventoryMeta(Trader trader, TradeInventoryType type){
        this.trader = trader;
        this.type = type;
        this.mat = null;
    }

    public Trader getTrader(){
        return this.trader;
    }

    public TradeInventoryType getType(){
        return this.type;
    }

    public Material getMaterial(){
        return this.mat;
    }
}
