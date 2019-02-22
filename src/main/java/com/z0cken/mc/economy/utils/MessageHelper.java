package com.z0cken.mc.economy.utils;

import net.md_5.bungee.api.chat.BaseComponent;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

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

    public static double roundToTwoDecimals(double value){
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.ENGLISH);
        DecimalFormat format = (DecimalFormat)nf;
        format.applyPattern("###.##");
        return Double.valueOf(format.format(value));
    }
}
