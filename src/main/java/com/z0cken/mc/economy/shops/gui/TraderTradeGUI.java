package com.z0cken.mc.economy.shops.gui;

import com.z0cken.mc.economy.Account;
import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.economy.shops.*;
import com.z0cken.mc.economy.utils.MessageBuilder;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.List;

public class TraderTradeGUI{

    private Trader trader;
    private static int[] singleSellSlots = TradeInventorySlotType.SLOT_SELL_SINGLE.getSlots();
    private static int[] stackSellSlots = TradeInventorySlotType.SLOT_SELL_STACK.getSlots();
    private static int[] invSellSlots = TradeInventorySlotType.SLOT_SELL_INV.getSlots();
    private static int[] singleBuySlots = TradeInventorySlotType.SLOT_BUY_SINGLE.getSlots();
    private static int[] stackBuySlots = TradeInventorySlotType.SLOT_BUY_STACK.getSlots();
    private static int[] invBuySlots = TradeInventorySlotType.SLOT_BUY_INV.getSlots();

    public TraderTradeGUI(Trader trader){
        this.trader = trader;
    }

    public Inventory getInventory(TradeItem item, Player player) {
        String title = trader.getTraderName() + "-" + WordUtils.capitalizeFully(item.getMaterial().name().replace("_", " "));
        Inventory inv = PCS_Economy.pcs_economy.getServer().createInventory(null, 27, title);

        normalSingleSell(inv);
        normalStackSell(inv);
        normalInvSell(inv);
        normalSingleBuy(inv);
        normalStackBuy(inv);
        normalInvBuy(inv);

        if(!item.isBuyable()){
            grayAllBuy(inv);
        }
        if(!item.isSellable()){
            grayAllSell(inv);
        }

        changeLoreGeneral(inv, player, item);

        ItemStack itemOfInterest = new ItemStack(item.getMaterial(), 1);
        inv.setItem(13, itemOfInterest);

        return inv;
    }

    public static void changeLoreSingleSell(Inventory inv, TradeItem item){
        if(inv != null && item != null){
            normalSingleSell(inv);
            List<String> lore = new LoreBuilder(true)
                    .setItem(item)
                    .setQuantity(1)
                    .addDescription(ConfigManager.tradeSellDescriptionSingle)
                    .addEmpty()
                    .addInformation()
                    .addQuantity()
                    .addTradeCost()
                    .addEmpty()
                    .addSellBuy(ConfigManager.tradeSell).buildLore();
            changeLoreWithList(inv, TradeInventorySlotType.SLOT_SELL_SINGLE, lore);
        }
    }

    public static void changeLoreStackSell(Inventory inv, TradeItem item){
        if(inv != null && item != null){
            normalStackSell(inv);
            List<String> lore = new LoreBuilder(true)
                    .setItem(item)
                    .setQuantity(item.getMaterial().getMaxStackSize())
                    .addDescription(ConfigManager.tradeSellDescriptionStack)
                    .addEmpty()
                    .addInformation()
                    .addQuantity()
                    .addTradeCost()
                    .addEmpty()
                    .addSellBuy(ConfigManager.tradeSell).buildLore();
            changeLoreWithList(inv, TradeInventorySlotType.SLOT_SELL_STACK, lore);
        }
    }

    public static void changeLoreInvSell(Inventory inv, Player player, TradeItem item){
        if(player != null && inv != null && item != null){
            normalInvSell(inv);
            List<String> lore = new LoreBuilder(true)
                    .setItem(item)
                    .setQuantity(InventoryHelper.getOverallItemCapacity(player.getInventory(), item.getMaterial()))
                    .addDescription(ConfigManager.tradeSellDescriptionInv)
                    .addEmpty()
                    .addInformation()
                    .addQuantity()
                    .addTradeCost()
                    .addEmpty()
                    .addSellBuy(ConfigManager.tradeSell).buildLore();
            changeLoreWithList(inv, TradeInventorySlotType.SLOT_SELL_INV, lore);
        }
    }

