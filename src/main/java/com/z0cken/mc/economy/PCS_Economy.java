package com.z0cken.mc.economy;

import co.aikar.commands.BukkitCommandManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.z0cken.mc.economy.commands.MoneyCommand;
import com.z0cken.mc.economy.commands.ShopCommand;
import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.economy.events.PlayerListener;
import com.z0cken.mc.economy.impl.VaultConnector;
import com.z0cken.mc.economy.shops.AdminShopItemManager;
import com.z0cken.mc.economy.shops.TraderManager;
import com.z0cken.mc.economy.utils.MessageBuilder;
import net.milkbowl.vault.economy.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public class PCS_Economy extends JavaPlugin {

    public static PCS_Economy pcs_economy;
    public AccountManager accountManager;
    public TraderManager traderManager;
    private static BukkitCommandManager commandManager;
    private Connection conn;
    public AdminShopItemManager adminShopItemManager;

    @Override
    public void onLoad(){
        pcs_economy = this;
        this.saveDefaultConfig();
        ConfigManager.loadConfig();

        this.adminShopItemManager = new AdminShopItemManager(this);
        this.adminShopItemManager.loadConfig();

        connectToDB();
        checkDBConnection();

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

        accountManager = new AccountManager(conn);

        getLogger().info("Load Complete");
    }

    @Override
    public void onEnable(){
        registerCommands();
        registerInterfaces();

        this.getServer().getPluginManager().registerEvents(new PlayerListener(conn), this);

        getLogger().info("Enabled");
    }

    private void registerCommands(){
        commandManager = new BukkitCommandManager(this);
        commandManager.registerCommand(new MoneyCommand().setExceptionHandler((command, registeredCommand, sender, args, t) -> {
            sender.sendMessage(MessageBuilder.buildMessage(ConfigManager.errorGeneral));
            return true;
        }));
        commandManager.registerCommand(new ShopCommand().setExceptionHandler((command, registeredCommand, sender, args, t) -> {
            sender.sendMessage(MessageBuilder.buildMessage(ConfigManager.errorGeneral));
            return true;
        }));
    }

    @Override
    public void onDisable() {
        saveTraderManagerJSON();

        try{
            conn.close();
            if(conn.isClosed()) { getLogger().info("SQL-connection closed"); }
        }catch (SQLException e){
            getLogger().log(Level.SEVERE, e.getMessage());
        }
        getLogger().info("PCS_Economy Disabled");
    }

    private void registerInterfaces(){
        ServicesManager sm = getServer().getServicesManager();
        sm.register(Economy.class, new VaultConnector(), this, ServicePriority.Highest);
        getLogger().info("Registered Vault Interface");
    }

    public Connection connectToDB(){
        String sqlConnPath = "jdbc:mysql://" + ConfigManager.mysqlAddress + ":"
                + ConfigManager.mysqlPort + "/"
                + ConfigManager.mysqlDatabase + "?"
                + "user=" + ConfigManager.mysqlUsername
                + "&password=" + ConfigManager.mysqlPassword;

        try{
            conn = DriverManager.getConnection(sqlConnPath);
            if(conn != null){
                getLogger().info("Database Connection Established");
            }
        }catch(SQLException ex){
            getLogger().log(Level.SEVERE, "Database Connection Failed");
            getLogger().log(Level.SEVERE,"SQLException: " + ex.getMessage());
            getLogger().log(Level.SEVERE, "SQLState: " + ex.getSQLState());
            getLogger().log(Level.SEVERE, "VendorError: " + ex.getErrorCode());
            conn = null;
        }
        return conn;
    }

    /*
        Hoffe, dass der Check ausgiebig genug ist. Auf Nachfrage kann ich u.U.
        noch Retries hinzufügen.
     */
    public boolean checkDBConnection(){
        if(conn != null){
            try{
                if(!conn.isValid(100)){
                    connectToDB();
                    return true;
                }
                return true;
            }catch (SQLException e){
                getLogger().log(Level.SEVERE, e.getMessage());
                return false;
            }
        }else{
            connectToDB();
            if(conn != null){
                try{
                    if(conn.isValid(100)){
                        return true;
                    }
                    return false;
                }catch (SQLException e){
                    getLogger().log(Level.SEVERE, e.getMessage());
                    return false;
                }
            }
            return false;
        }
    }

    public void saveTraderManagerJSON(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(FileWriter writer = new FileWriter(this.getDataFolder() + "/traders.json")) {
            gson.toJson(traderManager, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
