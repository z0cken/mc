package com.z0cken.mc.economy.shops;

import com.z0cken.mc.economy.PCS_Economy;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class InventoryHelper {
    public static boolean canFit(Inventory inv, Material mat, int amount){
        if(inv != null && mat != null && amount > 0){
            int freeSlots = getFreeSlots(inv);
            if(freeSlots >= 1){
                return true;
            }
            if(freeSlots == 0){
                if(inv.contains(mat)){
                    int filled = 0;
                    for (ItemStack stack : inv.getStorageContents()) {
                        if(stack.getType() == mat && stack.getAmount() < mat.getMaxStackSize()){
                            int dif = mat.getMaxStackSize() - stack.getAmount();
                            filled += dif;
                        }
                    }
                    if(filled >= amount){
                        return true;
                    }
                }
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
                    if(stack.getType() == mat){
                        if(stack.getAmount() < mat.getMaxStackSize()){
                            capacity += mat.getMaxStackSize() - stack.getAmount();
                        }
                    }
                }else{
                    capacity += mat.getMaxStackSize();
                }
            }
            return capacity;
        }
        return -1;
    }
}
