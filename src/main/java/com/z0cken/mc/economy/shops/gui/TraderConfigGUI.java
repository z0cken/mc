package com.z0cken.mc.economy.shops.gui;

import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.shops.Trader;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TraderConfigGUI {
    private Trader trader;

    public TraderConfigGUI(Trader trader){
        this.trader = trader;
    }

    public Inventory getInventory(){
        String title = null;
        if(trader.isAdminShop()){
            title = ChatColor.RED + trader.getTraderName() + "-Konfiguration";
        }else{
            title = ChatColor.GREEN + trader.getTraderName() + "-Konfiguration";
        }
        Inventory inv = PCS_Economy.pcs_economy.getServer().createInventory(null, 27, title);
        if(trader.isAdminShop()){
            inv.setMaxStackSize(1);
            trader.getTradeItems().forEach(item -> inv.addItem(new ItemStack(item.getMaterial(), 1)));
            return inv;
        }else{
            trader.getTradeItems().forEach(item -> inv.addItem(new ItemStack(item.getMaterial(), item.getAmount())));
            return inv;
        }
    }
}
