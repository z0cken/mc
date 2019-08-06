package com.z0cken.mc.end.egg;

import com.z0cken.mc.end.HiddenStringUtils;
import com.z0cken.mc.end.PCS_End;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public enum MagicEggType {
    MONSTER(MagicEggMonster.class), COMBAT(MagicEggCombat.class), MINING(MagicEggMining.class)/*, DIVER(MagicEggDiver.class)*/;

    private Class<? extends MagicEgg> clazz;

    private int range, duration, chargeTime;
    private Color color;
    private BarColor barColor;
    private ChatColor chatColor;

    private String title;
    private List<String> itemLore;

    MagicEggType(Class<? extends MagicEgg> clazz) {
        this.clazz = clazz;
        load();
    }

    private void load() {
        ConfigurationSection config = getConfig();

        range = config.getInt("range");
        duration = config.getInt("duration");
        chargeTime = config.getInt("charge-time");

        color = Color.fromRGB(config.getInt("rgb"));
        barColor = BarColor.valueOf(config.getString("bar-color").toUpperCase());
        chatColor = ChatColor.valueOf(config.getString("chat-color").toUpperCase());

        title = config.getString("title");

        itemLore = config.getStringList("lore");
        itemLore.add(0, HiddenStringUtils.encodeString(name()));
    }

    public ConfigurationSection getConfig() {
        return PCS_End.getInstance().getConfig().getConfigurationSection("eggs." + name().toLowerCase());
    }

    public MagicEgg spawn(Player owner, Block egg) {
        try {
            return clazz.getDeclaredConstructor(Player.class, Block.class).newInstance(owner, egg);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getRange() {
        return range;
    }

    public int getDuration() {
        return duration;
    }

    public int getChargeTime() {
        return chargeTime;
    }

    public static void reload() {
        for (MagicEggType value : values()) {
            value.load();
        }
    }

    public Color getColor() {
        return color;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public BarColor getBarColor() {
        return barColor;
    }

    public String getTitle() {
        return title;
    }

    public ItemStack getItemStack() {
        ItemStack is = new ItemStack(Material.DRAGON_EGG);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(title);
        meta.setLore(itemLore);
        is.setItemMeta(meta);

        return is;
    }
}