    public static void changeLoreSingleBuy(Inventory inv, TradeItem item){
        if(inv != null && item != null){
            normalSingleBuy(inv);
            List<String> lore = new LoreBuilder(false)
                    .setItem(item)
                    .setQuantity(1)
                    .addDescription(ConfigManager.tradeBuyDescriptionSingle)
                    .addEmpty()
                    .addInformation()
                    .addQuantity()
                    .addTradeCost()
                    .addEmpty()
                    .addSellBuy(ConfigManager.tradeBuy).buildLore();
            changeLoreWithList(inv, TradeInventorySlotType.SLOT_BUY_SINGLE, lore);
        }
    }

    public static void changeLoreStackBuy(Inventory inv, TradeItem item){
        if(inv != null && item != null){
            normalStackBuy(inv);
            List<String> lore = new LoreBuilder(false)
                    .setItem(item)
                    .setQuantity(item.getMaterial().getMaxStackSize())
                    .addDescription(ConfigManager.tradeBuyDescriptionStack)
                    .addEmpty()
                    .addInformation()
                    .addQuantity()
                    .addTradeCost()
                    .addEmpty()
                    .addSellBuy(ConfigManager.tradeBuy).buildLore();
            changeLoreWithList(inv, TradeInventorySlotType.SLOT_BUY_STACK, lore);
        }
    }

    public static void changeLoreInvBuy(Inventory inv, Player player, TradeItem item){
        if(player != null && inv != null && item != null){
            normalInvBuy(inv);
            List<String> lore = new LoreBuilder(false)
                    .setItem(item)
                    .setQuantity(InventoryHelper.getAmountOfItem(player.getInventory(), item.getMaterial()))
                    .addDescription(ConfigManager.tradeBuyDescriptionInv)
                    .addEmpty()
                    .addInformation()
                    .addQuantity()
                    .addTradeCost()
                    .addEmpty()
                    .addSellBuy(ConfigManager.tradeBuy).buildLore();
            changeLoreWithList(inv, TradeInventorySlotType.SLOT_BUY_INV, lore);
        }
    }

    /*public static void changeLoreItemNotSellable(Inventory inv){
        if(inv != null){
            graySingleSell(inv);
            grayStackSell(inv);
            grayInvSell(inv);
            List<String> lore = new LoreBuilder(false)
                    .addErrorCantSell().buildLore();
            changeLoreWithList(inv, TradeInventorySlotType.SLOT_ALL_SELL, lore);
        }
    }

    public static void changeLoreItemNotBuyable(Inventory inv){
        if(inv != null){
            graySingleBuy(inv);
            grayStackBuy(inv);
            grayInvSell(inv);
            List<String> lore = new LoreBuilder(false)
                    .addErrorCantBuy().buildLore();
            changeLoreWithList(inv, TradeInventorySlotType.SLOT_ALL_BUY, lore);
        }
    }*/

    public static void changeLoreWithList(Inventory inv, TradeInventorySlotType slotType, List<String> lore){
        if(inv != null && slotType != null && lore != null){
            ItemMeta meta = null;
            for(int i : slotType.getSlots()){
                ItemStack stack = inv.getItem(i);
                if(stack != null){
                    meta = stack.getItemMeta();
                    meta.setLore(lore);
                    stack.setItemMeta(meta);
                }
            }
        }
    }

    public static void changeLoreGeneral(Inventory inv, Player player, TradeItem item){
        if(inv != null && player != null && item != null){
            if(item.isSellable()){
                changeLoreSingleSell(inv, item);
                changeLoreStackSell(inv, item);
                changeLoreInvSell(inv, player, item);
            }
            if(item.isBuyable()){
                changeLoreSingleBuy(inv, item);
                changeLoreStackBuy(inv, item);
                changeLoreInvBuy(inv, player, item);
            }
        }
    }

    public static void changeLoreBasic(Inventory inv, TradeInventorySlotType slotType, String message){
        if(inv != null && slotType != null && message != null){
            changeLoreWithList(inv, slotType, new LoreBuilder(true).addCustom(message).buildLore());
        }
    }

    public static void changeLorePlayerInvFull(Inventory inv, TradeInventorySlotType slotType){
        if(inv != null && slotType != null){
            changeLoreBasic(inv, slotType, ConfigManager.tradeInventoryFull);
        }
    }

