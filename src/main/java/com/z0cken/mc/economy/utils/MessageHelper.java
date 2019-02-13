package com.z0cken.mc.economy.utils;

import net.md_5.bungee.api.chat.BaseComponent;

public class MessageHelper {
    public static String convertBcToString(BaseComponent[] bc){
        if(bc != null){
            BaseComponent[] bcs = bc;
            String message = "";
            for (BaseComponent c : bc){
                message += c.toLegacyText();
            }
            return message;
        }
        return null;
    }
}
