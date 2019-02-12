package com.z0cken.mc.economy.shops.gui;

import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.economy.shops.InventoryHelper;
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

        int freeSlots = getFreeSlots(player);

        String description = null;
        String empty = "";
        String information = MessageBuilder.buildMessage(false, ConfigManager.tradeInformation);
        String quantity = null;
        int tradeCost = 0;
        String cost = null;
        String sell = null;
        ItemStack[] slots = new ItemStack[6];
        List<String> lore = null;
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
                    lore = new LoreBuilder(true).setDescription(ConfigManager.tradeSellDescriptionSingle)
                                            .setMaterial(material)
                                            .setQuantity(1)
                                            .setSellBuy(ConfigManager.tradeSell).buildLore();
                    break;
                case 1:
                    meta.setDisplayName(MessageBuilder.buildMessage(false, ConfigManager.tradeSellTitleStack));
                    lore = new LoreBuilder(true).setDescription(ConfigManager.tradeSellDescriptionStack)
                                            .setMaterial(material)
                                            .setQuantity(material.getMaxStackSize())
                                            .setSellBuy(ConfigManager.tradeSell).buildLore();
                    break;
                case 2:
                    meta.setDisplayName(MessageBuilder.buildMessage(false, ConfigManager.tradeSellTitleInv));
                    int capacity = InventoryHelper.getOverallItemCapacity(player.getInventory(), material);
                    lore = new LoreBuilder(true).setDescription(ConfigManager.tradeSellDescriptionInv)
                                            .setMaterial(material)
                                            .setQuantity(capacity)
                                            .setSellBuy(ConfigManager.tradeSell).buildLore();
                    break;
                case 3:
                    meta.setDisplayName(MessageBuilder.buildMessage(false, ConfigManager.tradeBuyTitleSingle));
                    lore = new LoreBuilder(false).setDescription(ConfigManager.tradeBuyDescriptionSingle)
                                            .setMaterial(material)
                                            .setQuantity(1)
                                            .setSellBuy(ConfigManager.tradeBuy).buildLore();
                    break;
                case 4:
                    meta.setDisplayName(MessageBuilder.buildMessage(false, ConfigManager.tradeBuyTitleStack));
                    lore = new LoreBuilder(false).setDescription(ConfigManager.tradeBuyDescriptionStack)
                                            .setMaterial(material)
                                            .setQuantity(material.getMaxStackSize())
                                            .setSellBuy(ConfigManager.tradeBuy).buildLore();
                    break;
                case 5:
                    meta.setDisplayName(MessageBuilder.buildMessage(false, ConfigManager.tradeBuyTitleInv));
                    lore = new LoreBuilder(false).setDescription(ConfigManager.tradeBuyDescriptionInv)
                                            .setMaterial(material)
                                            .setQuantity(123)
                                            .setSellBuy(ConfigManager.tradeBuy).buildLore();
                    break;
            }
            meta.setLore(lore);
            slots[i].setItemMeta(meta);
        }

        //SELL
        inv.setItem(0, slots[0]);
        inv.setItem(1, slots[0]);
        inv.setItem(2, slots[0]);

        inv.setItem(9, slots[1]);
        inv.setItem(10, slots[1]);
        inv.setItem(11, slots[1]);

        inv.setItem(18, slots[2]);
        inv.setItem(19, slots[2]);
        inv.setItem(20, slots[2]);

        //BUY
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

    private static int getFreeSlots(Player player){
        int freeSlots = 0;
        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if(stack == null || stack.getType() == Material.AIR){
                freeSlots += 1;
            }
        }
        return freeSlots;
    }

    //TODO CHANGE LORE
    public static void changeLoreInvSell(Inventory inv, Player player, Material material){
        if(player != null && inv != null && material != null){
            List<String> lore = new LoreBuilder(true)
                    .setDescription(ConfigManager.tradeSellDescriptionInv)
                    .setMaterial(material)
                    .setQuantity(InventoryHelper.getOverallItemCapacity(player.getInventory(), material))
                    .setSellBuy(ConfigManager.tradeSell)
                    .buildLore();
            for (int i : TradeInventorySlotType.SLOT_SELL_INV.getSlots()) {
                ItemMeta meta = inv.getItem(i).getItemMeta();
                meta.setLore(lore);
                inv.getItem(i).setItemMeta(meta);
            }
        }
    }

    public void changeLoreTraderInvFull(){

    }

    public void changeLoreTraderInvEmpty(){

    }

    public void changeLorePlayerInvFull(){

    }

    public void changeLorePlayerInvEmpty(){

    }
}