    public static void changeLorePlayerInvNotEnoughItems(Inventory inv, TradeInventorySlotType slotType){
        if(inv != null && slotType != null){
            changeLoreBasic(inv, slotType, ConfigManager.tradeNotEnoughItems);
        }
    }

    public static void changeLorePlayerNotEnoughFunds(Inventory inv, TradeInventorySlotType slotType){
        if(inv != null && slotType != null){
            changeLoreBasic(inv, slotType, ConfigManager.tradeNotEnoughFunds);
        }
    }

    private static void normalSingleSell(Inventory inv){
        for(int i : singleSellSlots){
            ItemStack stack = new ItemStack(Material.GREEN_STAINED_GLASS, 1);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(ConfigManager.tradeSellTitleSingle);
            stack.setItemMeta(meta);
            inv.setItem(i, stack);
        }
    }

    private static void normalStackSell(Inventory inv){
        for(int i : stackSellSlots){
            ItemStack stack = new ItemStack(Material.GREEN_STAINED_GLASS, 64);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(ConfigManager.tradeSellTitleStack);
            stack.setItemMeta(meta);
            inv.setItem(i, stack);
        }
    }

    private static void normalInvSell(Inventory inv){
        for(int i : invSellSlots){
            ItemStack stack = new ItemStack(Material.GREEN_SHULKER_BOX, 1);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(ConfigManager.tradeSellTitleInv);
            stack.setItemMeta(meta);
            inv.setItem(i, stack);
        }
    }

    private static void normalSingleBuy(Inventory inv){
        for(int i : singleBuySlots){
            ItemStack stack = new ItemStack(Material.RED_STAINED_GLASS, 1);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(ConfigManager.tradeBuyTitleSingle);
            stack.setItemMeta(meta);
            inv.setItem(i, stack);
        }
    }

    private static void normalStackBuy(Inventory inv){
        for(int i : stackBuySlots){
            ItemStack stack = new ItemStack(Material.RED_STAINED_GLASS, 64);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(ConfigManager.tradeBuyTitleStack);
            stack.setItemMeta(meta);
            inv.setItem(i, stack);
        }
    }

    private static void normalInvBuy(Inventory inv){
        for(int i : invBuySlots){
            ItemStack stack = new ItemStack(Material.RED_SHULKER_BOX, 1);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(ConfigManager.tradeBuyTitleInv);
            stack.setItemMeta(meta);
            inv.setItem(i, stack);
        }
    }

    private static void graySingleSell(Inventory inv){
        for(int i : singleSellSlots){
            ItemStack stack = new ItemStack(Material.GRAY_STAINED_GLASS, 1);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(ConfigManager.tradeSellTitleSingle);
            stack.setItemMeta(meta);
            inv.setItem(i, stack);
        }
    }

    private static void grayStackSell(Inventory inv){
        for(int i : stackSellSlots){
            ItemStack stack = new ItemStack(Material.GRAY_STAINED_GLASS, 64);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(ConfigManager.tradeSellTitleStack);
            stack.setItemMeta(meta);
            inv.setItem(i, stack);
        }
    }

    private static void grayInvSell(Inventory inv){
        for(int i : invSellSlots){
            ItemStack stack = new ItemStack(Material.GRAY_SHULKER_BOX, 1);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(ConfigManager.tradeSellTitleInv);
            stack.setItemMeta(meta);
            inv.setItem(i, stack);
        }
    }

    private static void graySingleBuy(Inventory inv){
        for(int i : singleBuySlots){
            ItemStack stack = new ItemStack(Material.GRAY_STAINED_GLASS, 1);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(ConfigManager.tradeBuyTitleSingle);
            stack.setItemMeta(meta);
            inv.setItem(i, stack);
        }
    }

    private static void grayStackBuy(Inventory inv){
        for(int i : stackBuySlots){
            ItemStack stack = new ItemStack(Material.GRAY_STAINED_GLASS, 64);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(ConfigManager.tradeBuyTitleStack);
            stack.setItemMeta(meta);
            inv.setItem(i, stack);
        }
    }

