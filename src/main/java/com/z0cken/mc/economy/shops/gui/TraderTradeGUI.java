package com.z0cken.mc.economy.shops.gui;

import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.economy.shops.Trader;
import com.z0cken.mc.economy.utils.MessageBuilder;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import sun.security.krb5.Config;

import java.util.Arrays;
import java.util.List;

public class TraderTradeGUI{

    private Trader trader;

    public TraderTradeGUI(Trader trader){
        this.trader = trader;
    }

    public Inventory getInventory(Material material, Player player) {
        String title = trader.getTraderName() + "-" + WordUtils.capitalizeFully(material.name().replace("_", " "));
        Inventory inv = PCS_Economy.pcs_economy.getServer().createInventory(null, 27, title);

        String description = null;
        String empty = "";
        player.sendMessage(String.valueOf(ConfigManager.tradeInformation));
        String information = MessageBuilder.buildMessage(false, ConfigManager.tradeInformation);
        String quantity = null;
        String cost = null;
        String sell = null;
        ItemStack[] slots = new ItemStack[6];
        for(int i = 0; i < slots.length; i++){
            slots[i] = new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1);
            ItemMeta meta = slots[i].getItemMeta();
            switch (i){
                case 0:
                    meta.setDisplayName(MessageBuilder.buildMessage(false, ConfigManager.tradeSellTitleSingle));
                    description = MessageBuilder.buildMessage(false, ConfigManager.tradeSellDescriptionSingle);
                    quantity = MessageBuilder.buildMessage(false, ConfigManager.tradeQuantity, 1);
                    cost = MessageBuilder.buildMessage(false, ConfigManager.tradeCost, PCS_Economy.pcs_economy.adminShopItemManager.getTradeItem(material).getSellprice(), 0);
                    sell = MessageBuilder.buildMessage(false, ConfigManager.tradeSell);
                    break;
                case 1:
                    meta.setDisplayName(MessageBuilder.buildMessage(false, ConfigManager.tradeSellTitleStack));
                    description = MessageBuilder.buildMessage(false, ConfigManager.tradeSellDescriptionStack);
                    quantity = MessageBuilder.buildMessage(false, ConfigManager.tradeQuantity, 64);
                    cost = MessageBuilder.buildMessage(false, ConfigManager.tradeCost, PCS_Economy.pcs_economy.adminShopItemManager.getTradeItem(material).getSellprice() * 64, 0);
                    sell = MessageBuilder.buildMessage(false, ConfigManager.tradeSell);
                    break;
                case 2:
                    meta.setDisplayName(MessageBuilder.buildMessage(false, ConfigManager.tradeSellTitleInv));
                    description = MessageBuilder.buildMessage(false, ConfigManager.tradeSellDescriptionInv);
                    quantity = MessageBuilder.buildMessage(false, ConfigManager.tradeQuantity, 123);
                    cost = MessageBuilder.buildMessage(false, ConfigManager.tradeCost, PCS_Economy.pcs_economy.adminShopItemManager.getTradeItem(material).getSellprice() * 123, 0);
                    sell = MessageBuilder.buildMessage(false, ConfigManager.tradeSell);
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
            }
            List<String> lore;
            lore = Arrays.asList(description, empty, information, quantity, cost, empty, sell);
            meta.setLore(lore);
            slots[i].setItemMeta(meta);
        }
        ItemStack buySlotSingle = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
        ItemStack buySlotStack = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
        ItemStack buySlotInv = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);

        inv.setItem(0, slots[0]);
        inv.setItem(1, slots[0]);
        inv.setItem(2, slots[0]);

        inv.setItem(9, slots[1]);
        inv.setItem(10, slots[1]);
        inv.setItem(11, slots[1]);

        inv.setItem(18, slots[2]);
        inv.setItem(19, slots[2]);
        inv.setItem(20, slots[2]);

        inv.setItem(6, buySlotSingle);
        inv.setItem(7, buySlotSingle);
        inv.setItem(8, buySlotSingle);

        inv.setItem(15, buySlotStack);
        inv.setItem(16, buySlotStack);
        inv.setItem(17, buySlotStack);

        inv.setItem(24, buySlotInv);
        inv.setItem(25, buySlotInv);
        inv.setItem(26, buySlotInv);

        ItemStack itemOfInterest = new ItemStack(material, 1);
        inv.setItem(4, itemOfInterest);

        ItemStack backItem = new ItemStack(Material.END_PORTAL_FRAME, 1);
        inv.setItem(22, backItem);

        return inv;
    }
}
