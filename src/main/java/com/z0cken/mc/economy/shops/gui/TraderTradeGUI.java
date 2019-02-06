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

        int freeSlots = 0;
        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if(stack == null || stack.getType() == Material.AIR){
                freeSlots += 1;
            }
        }

        String description = null;
        String empty = "";
        String information = MessageBuilder.buildMessage(false, ConfigManager.tradeInformation);
        String quantity = null;
        int tradeCost = 0;
        String cost = null;
        String sell = null;
        ItemStack[] slots = new ItemStack[6];
        for(int i = 0; i < slots.length; i++){
            if(i < 3){
                slots[i] = new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1);
            }else{
                slots[i] = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
            }
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
                    quantity = MessageBuilder.buildMessage(false, ConfigManager.tradeQuantity, material.getMaxStackSize());
                    tradeCost = PCS_Economy.pcs_economy.adminShopItemManager.getTradeItem(material).getSellprice() * material.getMaxStackSize();
                    cost = MessageBuilder.buildMessage(false, ConfigManager.tradeCost, tradeCost, 0);
                    sell = MessageBuilder.buildMessage(false, ConfigManager.tradeSell);
                    break;
                case 2:
                    meta.setDisplayName(MessageBuilder.buildMessage(false, ConfigManager.tradeSellTitleInv));
                    description = MessageBuilder.buildMessage(false, ConfigManager.tradeSellDescriptionInv);
                    quantity = MessageBuilder.buildMessage(false, ConfigManager.tradeQuantity, 123);
                    tradeCost = PCS_Economy.pcs_economy.adminShopItemManager.getTradeItem(material).getSellprice() * (freeSlots * material.getMaxStackSize());
                    cost = MessageBuilder.buildMessage(false, ConfigManager.tradeCost, tradeCost,0);
                    sell = MessageBuilder.buildMessage(false, ConfigManager.tradeSell);
                    break;
                case 3:
                    meta.setDisplayName(MessageBuilder.buildMessage(false, ConfigManager.tradeBuyTitleSingle));
                    description = MessageBuilder.buildMessage(false, ConfigManager.tradeBuyDescriptionSingle);
                    quantity = MessageBuilder.buildMessage(false, ConfigManager.tradeQuantity, 1);
                    cost = MessageBuilder.buildMessage(false, ConfigManager.tradeCost, PCS_Economy.pcs_economy.adminShopItemManager.getTradeItem(material).getBuyPrice(), 0);
                    sell = MessageBuilder.buildMessage(false, ConfigManager.tradeBuy);
                    break;
                case 4:
                    meta.setDisplayName(MessageBuilder.buildMessage(false, ConfigManager.tradeBuyTitleStack));
                    description = MessageBuilder.buildMessage(false, ConfigManager.tradeBuyDescriptionStack);
                    quantity = MessageBuilder.buildMessage(false, ConfigManager.tradeQuantity, material.getMaxStackSize());
                    tradeCost = PCS_Economy.pcs_economy.adminShopItemManager.getTradeItem(material).getBuyPrice() * material.getMaxStackSize();
                    cost = MessageBuilder.buildMessage(false, ConfigManager.tradeCost, tradeCost, 0);
                    sell = MessageBuilder.buildMessage(false, ConfigManager.tradeBuy);
                    break;
                case 5:
                    meta.setDisplayName(MessageBuilder.buildMessage(false, ConfigManager.tradeBuyTitleInv));
                    description = MessageBuilder.buildMessage(false, ConfigManager.tradeBuyDescriptionInv);
                    quantity = MessageBuilder.buildMessage(false, ConfigManager.tradeQuantity, 123);
                    tradeCost = PCS_Economy.pcs_economy.adminShopItemManager.getTradeItem(material).getBuyPrice() * (freeSlots * material.getMaxStackSize());
                    cost = MessageBuilder.buildMessage(false, ConfigManager.tradeCost, tradeCost, 0);
                    sell = MessageBuilder.buildMessage(false, ConfigManager.tradeBuy);
                    break;
            }
            List<String> lore;
            lore = Arrays.asList(description, empty, information, quantity, cost, empty, sell);
            meta.setLore(lore);
            slots[i].setItemMeta(meta);
        }

        inv.setItem(0, slots[0]);
        inv.setItem(1, slots[0]);
        inv.setItem(2, slots[0]);

        inv.setItem(9, slots[1]);
        inv.setItem(10, slots[1]);
        inv.setItem(11, slots[1]);

        inv.setItem(18, slots[2]);
        inv.setItem(19, slots[2]);
        inv.setItem(20, slots[2]);

        inv.setItem(6, slots[3]);
        inv.setItem(7, slots[3]);
        inv.setItem(8, slots[3]);

        inv.setItem(15, slots[4]);
        inv.setItem(16, slots[4]);
        inv.setItem(17, slots[4]);

        inv.setItem(24, slots[5]);
        inv.setItem(25, slots[5]);
        inv.setItem(26, slots[5]);

        ItemStack itemOfInterest = new ItemStack(material, 1);
        inv.setItem(13, itemOfInterest);

        return inv;
    }
}