    private static void grayInvBuy(Inventory inv){
        for(int i : invBuySlots){
            ItemStack stack = new ItemStack(Material.GRAY_SHULKER_BOX, 1);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(ConfigManager.tradeBuyTitleInv);
            stack.setItemMeta(meta);
            inv.setItem(i, stack);
        }
    }

    @SuppressWarnings("Duplicates")
    private static void grayAllSell(Inventory inv){
        for(int i : TradeInventorySlotType.SLOT_ALL_SELL.getSlots()){
            if(i != 16){
                ItemStack stack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE,1);
                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(ConfigManager.tradeSellErrorCantSell);
                stack.setItemMeta(meta);
                inv.setItem(i, stack);
            }else{
                ItemStack stack = new ItemStack(Material.BARRIER, 1);
                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(ConfigManager.tradeSellErrorCantSell);
                stack.setItemMeta(meta);
                inv.setItem(i, stack);
            }
        }
    }

    @SuppressWarnings("Duplicates")
    private static void grayAllBuy(Inventory inv){
        for(int i : TradeInventorySlotType.SLOT_ALL_BUY.getSlots()){
            if(i != 10){
                ItemStack stack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE,1);
                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(ConfigManager.tradeBuyErrorCantBuy);
                stack.setItemMeta(meta);
                inv.setItem(i, stack);
            }else{
                ItemStack stack = new ItemStack(Material.BARRIER,1);
                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(ConfigManager.tradeBuyErrorCantBuy);
                stack.setItemMeta(meta);
                inv.setItem(i, stack);
            }
        }
    }

    public static void doInvLogic(Inventory inv, TradeItem item, Account account){
        Player player = account.getHolder().getPlayer();
        changeLoreGeneral(inv, player, item);
        //SELL
        if(item.isSellable()){
            //SINGLE
            if(account.has(item.getSellprice())){
                if(!InventoryHelper.canFit(inv, item.getMaterial(), 1)){
                    graySingleSell(inv);
                    grayStackSell(inv);
                    grayInvSell(inv);
                    changeLorePlayerInvFull(inv, TradeInventorySlotType.SLOT_ALL_SELL);
                }
            }else{
                graySingleSell(inv);
                grayStackSell(inv);
                grayInvSell(inv);
                changeLorePlayerNotEnoughFunds(inv, TradeInventorySlotType.SLOT_ALL_SELL);
            }
            //STACK
            if(account.has(item.getSellprice() * item.getMaterial().getMaxStackSize())){
                if(!InventoryHelper.canFit(inv, item.getMaterial(), item.getMaterial().getMaxStackSize())){
                    grayStackSell(inv);
                    changeLorePlayerInvFull(inv, TradeInventorySlotType.SLOT_SELL_STACK);
                }
            }else{
                grayStackSell(inv);
                changeLorePlayerNotEnoughFunds(inv, TradeInventorySlotType.SLOT_SELL_STACK);
            }
            //INV
            int capacity = InventoryHelper.getOverallItemCapacity(inv, item.getMaterial());
            if(capacity == 0){
                graySingleSell(inv);
                grayStackSell(inv);
                grayInvSell(inv);
                changeLorePlayerInvFull(inv, TradeInventorySlotType.SLOT_ALL_SELL);
            }else{
                if(!account.has(item.getSellprice() * capacity)){
                    grayInvSell(inv);
                    changeLorePlayerNotEnoughFunds(inv, TradeInventorySlotType.SLOT_SELL_INV);
                }
            }
        }
        if(item.isBuyable()){
            //BUY
            int itemAmount = InventoryHelper.getAmountOfItem(player.getInventory(), item.getMaterial());
            //SINGLE
            if(itemAmount == 0){
                graySingleBuy(inv);
                grayStackBuy(inv);
                grayInvBuy(inv);
                changeLorePlayerInvNotEnoughItems(inv, TradeInventorySlotType.SLOT_ALL_BUY);
            }
            //STACK
            if(itemAmount < item.getMaterial().getMaxStackSize()){
                grayStackBuy(inv);
                changeLorePlayerInvNotEnoughItems(inv, TradeInventorySlotType.SLOT_BUY_STACK);
            }
        }
    }
}
