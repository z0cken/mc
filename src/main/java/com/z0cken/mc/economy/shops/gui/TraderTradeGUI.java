package com.z0cken.mc.economy.shops.gui;

import com.z0cken.mc.economy.Account;
import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.economy.shops.InventoryHelper;
import com.z0cken.mc.economy.shops.TradeItem;
import com.z0cken.mc.economy.shops.Trader;
import com.z0cken.mc.economy.utils.MessageBuilder;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class TraderTradeGUI{

    private Trader trader;

    public TraderTradeGUI(Trader trader){
        this.trader = trader;
    }

    public Inventory getInventory(TradeItem item, Player player) {
        String title = trader.getTraderName() + "-" + WordUtils.capitalizeFully(item.getMaterial().name().replace("_", " "));
        Inventory inv = PCS_Economy.pcs_economy.getServer().createInventory(null, 27, title);
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
                    break;
                case 1:
                    meta.setDisplayName(MessageBuilder.buildMessage(false, ConfigManager.tradeSellTitleStack));
                    break;
                case 2:
                    meta.setDisplayName(MessageBuilder.buildMessage(false, ConfigManager.tradeSellTitleInv));
                    break;
                case 3:
                    meta.setDisplayName(MessageBuilder.buildMessage(false, ConfigManager.tradeBuyTitleSingle));
                    break;
                case 4:
                    meta.setDisplayName(MessageBuilder.buildMessage(false, ConfigManager.tradeBuyTitleStack));
                    break;
                case 5:
                    meta.setDisplayName(MessageBuilder.buildMessage(false, ConfigManager.tradeBuyTitleInv));
                    break;
            }
            slots[i].setItemMeta(meta);
        }

        changeLoreGeneral(inv, player, item);

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

        ItemStack itemOfInterest = new ItemStack(item.getMaterial(), 1);
        inv.setItem(13, itemOfInterest);

        return inv;
    }

    public static void changeLoreSingleSell(Inventory inv, TradeItem item){
        if(inv != null && item != null){
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

    public static void changeLoreItemNotSellable(Inventory inv){
        if(inv != null){
            List<String> lore = new LoreBuilder(false)
                    .addErrorCantSell().buildLore();
            changeLoreWithList(inv, TradeInventorySlotType.SLOT_ALL_SELL, lore);
        }
    }

    public static void changeLoreItemNotBuyable(Inventory inv){
        if(inv != null){
            List<String> lore = new LoreBuilder(false)
                    .addErrorCantBuy().buildLore();
            changeLoreWithList(inv, TradeInventorySlotType.SLOT_ALL_BUY, lore);
        }
    }

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
            }else{
                changeLoreItemNotSellable(inv);
            }
            if(item.isBuyable()){
                changeLoreSingleBuy(inv, item);
                changeLoreStackBuy(inv, item);
                changeLoreInvBuy(inv, player, item);
            }else{
                changeLoreItemNotBuyable(inv);
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

    public static void doInvLogic(Inventory inv, TradeItem item, Account account){
        Player player = account.getHolder().getPlayer();
        changeLoreGeneral(inv, player, item);
        //SELL
        if(item.isSellable()){
            //SINGLE
            if(account.has(item.getSellprice())){
                if(!InventoryHelper.canFit(inv, item.getMaterial(), 1)){
                    changeLorePlayerInvFull(inv, TradeInventorySlotType.SLOT_ALL_SELL);
                }
            }else{
                changeLorePlayerNotEnoughFunds(inv, TradeInventorySlotType.SLOT_ALL_SELL);
            }
            //STACK
            if(account.has(item.getSellprice() * item.getMaterial().getMaxStackSize())){
                if(!InventoryHelper.canFit(inv, item.getMaterial(), item.getMaterial().getMaxStackSize())){
                    changeLorePlayerInvFull(inv, TradeInventorySlotType.SLOT_SELL_STACK);
                }
            }else{
                changeLorePlayerNotEnoughFunds(inv, TradeInventorySlotType.SLOT_SELL_STACK);
            }
            //INV
            int capacity = InventoryHelper.getOverallItemCapacity(inv, item.getMaterial());
            if(capacity == 0){
                changeLorePlayerInvFull(inv, TradeInventorySlotType.SLOT_ALL_SELL);
            }else{
                if(!account.has(item.getSellprice() * capacity)){
                    changeLorePlayerNotEnoughFunds(inv, TradeInventorySlotType.SLOT_SELL_INV);
                }
            }
        }
        if(item.isBuyable()){
            //BUY
            int itemAmount = InventoryHelper.getAmountOfItem(player.getInventory(), item.getMaterial());
            //SINGLE
            if(itemAmount == 0){
                changeLorePlayerInvNotEnoughItems(inv, TradeInventorySlotType.SLOT_ALL_BUY);
            }
            //STACK
            if(itemAmount < item.getMaterial().getMaxStackSize()){
                changeLorePlayerInvNotEnoughItems(inv, TradeInventorySlotType.SLOT_BUY_STACK);
            }
        }
    }
}
