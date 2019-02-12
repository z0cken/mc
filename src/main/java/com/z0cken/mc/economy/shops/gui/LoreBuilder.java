package com.z0cken.mc.economy.shops.gui;

import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.economy.utils.MessageBuilder;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoreBuilder {
    private int quantity;
    private Material material;
    private boolean sell;
    private ArrayList<String> lore;

    public LoreBuilder(boolean sell){
        this.sell = sell;
        lore = new ArrayList<>();
    }

    public LoreBuilder addDescription(String description){
        String builtDescription = MessageBuilder.buildMessage(false, description);
        lore.add(builtDescription);
        return this;
    }

    public LoreBuilder addInformation(){
        String builtInformation = MessageBuilder.buildMessage(false, ConfigManager.tradeInformation);
        lore.add(builtInformation);
        return this;
    }

    public LoreBuilder setQuantity(int quantity){
        this.quantity = quantity;
        return this;
    }

    public LoreBuilder addQuantity(){
        String builtQuantity = MessageBuilder.buildMessage(false, ConfigManager.tradeQuantity, this.quantity);
        lore.add(builtQuantity);
        return this;
    }

    public LoreBuilder addSellBuy(String sellBuy){
        String builtSellBuy = MessageBuilder.buildMessage(false, sellBuy);
        lore.add(builtSellBuy);
        return this;
    }

    public LoreBuilder addTradeCost(){
        int tradeCost = 0;
        if(this.sell){
            tradeCost = PCS_Economy.pcs_economy.adminShopItemManager.getTradeItem(material).getSellprice() * this.quantity;
        }else{
            tradeCost = PCS_Economy.pcs_economy.adminShopItemManager.getTradeItem(material).getBuyPrice() * this.quantity;
        }
        String builtCost = MessageBuilder.buildMessage(false, ConfigManager.tradeCost, tradeCost, 0);
        lore.add(builtCost);
        return this;
    }

    public LoreBuilder addEmpty(){
        lore.add("");
        return this;
    }

    public LoreBuilder addCustom(String line){
        lore.add(MessageBuilder.buildMessage(false, line));
        return this;
    }

    public LoreBuilder setMaterial(Material mat){
        this.material = mat;
        return this;
    }

    public List<String> buildLore(){
        return this.lore;
    }
}
