package com.z0cken.mc.economy.shops;

import org.bukkit.Material;

public class TradeItem {
    private transient Material mat;
    private String matName;
    private int buyPrice;
    private int sellPrice;
    private boolean canBuy;
    private boolean canSell;
    private int amount;

    public TradeItem(Material mat, int buyPrice, int sellPrice, boolean canBuy, boolean canSell, int amount){
        this.mat = mat;
        this.matName = mat.name();
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.canBuy = canBuy;
        this.canSell = canSell;
        this.amount = amount;
    }

    public void setMat(Material mat){
        this.mat = mat;
    }

    public void setBuyPrice(int price){
        this.buyPrice = price;
    }

    public void setSellPrice(int price){
        this.sellPrice = price;
    }

    public Material getMaterial(){
        if(this.mat == null){
            return Material.getMaterial(this.matName);
        }
        return this.mat;
    }

    public int getBuyPrice(){
        return this.buyPrice;
    }

    public int getSellprice(){
        return this.sellPrice;
    }

    public int getAmount(){
        return this.amount;
    }

    public void setAmount(int amount){
        this.amount = amount;
    }

    public String getMatName(){
        return this.matName;
    }

    public boolean isSellable(){
        return this.canSell;
    }

    public boolean isBuyable(){
        return this.canBuy;
    }

    public static TradeItem createTradeItem(String confString){
        return null;
    }

    @Override
    public String toString(){
        String s = matName + "|" + sellPrice + "|" + buyPrice + "|";
        s += (canSell ? 1 : 0) + (canBuy ? 1 : 0);
        return s;
    }
}
