package com.z0cken.mc.shout.config;

import com.z0cken.mc.shout.PCS_Shout;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    public static FileConfiguration config = null;

    public static String messagePrefix = null;
    public static String errorGeneral = null;
    public static String successGeneral = null;
    public static String successReload = null;

    public static String loreGroupBuyTitle = null;
    public static String loreGroupBuyPrice = null;
    public static String loreGroupBuyDescription = null;
    public static String loreShoutBuyTitle = null;
    public static String loreShoutBuyPrice = null;
    public static String loreShoutBuyDescription = null;

    public static void loadConfig(){
        config = PCS_Shout.getInstance().getConfig();

        messagePrefix = config.getString("shout.messagePrefix");
        errorGeneral = config.getString("shout.messages.errorGeneral");
        successGeneral = config.getString("shout.messages.successGeneral");
        successReload = config.getString("shout.messages.successReload");

        loreGroupBuyTitle = config.getString("shout.gui.lore.group.buyTitle");
        loreGroupBuyPrice = config.getString("shout.gui.lore.group.buyPrice");
        loreGroupBuyDescription = config.getString("shout.gui.lore.group.buyDescription");
        loreShoutBuyTitle = config.getString("shout.gui.lore.shout.buyTitle");
        loreShoutBuyPrice = config.getString("shout.gui.lore.shout.buyPrice");
        loreShoutBuyDescription = config.getString("shout.gui.lore.shout.buyDescription");
    }
}
