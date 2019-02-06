package com.z0cken.mc.economy.utils;

import com.z0cken.mc.economy.config.ConfigManager;
import org.bukkit.ChatColor;

public class MessageBuilder {
    public static String buildMessage(boolean prefix, String message, String player, String player2, double amount, int quantity){
        String builtMessage = message;
        if(player != null && !player.isEmpty()){
            builtMessage = builtMessage.replace("{player}", player);
        }
        if(player2 != null && !player2.isEmpty()){
            builtMessage = builtMessage.replace("{player2}", player2);
        }
        builtMessage = builtMessage.replace("{amount}", String.format("%.0f", amount));
        builtMessage = builtMessage.replace("{csymbol}", ConfigManager.currencySymbol);
        builtMessage = builtMessage.replace("{quantity}", String.valueOf(quantity));
        if(prefix){
            return ChatColor.translateAlternateColorCodes('§',ConfigManager.messagePrefix + builtMessage);
        }else{
            return ChatColor.translateAlternateColorCodes('§', builtMessage);
        }
    }

    public static String buildMessage(boolean prefix, String message, String player, String player2){
        return buildMessage(prefix, message, player, player2, 0, 0);
    }

    public static String buildMessage(boolean prefix, String message, String player, double amount){
        return buildMessage(prefix, message, player, null, amount, 0);
    }

    public static String buildMessage(boolean prefix, String message, String player){
        return buildMessage(prefix, message, player, null, 0, 0);
    }

    public static String buildMessage(boolean prefix, String message){
        return buildMessage(prefix, message, null);
    }

    public static String buildMessage(boolean prefix, String message, int quantity){
        return buildMessage(prefix, message, null, null, 0, quantity);
    }

    public static String buildMessage(boolean prefix, String message, int amount, int quantity){
        return buildMessage(prefix, message, null, null, amount, quantity);
    }
}
