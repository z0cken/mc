package com.z0cken.mc.metro;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.z0cken.mc.core.util.MessageBuilder;
import com.z0cken.mc.metro.listener.EffectListener;
import com.z0cken.mc.metro.listener.MetroListener;
import com.z0cken.mc.metro.listener.MobListener;
import com.z0cken.mc.metro.listener.ProtectionListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


public class PCS_Metro extends JavaPlugin {

    //public static final EnumFlag<Difficulty> DIFFICULTY_FLAG = new EnumFlag<>("difficulty", Difficulty.class);
    private final MessageBuilder messageBuilder;
    public static final StringFlag STRING_FLAG = new StringFlag("profile");
    private static PCS_Metro instance;
    private Metro metro;

    public static PCS_Metro getInstance() {
        return instance;
    }

    public PCS_Metro() {
        if(instance != null) throw new IllegalStateException(this.getClass().getName() + " cannot be instantiated twice!");
        messageBuilder = MessageBuilder.DEFAULT.define("PREFIX", getConfig().getString("messages.prefix"));
    }

    @Override
    public void onLoad() {
        instance = this;
        saveDefaultConfig();

        //WorldGuard.getInstance().getFlagRegistry().register(DIFFICULTY_FLAG);
        WorldGuard.getInstance().getFlagRegistry().register(STRING_FLAG);
    }

    @Override
    public void onEnable() {
        metro = Metro.getInstance();
        saveConfig();

        Bukkit.getPluginManager().registerEvents(new MetroListener(), this);
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(), this);
        Bukkit.getPluginManager().registerEvents(new MobListener(), this);
        Bukkit.getPluginManager().registerEvents(new EffectListener(), this);
        Bukkit.getPluginCommand("metro").setExecutor(new MetroCommand());
    }


    @Override
    public void onDisable() {
        instance = null;
        if(metro != null) metro.getAppropriateEffect().deactivate();
    }

    public MessageBuilder getMessageBuilder() {
        return messageBuilder;
    }
}
