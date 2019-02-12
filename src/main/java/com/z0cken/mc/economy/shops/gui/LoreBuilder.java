package com.z0cken.mc.economy.shops.gui;

import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.economy.utils.MessageBuilder;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoreBuilder {
    private String description;
    private int quantity;
    private String sellBuy;
    private Material material;
    private boolean sell;

    public LoreBuilder(boolean sell){
        this.sell = sell;
    }

    public LoreBuilder setDescription(String description){
        this.description = description;
        return this;
    }

    public LoreBuilder setQuantity(int quantity){
        this.quantity = quantity;
        return this;
    }

    public LoreBuilder setSellBuy(String sellBuy){
        this.sellBuy = sellBuy;
        return this;
    }

    public LoreBuilder setMaterial(Material mat){
        this.material = mat;
        return this;
    }

    public List<String> buildLore(){
        String builtDescription = MessageBuilder.buildMessage(false, this.description);
        String builtInformation = MessageBuilder.buildMessage(false, ConfigManager.tradeInformation);
        String builtQuantity = MessageBuilder.buildMessage(false, ConfigManager.tradeQuantity, this.quantity);
        int tradeCost = 0;
        if(this.sell){
            tradeCost = PCS_Economy.pcs_economy.adminShopItemManager.getTradeItem(material).getSellprice() * this.quantity;
        }else{
            tradeCost = PCS_Economy.pcs_economy.adminShopItemManager.getTradeItem(material).getBuyPrice() * this.quantity;
        }
        String builtCost = MessageBuilder.buildMessage(false, ConfigManager.tradeCost, tradeCost, 0);
        String builtSellBuy = MessageBuilder.buildMessage(false, sellBuy);
        String empty = "";
        List<String> lore = Arrays.asList(builtDescription, empty, builtInformation, builtQuantity, builtCost, empty, builtSellBuy);
        return lore;
    }
}
