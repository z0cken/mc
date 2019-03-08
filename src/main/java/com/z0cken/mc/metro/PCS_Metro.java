package com.z0cken.mc.metro;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.z0cken.mc.metro.listener.MetroListener;
import com.z0cken.mc.metro.listener.MobListener;
import com.z0cken.mc.metro.listener.ProtectionListener;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;


public class PCS_Metro extends JavaPlugin {

    //public static final EnumFlag<Difficulty> DIFFICULTY_FLAG = new EnumFlag<>("difficulty", Difficulty.class);
    public static final StringFlag STRING_FLAG = new StringFlag("profile");
    private static PCS_Metro instance;
    private Metro metro;

    public static PCS_Metro getInstance() {
        return instance;
    }

    public PCS_Metro() {
        if(instance != null) throw new IllegalStateException(this.getClass().getName() + " cannot be instantiated twice!");
    }

    @Override
    public void onLoad() {
        instance = this;
        saveDefaultConfig();

        //WorldGuard.getInstance().getFlagRegistry().register(DIFFICULTY_FLAG);
        WorldGuard.getInstance().getFlagRegistry().register(STRING_FLAG);
    }

    @Override
    public void onEnable() {
        metro = Metro.getInstance();

        ItemStack is = new ItemStack(Material.LEATHER_HELMET, 2);
        LeatherArmorMeta meta = (LeatherArmorMeta) is.getItemMeta();
        meta.setColor(Color.NAVY);
        List<String> lore = new ArrayList<>();
        lore.add("Test-Lore");
        meta.setLore(lore);
        meta.setDisplayName("Display Name");
        meta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 20, true);
        is.setItemMeta(meta);
        getConfig().set("itemstack", is);

        ItemStack i2 = new ItemStack(Material.IRON_SWORD, 1);
        ItemMeta itemMeta = i2.getItemMeta();
        itemMeta.setUnbreakable(true);
        itemMeta.setDisplayName("name lol");
        ((Damageable)itemMeta).setDamage(50);
        i2.setItemMeta(itemMeta);
        getConfig().set("i2", i2);

        ItemStack i3 = new ItemStack(Material.DIRT, 20);
        i3.setItemMeta(i3.getItemMeta());
        getConfig().set("i3", i3);

        PotionEffect potionEffect = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20, 20);
        getConfig().set("effect", potionEffect);

        saveConfig();

        Bukkit.getPluginManager().registerEvents(new MetroListener(), this);
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(), this);
        Bukkit.getPluginManager().registerEvents(new MobListener(), this);
    }


    @Override
    public void onDisable() {
        instance = null;
    }

}
