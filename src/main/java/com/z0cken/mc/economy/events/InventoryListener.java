package com.z0cken.mc.economy.events;

import com.z0cken.mc.economy.Account;
import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.economy.shops.*;
import com.z0cken.mc.economy.shops.gui.TradeInventorySlot;
import com.z0cken.mc.economy.shops.gui.TradeInventorySlotType;
import com.z0cken.mc.economy.shops.gui.TraderTradeGUI;
import com.z0cken.mc.economy.shops.gui.TraderTradeSelectionGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class InventoryListener implements Listener {
    private PCS_Economy pcs_economy;

    public InventoryListener(PCS_Economy pcs_economy){
        this.pcs_economy = pcs_economy;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        if(pcs_economy.inventoryManager.getInventories().containsKey(e.getClickedInventory()) && e.getWhoClicked() instanceof Player){
            InventoryMeta information = pcs_economy.inventoryManager.getInventories().get(e.getClickedInventory());
            Trader trader = information.getTrader();
            ItemStack stack = e.getCurrentItem();
            Player player = (Player)e.getWhoClicked();
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
                    int clickedSlot = e.getSlot();
                    TradeInventorySlotType type = TradeInventorySlotType.getCorrespondingType(clickedSlot);
                    Inventory playerInventory = e.getWhoClicked().getInventory();
                    Account account = pcs_economy.accountManager.getAccount(e.getWhoClicked().getUniqueId());
                    TradeItem item = pcs_economy.adminShopItemManager.getTradeItem(information.getMaterial());
                    if(type != null && playerInventory.firstEmpty() != -1){
                        switch (type){
                            case SLOT_SELL_SINGLE:
                                if(account.has(item.getSellprice())){
                                    if(InventoryHelper.canFit(playerInventory, item.getMaterial(), 1)){
                                        account.subtract(item.getSellprice());
                                        TraderTradeGUI.changeLoreInvSell(e.getInventory(), player, item.getMaterial());
                                        playerInventory.addItem(new ItemStack(item.getMaterial(), 1));
                                    }
                                }
                                break;
                            case SLOT_SELL_STACK:
                                if(account.has(item.getSellprice() * item.getMaterial().getMaxStackSize())){
                                    if(InventoryHelper.canFit(playerInventory, item.getMaterial(), item.getMaterial().getMaxStackSize())){
                                        account.subtract(item.getSellprice() * item.getMaterial().getMaxStackSize());
                                        TraderTradeGUI.changeLoreInvSell(e.getInventory(), player, item.getMaterial());
                                        playerInventory.addItem(new ItemStack(item.getMaterial(), item.getMaterial().getMaxStackSize()));
                                    }
                                }
                                break;
                            case SLOT_SELL_INV:
                                int capacity = InventoryHelper.getOverallItemCapacity(playerInventory, item.getMaterial());
                                if(account.has(item.getSellprice() * capacity)){
                                    account.subtract(item.getSellprice() * capacity);
                                    TraderTradeGUI.changeLoreInvSell(e.getInventory(), player, item.getMaterial());
                                    playerInventory.addItem(new ItemStack(item.getMaterial(), capacity));
                                }
                                break;
                            case SLOT_BUY_SINGLE:
                                break;
                            case SLOT_BUY_STACK:
                                break;
                            case SLOT_BUY_INV:
                                break;
                        }
                    }else{
                        if(playerInventory.firstEmpty() == -1){

                        }
                    }
                    break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
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
                    new BukkitRunnable(){
                        @Override
                        public void run(){
                            Inventory inv = new TraderTradeSelectionGUI(trader).getInventory();
                            pcs_economy.inventoryManager.getInventories().put(inv, new InventoryMeta(trader, TradeInventoryType.SELECTION));
                            e.getPlayer().openInventory(inv);
                        }
                    }.runTaskLater(PCS_Economy.pcs_economy, 3);
                    break;
            }
            pcs_economy.inventoryManager.getInventories().remove(e.getInventory());
        }
    }
}
