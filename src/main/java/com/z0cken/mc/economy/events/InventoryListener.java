package com.z0cken.mc.economy.events;

import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.shops.Trader;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {
    private PCS_Economy pcs_economy;

    public InventoryListener(PCS_Economy pcs_economy){
        this.pcs_economy = pcs_economy;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        String title = e.getView().getTitle();
        if(title != null && !title.isEmpty()){
            if(title.contains("Shop")){
                e.setCancelled(true);
                String[] components = title.split("-");
                Trader trader = pcs_economy.traderManager.getTrader(components[0]);
            }else if(title.contains("Konfiguration")){
                String[] components = title.split("-");
                Trader trader = pcs_economy.traderManager.getTrader(components[0]);
            }else{
                return;
            }
        }
        return;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e){
        String title = e.getView().getTitle();
        if(title != null && !title.isEmpty()){
            if(title.contains("Shop")){
            }else if(title.contains("Konfiguration")){
                String[] components = title.split("-");
                Trader trader = pcs_economy.traderManager.getTrader(ChatColor.stripColor(components[0]));
                if(trader.isAdminShop()){

                }else{

                }
            }

        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e){
        String title = e.getView().getTitle();
        if(title != null && !title.isEmpty()){
            if(title.contains("Konfiguration")){
                String[] components = title.split("-");
                Trader trader = pcs_economy.traderManager.getTrader(ChatColor.stripColor(components[0]));
                ItemStack[] contents = e.getInventory().getStorageContents();
                if(contents.length > 0){
                    trader.getTradeItems().clear();
                    e.getPlayer().sendMessage(String.valueOf(contents.length));
                    for(ItemStack stack : contents){
                        if(stack != null){
                            trader.addTradeItem(pcs_economy.adminShopItemManager.getTradeItem(stack.getType()));
                        }
                    }
                }
            }
        }
    }
}
