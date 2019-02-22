package com.z0cken.mc.economy.shops.gui;

import com.z0cken.mc.core.util.MessageBuilder;
import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.economy.shops.TradeItem;
import com.z0cken.mc.economy.utils.MessageHelper;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class LoreBuilder {
    private int quantity;
    private TradeItem item;
    private boolean sell;
    private ArrayList<String> lore;

    public LoreBuilder(boolean sell){
        this.sell = sell;
        lore = new ArrayList<>();
    }

    public LoreBuilder addDescription(String description){
        String builtDescription = MessageHelper.convertBcToString(PCS_Economy.pcs_economy.getMessageBuilder().build(description));
        lore.add(builtDescription);
        return this;
    }

    public LoreBuilder addInformation(){
        String builtInformation = MessageHelper.convertBcToString(PCS_Economy.pcs_economy.getMessageBuilder().build(ConfigManager.tradeInformation));
        lore.add(builtInformation);
        return this;
    }

    public LoreBuilder setQuantity(int quantity){
        this.quantity = quantity;
        return this;
    }

    public LoreBuilder addQuantity(){
        String builtQuantity = MessageHelper.convertBcToString(PCS_Economy.pcs_economy.getMessageBuilder()
                .define("AMOUNT", String.valueOf(this.quantity))
                .build(ConfigManager.tradeQuantity));
        lore.add(builtQuantity);
        return this;
    }

    public LoreBuilder addSellBuy(String sellBuy){
        String builtSellBuy = MessageHelper.convertBcToString(PCS_Economy.pcs_economy.getMessageBuilder().build(sellBuy));
        lore.add(builtSellBuy);
        return this;
    }

    public LoreBuilder addErrorCantSell(){
        String builtErrorCantSell = MessageHelper.convertBcToString(PCS_Economy.pcs_economy.getMessageBuilder().build(ConfigManager.tradeSellErrorCantSell));
        lore.add(builtErrorCantSell);
        return this;
    }

    public LoreBuilder addErrorCantBuy(){
        String builtErrorCantBuy = MessageHelper.convertBcToString(PCS_Economy.pcs_economy.getMessageBuilder().build(ConfigManager.tradeBuyErrorCantBuy));
        lore.add(builtErrorCantBuy);
        return this;
    }

    public LoreBuilder addTradeCost(){
        double tradeCost = 0;
        if(this.sell){
            tradeCost = this.item.getSellprice() * this.quantity;
        }else{
            tradeCost = this.item.getBuyPrice() * this.quantity;
        }
        tradeCost = MessageHelper.roundToTwoDecimals(tradeCost);
        String builtCost = MessageHelper.convertBcToString(PCS_Economy.pcs_economy.getMessageBuilder()
                .define("AMOUNT", String.valueOf(tradeCost))
                .build(ConfigManager.tradeCost));
        lore.add(builtCost);
        return this;
    }

    public LoreBuilder addEmpty(){
        lore.add("");
        return this;
    }

    public LoreBuilder addCustom(String line){
        lore.add(MessageHelper.convertBcToString(PCS_Economy.pcs_economy.getMessageBuilder().build(line)));
        return this;
    }

    public LoreBuilder setItem(TradeItem item){
        this.item = item;
        return this;
    }

    public List<String> buildLore(){
        return this.lore;
    }
}
