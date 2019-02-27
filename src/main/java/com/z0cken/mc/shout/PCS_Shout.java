package com.z0cken.mc.shout;

import co.aikar.commands.BukkitCommandManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.z0cken.mc.core.bukkit.PCS_Core;
import com.z0cken.mc.core.util.MessageBuilder;
import com.z0cken.mc.shout.commands.ShoutCommand;
import com.z0cken.mc.shout.config.ConfigManager;
import com.z0cken.mc.shout.config.ShoutManager;
import com.z0cken.mc.shout.gui.ShoutFavoriteManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public class PCS_Shout extends JavaPlugin {
    private static PCS_Shout shout;
    private MessageBuilder messageBuilder;
    private BukkitCommandManager commandManager;
    private Permission vaultPermission;
    private Economy vaultEconomy;
    private ShoutFavoriteManager favoriteManager;

    @Override
    public void onLoad(){
        shout = this;
        this.saveDefaultConfig();
        ConfigManager.loadConfig();
        ShoutManager.load();
        loadFavorites();
        messageBuilder = new MessageBuilder().define("PREFIX", ConfigManager.messagePrefix);
        getLogger().info("Load Complete");
    }

    @Override
    public void onEnable(){
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
        if(permissionProvider != null){
            vaultPermission = permissionProvider.getProvider();
        }
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if(economyProvider != null){
            vaultEconomy = economyProvider.getProvider();
        }
        registerCommands();
        getLogger().info("Enabled");
    }

    @Override
    public void onDisable(){
        saveFavorites();
        getLogger().info("Disabled");
    }

    private void registerCommands(){
        commandManager = new BukkitCommandManager(this);
        commandManager.registerCommand(new ShoutCommand().setExceptionHandler((command, registeredCommand, sender, args, t) -> {
           if(sender instanceof Player){

           }
           return true;
        }));
    }

    private void saveFavorites(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(FileWriter writer = new FileWriter(this.getDataFolder() + "/favorites.json")){
            gson.toJson(favoriteManager, writer);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void loadFavorites(){
        Gson gson = new Gson();
        File favoritesJson = new File(this.getDataFolder() + "/favorites.json");
        if(favoritesJson.exists() && !favoritesJson.isDirectory()){
            try(FileReader reader = new FileReader(favoritesJson)){
                favoriteManager = gson.fromJson(reader, ShoutFavoriteManager.class);
            }catch (IOException e){
                e.printStackTrace();
            }
        }else{
            favoriteManager = new ShoutFavoriteManager();
        }
    }

    public ShoutFavoriteManager getFavoriteManager(){
        return this.favoriteManager;
    }

    public static PCS_Shout getInstance(){
        return shout;
    }

    public MessageBuilder getMessageBuilder(){
        return messageBuilder;
    }

    public Permission getPermissions(){
        return vaultPermission;
    }

    public Economy getEconomy(){
        return vaultEconomy;
    }
}
