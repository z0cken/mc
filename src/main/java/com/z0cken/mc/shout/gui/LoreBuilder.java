package com.z0cken.mc.shout.gui;

import com.z0cken.mc.shout.PCS_Shout;
import com.z0cken.mc.shout.util.MessageHelper;

import java.util.ArrayList;
import java.util.List;

public class LoreBuilder {

    public LoreBuilder(){

    }

    public static List<String> build(String sPrice, double price, String description){
        String builtPrice = MessageHelper.convertBcToString(PCS_Shout.getInstance().getMessageBuilder()
            .define("AMOUNT", String.valueOf(MessageHelper.roundToTwoDecimals(price))).build(sPrice));
        String builtDescription = MessageHelper.convertBcToString(PCS_Shout.getInstance().getMessageBuilder().build(description));
        ArrayList<String> list = new ArrayList<>();
        list.add(builtPrice);
        list.add(builtDescription);
        return list;
    }
}
