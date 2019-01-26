package com.z0cken.mc.economy.events;

import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.shops.InventoryMeta;
import com.z0cken.mc.economy.shops.TradeInventoryType;
import com.z0cken.mc.economy.shops.Trader;
import com.z0cken.mc.economy.shops.gui.TraderTradeGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {
    private PCS_Economy pcs_economy;

    public InventoryListener(PCS_Economy pcs_economy){
        this.pcs_economy = pcs_economy;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        if(pcs_economy.inventoryManager.getInventories().containsKey(e.getClickedInventory())){
            InventoryMeta information = pcs_economy.inventoryManager.getInventories().get(e.getClickedInventory());
            Trader trader = information.getTrader();
            ItemStack stack = e.getCurrentItem();
            switch(information.getType()){
                case CONFIG:
                    break;
                case SELECTION:
                    e.setCancelled(true);
                    if(stack != null && stack.getType() != Material.AIR){
                        e.getWhoClicked().closeInventory();
                        Inventory inv = new TraderTradeGUI(trader).getInventory(stack.getType(), (Player)e.getWhoClicked());
                        e.getWhoClicked().closeInventory();
                        e.getWhoClicked().openInventory(inv);
                        pcs_economy.inventoryManager.getInventories().put(inv, new InventoryMeta(trader, TradeInventoryType.TRADE, stack.getType()));
                    }
                    break;
                case TRADE:
                    e.setCancelled(true);

                    break;
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e){
        if(pcs_economy.inventoryManager.getInventories().containsKey(e.getInventory())){
            InventoryMeta information = pcs_economy.inventoryManager.getInventories().get(e.getInventory());
            switch (information.getType()){
                case TRADE:
                    e.setCancelled(true);
                    break;
                case SELECTION:
                    e.setCancelled(true);
                    break;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e){
        if(pcs_economy.inventoryManager.getInventories().containsKey(e.getInventory())){
            InventoryMeta information = pcs_economy.inventoryManager.getInventories().get(e.getInventory());
            Trader trader = information.getTrader();
            switch (information.getType()){
                case CONFIG:
                    ItemStack[] contents = e.getInventory().getStorageContents();
                    if(contents.length > 0){
                        trader.getTradeItems().clear();
                        for(ItemStack stack : contents){
                            if(stack != null){
                                trader.addTradeItem(pcs_economy.adminShopItemManager.getTradeItem(stack.getType()));
                            }
                        }
                    }
                    break;
                case TRADE:

                    break;
            }
            pcs_economy.inventoryManager.getInventories().remove(e.getInventory());
        }
        /*String title = e.getView().getTitle();
        if(title != null && !title.isEmpty()){
            if(title.contains("Konfiguration")){
                String[] components = title.split("-");
                Trader trader = pcs_economy.traderManager.getTrader(ChatColor.stripColor(components[0]));
                ItemStack[] contents = e.getInventory().getStorageContents();
                if(contents.length > 0){
                    trader.getTradeItems().clear();
                    for(ItemStack stack : contents){
                        if(stack != null){
                            trader.addTradeItem(pcs_economy.adminShopItemManager.getTradeItem(stack.getType()));
                        }
                    }
                }
            }
        }*/
    }
}
