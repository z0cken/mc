package com.z0cken.mc.economy.shops.gui;

import com.z0cken.mc.economy.PCS_Economy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum TradeInventorySlotType {
    SLOT_SELL_SINGLE(new int[]{6, 15, 24}),
    SLOT_SELL_STACK(new int[] {7, 16, 25}),
    SLOT_SELL_INV(new int[]   {8, 17, 26}),
    SLOT_BUY_SINGLE(new int[] {2, 11, 20}),
    SLOT_BUY_STACK(new int[]  {1, 10, 19}),
    SLOT_BUY_INV(new int[]    {0, 9, 18}),
    SLOT_ALL(new int[]{0, 1, 2, 9, 10, 11, 18, 19, 20, 6, 7, 8, 15, 16, 17, 24, 25, 26}),
    SLOT_ALL_SELL(new int[]{6, 15, 24, 7, 16, 25, 8, 17, 26}),
    SLOT_ALL_BUY(new int[]{2, 11, 20, 1, 10, 19, 0, 9, 18});

    private int[] slots;

    TradeInventorySlotType(int[] slots){
        this.slots = slots;
    }

    public int[] getSlots(){
        return this.slots;
    }

    public static TradeInventorySlotType getCorrespondingType(int l){
        List<Integer> slotAllList = Arrays.stream(SLOT_ALL.getSlots()).boxed().collect(Collectors.toList());
        if(slotAllList.contains(l)){
            for (TradeInventorySlotType type : values()) {
                if(Arrays.stream(type.getSlots()).boxed().collect(Collectors.toList()).contains(l) && type != SLOT_ALL){
                    return type;
                }
            }
        }
        return null;
    }
}