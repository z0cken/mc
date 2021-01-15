package com.z0cken.mc.economy.shops.gui;

import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.economy.shops.TradeItem;
import com.z0cken.mc.economy.shops.Trader;
import com.z0cken.mc.economy.utils.MessageHelper;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TraderTradeSelectionGUI{

    private Trader trader;

    public TraderTradeSelectionGUI(Trader trader){
        this.trader = trader;
    }

    public Inventory getInventory(){
        String title;
        if(trader.isAdminShop()){
            title = ChatColor.RED + trader.getTraderName() + "-Shop";
        }else{
            title = ChatColor.GREEN + trader.getTraderName() + "-Shop";
        }

        Inventory inv = PCS_Economy.pcs_economy.getServer().createInventory(null, 27, title);
        if(trader.isAdminShop()){
            trader.getTradeItems().forEach(item -> {
                ItemStack stack = new ItemStack(item.getMaterial(), 1);
                ItemMeta meta = stack.getItemMeta();
                TradeItem adminItem = PCS_Economy.pcs_economy.adminShopItemManager.getTradeItem(item.getMaterial());
                String sellPrice = null;
                String buyPrice = null;
                if(adminItem.isSellable()){
                    sellPrice = MessageHelper.convertBcToString(PCS_Economy.pcs_economy.getMessageBuilder()
                            .define("AMOUNT", String.valueOf(adminItem.getSellprice()))
                            .build(ConfigManager.tradeSelectionSellPrice));
                }
                if(adminItem.isBuyable()){
                    buyPrice = MessageHelper.convertBcToString(PCS_Economy.pcs_economy.getMessageBuilder()
                            .define("AMOUNT", String.valueOf(adminItem.getBuyPrice()))
                            .build(ConfigManager.tradeSelectionBuyPrice));
                }
                List<String> lore = new ArrayList<>();
                if(sellPrice != null){
                    lore.add(sellPrice);
                }
                if(buyPrice != null){
                    lore.add(buyPrice);
                }
                meta.setLore(lore);
                stack.setItemMeta(meta);
                inv.addItem(stack);
            });
        }
        return inv;
    }
}