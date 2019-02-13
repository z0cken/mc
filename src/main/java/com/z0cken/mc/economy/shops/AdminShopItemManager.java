package com.z0cken.mc.economy.shops;

import com.z0cken.mc.economy.PCS_Economy;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class AdminShopItemManager {
    private PCS_Economy pcs_economy;
    private HashMap<Material, TradeItem> tradeItems;
    private File adminShopItemDataFile;
    public AdminShopItemManager(PCS_Economy pcs_economy){
        this.pcs_economy = pcs_economy;
        this.tradeItems = new HashMap<>();
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
        stringList.forEach(s ->
        {
            TradeItem item = parseToTradeItem(s);
            tradeItems.put(item.getMaterial(), item);
        });
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
            tradeItems.values().forEach(item -> tradeItemMats.add(item.getMaterial()));
            mats.forEach(mat -> {
                TradeItem item = null;
                if(!tradeItemMats.contains(mat)){
                    item = new TradeItem(mat, 0, 0, false, false, 0);
                    tradeItems.put(mat, item);
                }
            });
        }
    }

    public void saveConfig(){
        ArrayList<String> strings = new ArrayList<>();
        tradeItems.values().forEach(item -> strings.add(item.toString()));
        Collections.sort(strings, Comparator.naturalOrder());
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

    public HashMap<Material, TradeItem> getTradeItems(){
        return this.tradeItems;
    }

    public TradeItem getTradeItem(Material mat){
        return tradeItems.get(mat);
    }

    public TradeItem getTradeItem(String matName){
        return getTradeItem(Material.getMaterial(matName));
    }
}
