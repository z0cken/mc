package com.z0cken.mc.shout.config;

import com.z0cken.mc.shout.PCS_Shout;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    public static FileConfiguration config = null;

    public static String messagePrefix = null;
    public static String errorGeneral = null;
    public static String successGeneral = null;

    public static void loadConfig(){
        config = PCS_Shout.getInstance().getConfig();

        messagePrefix = config.getString("shout.messagePrefix");
        errorGeneral = config.getString("shout.messages.errorGeneral");
        successGeneral = config.getString("shout.messages.successGeneral");
    }
}
