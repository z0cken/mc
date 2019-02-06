package com.z0cken.mc.economy.shops.gui;

import org.bukkit.Keyed;

public enum TradeInventorySlots{
    SLOTS_SELL_SINGLE(new int[] {0, 1, 2});

    private int[] slots;

    TradeInventorySlots(int[] slots){
        this.slots = slots;
    }
}
