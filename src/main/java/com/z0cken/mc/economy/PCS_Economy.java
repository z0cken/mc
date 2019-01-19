package com.z0cken.mc.economy;

import co.aikar.commands.BukkitCommandManager;
import com.z0cken.mc.economy.commands.MoneyCommand;
import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.economy.events.PlayerListener;
import com.z0cken.mc.economy.impl.VaultConnector;
import com.z0cken.mc.economy.utils.MessageBuilder;
import net.milkbowl.vault.economy.*;
import net.minecraft.server.v1_13_R1.WorldGenVillagePieces;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public class PCS_Economy extends JavaPlugin {

    public static PCS_Economy pcs_economy;
    public AccountManager accountManager;
    private static BukkitCommandManager commandManager;
    private Connection conn;

    @Override
    public void onLoad(){
        pcs_economy = this;
        this.saveDefaultConfig();
        ConfigManager.loadConfig();

        connectToDB();
        checkDBConnection();

        accountManager = new AccountManager(conn);

        getLogger().info("Load Complete");
    }

    @Override
    public void onEnable(){
        registerCommands();
        registerInterfaces();

        this.getServer().getPluginManager().registerEvents(new PlayerListener(conn), this);

        getLogger().info("PCS_Economy Enabled");
    }

    private void registerCommands(){
        commandManager = new BukkitCommandManager(this);
        commandManager.registerCommand(new MoneyCommand().setExceptionHandler((command, registeredCommand, sender, args, t) -> {
            sender.sendMessage(MessageBuilder.buildMessage(ConfigManager.errorGeneral));
            return true;
        }));
    }

    @Override
    public void onDisable() {
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
        String sqlConnPath = "jdbc:mysql://" + ConfigManager.mysqlAddress + ":" +
                ConfigManager.mysqlPort + "/" +
                ConfigManager.mysqlDatabase + "?" +
                "user=" + ConfigManager.mysqlUsername +
                "&password=" + ConfigManager.mysqlPassword;
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
        noch Retrys hinzufügen.
     */
    public boolean checkDBConnection(){
        if(conn != null){
            try{
                if(!conn.isValid(100)){
                    connectToDB();
                    return true;
                }
                return false;
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
}
