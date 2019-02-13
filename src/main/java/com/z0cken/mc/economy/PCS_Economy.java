package com.z0cken.mc.economy;

import co.aikar.commands.BukkitCommandManager;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.z0cken.mc.core.util.MessageBuilder;
import com.z0cken.mc.economy.commands.MoneyCommand;
import com.z0cken.mc.economy.commands.ShopCommand;
import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.economy.events.InventoryListener;
import com.z0cken.mc.economy.events.PlayerListener;
import com.z0cken.mc.economy.impl.VaultConnector;
import com.z0cken.mc.economy.shops.AdminShopItemManager;
import com.z0cken.mc.economy.shops.Trader;
import com.z0cken.mc.economy.shops.TraderManager;
import com.z0cken.mc.economy.shops.InventoryManager;
import com.z0cken.mc.economy.utils.DatabaseHelper;
import com.z0cken.mc.core.Database;
import net.milkbowl.vault.economy.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.xml.crypto.Data;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class PCS_Economy extends JavaPlugin {

    public static PCS_Economy pcs_economy;
    public static MessageBuilder messageBuilder;
    public AccountManager accountManager;
    public TraderManager traderManager;
    private static BukkitCommandManager commandManager;
    public AdminShopItemManager adminShopItemManager;
    public InventoryManager inventoryManager;

    @Override
    public void onLoad(){
        pcs_economy = this;

        this.saveDefaultConfig();
        ConfigManager.loadConfig();
        this.messageBuilder = new MessageBuilder()
                .define("CSYMBOL", ChatColor.GOLD.toString() + ConfigManager.currencySymbol)
                .define("PREFIX", ConfigManager.messagePrefix);

        this.adminShopItemManager = new AdminShopItemManager(this);
        this.adminShopItemManager.loadConfig();

        this.inventoryManager = new InventoryManager();

        Gson gson = new Gson();
        File tradersJson = new File(this.getDataFolder() + "/traders.json");
        if(tradersJson.exists() && !tradersJson.isDirectory()){
            try(FileReader reader = new FileReader(tradersJson)){
                traderManager = gson.fromJson(reader, TraderManager.class);
            }catch(FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            traderManager = new TraderManager();
        }

        accountManager = new AccountManager();

        getLogger().info("Load Complete");
    }

    @Override
    public void onEnable(){
        DatabaseHelper.createTable();
        registerCommands();
        registerInterfaces();

        this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        this.getServer().getPluginManager().registerEvents(new InventoryListener(this), this);

        getLogger().info("Enabled");
    }

    @SuppressWarnings("Duplicates")
    private void registerCommands(){
        commandManager = new BukkitCommandManager(this);
        commandManager.registerCommand(new MoneyCommand().setExceptionHandler((command, registeredCommand, sender, args, t) -> {
            if(sender.isPlayer()){
                Player p = sender.getIssuer();
                p.spigot().sendMessage(getMessageBuilder().build(ConfigManager.errorGeneral));
            }
            return true;
        }));
        commandManager.registerCommand(new ShopCommand().setExceptionHandler((command, registeredCommand, sender, args, t) -> {
            if(sender.isPlayer()){
                Player p = sender.getIssuer();
                p.spigot().sendMessage(getMessageBuilder().build(ConfigManager.errorGeneral));
            }
            return true;
        }));
        createTraderCommandCompletionHandler();
    }

    @Override
    public void onDisable() {
        saveTraderManagerJSON();
        adminShopItemManager.saveConfig();
        getLogger().info("Disabled");
    }

    private void registerInterfaces(){
        ServicesManager sm = getServer().getServicesManager();
        sm.register(Economy.class, new VaultConnector(), this, ServicePriority.Highest);
        getLogger().info("Registered Vault Interface");
    }

    public void saveTraderManagerJSON(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(FileWriter writer = new FileWriter(this.getDataFolder() + "/traders.json")) {
            gson.toJson(traderManager, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createTraderCommandCompletionHandler(){
        commandManager.getCommandCompletions().registerCompletion("traderID", c -> {
            List<String> traderIDs = new ArrayList<>();
            for (Trader trader : traderManager.getTraders()){
                traderIDs.add(String.valueOf(trader.getTraderID()));
            }
            return ImmutableList.copyOf(traderIDs);
        });
    }

    public MessageBuilder getMessageBuilder(){
        return messageBuilder;
    }
}
