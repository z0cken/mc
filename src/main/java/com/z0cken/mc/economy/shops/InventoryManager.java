package com.z0cken.mc.economy.shops;

import org.bukkit.inventory.Inventory;

import java.util.HashMap;

public class InventoryManager {
    private HashMap<Inventory, InventoryMeta> inventories;

    public InventoryManager(){
        this.inventories = new HashMap<>();
    }

    public HashMap<Inventory, InventoryMeta> getInventories(){
        return inventories;
    }
}
