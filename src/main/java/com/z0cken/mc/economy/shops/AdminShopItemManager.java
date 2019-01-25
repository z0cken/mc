package com.z0cken.mc.economy.shops;

import com.z0cken.mc.economy.PCS_Economy;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

public class AdminShopItemManager {
    private PCS_Economy pcs_economy;
    private ArrayList<TradeItem> tradeItems;
    private File adminShopItemDataFile;
    public AdminShopItemManager(PCS_Economy pcs_economy){
        this.pcs_economy = pcs_economy;
        this.tradeItems = new ArrayList<>();
        this.adminShopItemDataFile = new File(pcs_economy.getDataFolder() + "/adminShopConfig.yml");
    }

    public ArrayList<Material> createMatList(){
        ArrayList<Material> matList = new ArrayList<>();
        for(Material mat : Material.values()){
            matList.add(mat);
        }
        return matList;
    }

    public void loadConfig(){
        if(!adminShopItemDataFile.exists()){
            writeConfig();
        }
        FileConfiguration adminShopItemDataConfig = YamlConfiguration.loadConfiguration(adminShopItemDataFile);
        ArrayList<String> stringList = (ArrayList<String>)adminShopItemDataConfig.get("items");
        stringList.forEach(s -> tradeItems.add(parseToTradeItem(s)));
        if(tradeItems.size() != createMatList().size()){
            writeConfig();
        }
    }

    public void writeConfig(){
        if(!adminShopItemDataFile.exists()){
            ArrayList<String> strings = new ArrayList<>();
            createMatList().forEach(mat -> strings.add(mat.name() + "|0|0|00"));
            saveConfigToFile(strings);
        }else{
            pcs_economy.getLogger().info("Materials in AdminShopConfig missing. Adding materials.");

            ArrayList<Material> mats = createMatList();
            ArrayList<Material> tradeItemMats = new ArrayList<>();
            tradeItems.forEach(item -> tradeItemMats.add(item.getMaterial()));
            mats.forEach(mat -> {
                if(!tradeItemMats.contains(mat)){
                    tradeItems.add(new TradeItem(mat, 0, 0, false, false, 0));
                }
            });
        }
    }

    public void saveConfig(){
        ArrayList<String> strings = new ArrayList<>();
        tradeItems.forEach(item -> strings.add(item.toString()));
        saveConfigToFile(strings);
    }

    public void saveConfigToFile(ArrayList<String> strings){
        FileConfiguration adminShopItemDataConfig = YamlConfiguration.loadConfiguration(adminShopItemDataFile);
        adminShopItemDataConfig.set("items", strings);
        try{
            adminShopItemDataConfig.save(adminShopItemDataFile);
        }catch (IOException e){
            pcs_economy.getLogger().log(Level.SEVERE, e.getMessage());
        }
    }

    public TradeItem parseToTradeItem(String configString){
        Material material;
        int sellPrice;
        int buyPrice;
        boolean canSell = false;
        boolean canBuy = false;

        String[] components = configString.split("\\|");
        if(components.length == 4){
            material = Material.getMaterial(components[0]);
            sellPrice = Integer.valueOf(components[1]);
            buyPrice = Integer.valueOf(components[2]);
            switch (components[3]){
                case "01":
                    canBuy = true;
                    break;
                case "10":
                    canSell = true;
                    break;
                case "11":
                    canSell = true;
                    canBuy = true;
                    break;
            }
            return new TradeItem(material, buyPrice, sellPrice, canBuy, canSell, 0);
        }
        return null;
    }

    public ArrayList<TradeItem> getTradeItems(){
        return this.tradeItems;
    }

    public TradeItem getTradeItem(Material mat){
        return tradeItems.stream().filter(i -> i.getMaterial() == mat).findFirst().orElse(null);
    }

    public TradeItem getTradeItem(String matName){
        return tradeItems.stream().filter(i -> i.getMaterial().name().equals(matName)).findFirst().orElse(null);
    }
}
