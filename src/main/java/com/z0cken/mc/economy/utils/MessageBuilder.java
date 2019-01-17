package com.z0cken.mc.economy.utils;

import com.z0cken.mc.economy.config.ConfigManager;
import org.bukkit.ChatColor;

public class MessageBuilder {
    public static String buildMessage(String message, String player, String player2, double amount){
        String builtMessage = message;
        if(player != null && !player.isEmpty()){
            builtMessage = builtMessage.replace("{player}", player);
        }
        if(player2 != null && !player2.isEmpty()){
            builtMessage = builtMessage.replace("{player2}", player2);
        }
        builtMessage = builtMessage.replace("{amount}", String.valueOf(amount));
        builtMessage = builtMessage.replace("{csymbol}", ConfigManager.currencySymbol);
        return ChatColor.translateAlternateColorCodes('&',ConfigManager.messagePrefix + builtMessage);
    }

    public static String buildMessage(String message, String player, String player2){
        return buildMessage(message, player, player2, 0);
    }

    public static String buildMessage(String message, String player, double amount){
        return buildMessage(message, player, null, amount);
    }

    public static String buildMessage(String message, String player){
        return buildMessage(message, player, null, 0);
    }

    public static String buildMessage(String message){
        return buildMessage(message, null);
    }
}
