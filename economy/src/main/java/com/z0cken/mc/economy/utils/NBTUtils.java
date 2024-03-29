package com.z0cken.mc.economy.utils;

import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class NBTUtils {
    public static String getStringValue(ItemStack stack, String nbtID){
        net.minecraft.server.v1_16_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
        NBTTagCompound compound = getNBTTagCompound(nmsStack);
        return compound.getString(nbtID);
    }

    public static NBTTagCompound getNBTTagCompound(net.minecraft.server.v1_16_R3.ItemStack stack){
        return (stack.hasTag()) ? stack.getTag() : new NBTTagCompound();
    }
}
