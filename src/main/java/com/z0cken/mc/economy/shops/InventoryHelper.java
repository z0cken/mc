package com.z0cken.mc.economy.shops;

import com.z0cken.mc.economy.Account;
import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.shops.gui.TradeInventorySlotType;
import com.z0cken.mc.economy.shops.gui.TraderTradeGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class InventoryHelper {
    public static boolean canFit(Inventory inv, Material mat, int amount){
        if(inv != null && mat != null && amount > 0){
            if(getOverallItemCapacity(inv, mat) >= amount){
                return true;
            }
        }
        return false;
    }

    public static int getFreeSlots(Inventory inv){
        if(inv != null){
            int freeSlots = 0;
            for (ItemStack stack : inv.getStorageContents()) {
                if(stack == null){
                    freeSlots++;
                }
            }
            return freeSlots;
        }
        return -1;
    }

    public static int getOverallItemCapacity(Inventory inv, Material mat){
        if(inv != null && mat != null){
            int capacity = 0;
            for (ItemStack stack : inv.getStorageContents()) {
                if(stack != null){
                    if(stack.getType() == mat && stack.getAmount() < mat.getMaxStackSize()){
                        capacity += mat.getMaxStackSize() - stack.getAmount();
                    }
                }else{
                    capacity += mat.getMaxStackSize();
                }
            }
            return capacity;
        }
        return -1;
    }

    public static boolean hasAmountOfItem(Inventory inv, Material mat, int amount){
        if(inv != null && mat != null & amount > 0){
            if(getAmountOfItem(inv, mat) >= amount){
                return true;
            }
        }
        return false;
    }

    public static int getAmountOfItem(Inventory inv, Material mat){
        if(inv != null && mat != null){
            int amount = 0;
            for(ItemStack stack : inv.getStorageContents()){
                if(stack != null && stack.getType() == mat){
                    amount += stack.getAmount();
                }
            }
            return amount;
        }
        return -1;
    }
}