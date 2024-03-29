package com.z0cken.mc.economy.events;

import com.z0cken.mc.economy.Account;
import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.shops.*;
import com.z0cken.mc.economy.shops.gui.TradeInventorySlotType;
import com.z0cken.mc.economy.shops.gui.TraderTradeGUI;
import com.z0cken.mc.economy.shops.gui.TraderTradeSelectionGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("Duplicates")
public class InventoryListener implements Listener {
    private PCS_Economy pcs_economy;

    public InventoryListener(PCS_Economy pcs_economy){
        this.pcs_economy = pcs_economy;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        ItemStack stack = e.getCurrentItem();
        Player player = (Player)e.getWhoClicked();
        Inventory topInventory = player.getOpenInventory().getTopInventory();
        InventoryMeta information = null;
        if(topInventory != null && pcs_economy.inventoryManager.getInventories().containsKey(topInventory)){
            information = pcs_economy.inventoryManager.getInventories().get(topInventory);
            if(information.getType() == TradeInventoryType.TRADE || information.getType() == TradeInventoryType.SELECTION){
                e.setCancelled(true);
            }
        }
        if(pcs_economy.inventoryManager.getInventories().containsKey(e.getClickedInventory()) && e.getWhoClicked() instanceof Player
            && stack != null && stack.getType() != Material.AIR){
            Trader trader = information.getTrader();
            TradeItem item = pcs_economy.adminShopItemManager.getTradeItem(information.getMaterial());
            Account account = pcs_economy.accountManager.getAccount(e.getWhoClicked().getUniqueId());
            switch(information.getType()){
                case CONFIG:
                    break;
                case SELECTION:
                    e.setCancelled(true);
                    if(stack != null && stack.getType() != Material.AIR){
                        item = pcs_economy.adminShopItemManager.getTradeItem(stack.getType());
                        e.getWhoClicked().closeInventory();
                        Inventory inv = new TraderTradeGUI(trader).getInventory(item, (Player)e.getWhoClicked());
                        e.getWhoClicked().closeInventory();
                        e.getWhoClicked().openInventory(inv);
                        pcs_economy.inventoryManager.getInventories().put(inv, new InventoryMeta(trader, TradeInventoryType.TRADE, stack.getType()));
                        TraderTradeGUI.doInvLogic(inv, item, account);
                    }
                    break;
                case TRADE:
                    e.setCancelled(true);
                    int clickedSlot = e.getSlot();
                    TradeInventorySlotType type = TradeInventorySlotType.getCorrespondingType(clickedSlot);
                    Inventory playerInventory = e.getWhoClicked().getInventory();
                    if(type != null){
                        switch (type){
                            case SLOT_SELL_SINGLE:
                                if(item.isSellable() && account.has(item.getSellprice())){
                                    if(InventoryHelper.canFit(playerInventory, item.getMaterial(), 1)){
                                        account.subtract(item.getSellprice());
                                        playerInventory.addItem(new ItemStack(item.getMaterial(), 1));
                                    }
                                }
                                break;
                            case SLOT_SELL_STACK:
                                if(item.isSellable() && account.has(item.getSellprice() * item.getMaterial().getMaxStackSize())){
                                    if(InventoryHelper.canFit(playerInventory, item.getMaterial(), item.getMaterial().getMaxStackSize())){
                                        account.subtract(item.getSellprice() * item.getMaterial().getMaxStackSize());
                                        playerInventory.addItem(new ItemStack(item.getMaterial(), item.getMaterial().getMaxStackSize()));
                                    }
                                }
                                break;
                            case SLOT_SELL_INV:
                                if(item.isSellable()){
                                    int capacity = InventoryHelper.getOverallItemCapacity(playerInventory, item.getMaterial());
                                    if(capacity > 0 && account.has(item.getSellprice() * capacity)){
                                        account.subtract(item.getSellprice() * capacity);
                                        playerInventory.addItem(new ItemStack(item.getMaterial(), capacity));
                                    }
                                }
                                break;
                            case SLOT_BUY_SINGLE:
                                if(item.isBuyable() && InventoryHelper.hasAmountOfItem(playerInventory, item.getMaterial(), 1)){
                                    account.add(item.getBuyPrice());
                                    playerInventory.removeItem(new ItemStack(item.getMaterial(), 1));
                                }
                                break;
                            case SLOT_BUY_STACK:
                                if(item.isBuyable() && InventoryHelper.hasAmountOfItem(playerInventory, item.getMaterial(), item.getMaterial().getMaxStackSize())){
                                    account.add(item.getBuyPrice() * item.getMaterial().getMaxStackSize());
                                    playerInventory.removeItem(new ItemStack(item.getMaterial(), item.getMaterial().getMaxStackSize()));
                                }
                                break;
                            case SLOT_BUY_INV:
                                if(item.isBuyable()){
                                    int amount = InventoryHelper.getAmountOfItem(playerInventory, item.getMaterial());
                                    if(amount > 0){
                                        account.add(item.getBuyPrice() * amount);
                                        playerInventory.removeItem(new ItemStack(item.getMaterial(), amount));
                                    }
                                }
                                break;
                        }
                        TraderTradeGUI.doInvLogic(e.getInventory(), item, account);
                    }else{
                        if(playerInventory.firstEmpty() == -1){

                        }
                    }
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
