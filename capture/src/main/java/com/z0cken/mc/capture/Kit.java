package com.z0cken.mc.capture;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Kit {

    private Map<Integer, ItemStack> content = new HashMap<>();

    Kit(ConfigurationSection section) {
        section.getKeys(false).forEach(s -> content.put(Integer.parseInt(s), section.getSerializable(s, ItemStack.class)));
    }

    public void apply(Player player) {
        PlayerInventory inventory = player.getInventory();
        content.forEach((i, itemstack) -> {

            if(itemstack.getType().name().contains("LEATHER") && itemstack.getType() != Material.LEATHER) {
                LeatherArmorMeta meta = (LeatherArmorMeta) itemstack.getItemMeta();
                meta.setColor(Arena.getTeam(player).getColor());
                itemstack.setItemMeta(meta);
            } else if(itemstack.getType() == Material.SHIELD) {
                ItemStack shield = new ItemStack(Material.SHIELD);
                ItemMeta shieldMeta = shield.getItemMeta();
                BlockStateMeta bmeta = (BlockStateMeta) shieldMeta;

                Banner bannerBlue = (Banner) bmeta.getBlockState();
                bannerBlue.setBaseColor(Arena.getTeam(player).getColor() == Color.RED ? DyeColor.RED : DyeColor.BLUE);
                bannerBlue.update();

                bmeta.setBlockState(bannerBlue);
                shield.setItemMeta(bmeta);
            }
            inventory.setItem(i, itemstack);
        });
    }

    public Collection<ItemStack> getContent() {
        return content.values();
    }

    public boolean contains(ItemStack itemStack) {
        return content.containsValue(itemStack);
    }

    public boolean contains(Material material) {
        return content.values().stream().map(ItemStack::getType).anyMatch(m -> m.equals(material));
    }
}
