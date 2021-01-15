package com.z0cken.mc.Revive.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerUtils {

    public static final ItemStack AIR = new ItemStack(Material.AIR);

    public static boolean isHolding(Player player, Material material) {
        return isHoldingInMain(player, material) || isHoldingInOff(player, material);
    }

    public static boolean isHoldingInMain(Player player, Material material) {
        return player.getInventory().getItemInMainHand() != null && player.getInventory().getItemInMainHand().getType() == material;
    }

    public static boolean isHoldingInOff(Player player, Material material) {
        return player.getInventory().getItemInOffHand() != null && player.getInventory().getItemInOffHand().getType() == material;
    }

    public static boolean removeFromHand(Player player, Material material, int amount) {
        if(isHoldingInMain(player, material)) {
            ItemStack itemStack = player.getInventory().getItemInMainHand();
            if(itemStack.getAmount() == 1) {
                itemStack = AIR;
            } else {
                itemStack.setAmount(itemStack.getAmount() - 1);
            }

            player.getInventory().setItemInMainHand(itemStack);
            return true;
        } else if(isHoldingInOff(player, material)) {
            ItemStack itemStack = player.getInventory().getItemInOffHand();
            if(itemStack.getAmount() == 1) {
                itemStack = AIR;
            } else {
                itemStack.setAmount(itemStack.getAmount() - 1);
            }

            player.getInventory().setItemInOffHand(itemStack);
            return true;
        }

        return false;
    }

    public static void clearPlayerInvAll(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); ++i) {
            player.getInventory().setItem(i, AIR);
        }

        player.getInventory().setItemInOffHand(AIR);
        player.getInventory().setHelmet(AIR);
        player.getInventory().setChestplate(AIR);
        player.getInventory().setLeggings(AIR);
        player.getInventory().setBoots(AIR);


        player.setItemOnCursor(AIR);

        //Crafting slots leeren
        player.getOpenInventory().getTopInventory().setItem(0, AIR);
        player.getOpenInventory().getTopInventory().setItem(1, AIR);
        player.getOpenInventory().getTopInventory().setItem(2, AIR);
        player.getOpenInventory().getTopInventory().setItem(3, AIR);
        player.getOpenInventory().getTopInventory().setItem(4, AIR);
    }

}
