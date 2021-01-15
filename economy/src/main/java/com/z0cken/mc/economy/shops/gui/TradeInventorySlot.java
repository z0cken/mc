package com.z0cken.mc.economy.shops.gui;

public class TradeInventorySlot {
    private final TradeInventorySlotType type;
    private final double price;

    public TradeInventorySlot(TradeInventorySlotType type, double price){
        this.type = type;
        this.price = price;
    }

    public TradeInventorySlotType getType(){
        return this.type;
    }

    public double getPrice(){
        return this.price;
    }
}
