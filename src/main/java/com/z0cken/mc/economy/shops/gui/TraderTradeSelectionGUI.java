package com.z0cken.mc.economy.shops.gui;

import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.economy.shops.Trader;
import com.z0cken.mc.economy.utils.MessageBuilder;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        trader.getTradeItems().forEach(item -> {
            ItemStack stack = new ItemStack(item.getMaterial(), 1);
            ItemMeta meta = stack.getItemMeta();
            String sellPrice = MessageBuilder.buildMessage(false, ConfigManager.tradeSelectionSellPrice, item.getSellprice(), 0);
            String buyPrice = MessageBuilder.buildMessage(false, ConfigManager.tradeSelectionBuyPrice, item.getBuyPrice(), 0);
            List<String> lore = Arrays.asList(sellPrice, buyPrice);
            meta.setLore(lore);
            stack.setItemMeta(meta);
            inv.addItem(stack);
        });

        return inv;
    }
}
