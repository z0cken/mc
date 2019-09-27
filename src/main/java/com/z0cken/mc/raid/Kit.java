package com.z0cken.mc.raid;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Kit implements ConfigurationSerializable {

    private final String name;
    private final ItemStack button;
    private final Map<Integer, ItemStack> items;
    private final Collection<PotionEffect> effects;

    Kit(Player player, String name) {
        this.name = name;
        effects = new HashSet<>(player.getActivePotionEffects());
        final ItemStack[] contents = player.getInventory().getContents();
        button = player.getInventory().getItemInOffHand();
        player.getInventory().setItemInOffHand(null);
        items = new HashMap<>();

        for(int i = 0; i < contents.length; i++) {
            ItemStack is = contents[i];
            if(is != null && i != 40) items.put(i, is);
        }
    }

    Kit(ConfigurationSection section) {
        name = null;
        button = section.getItemStack("button");

        items = new HashMap<>();
        final ConfigurationSection subItems = section.getConfigurationSection("items");
        if(subItems != null) subItems.getKeys(false).forEach(s -> items.put(Integer.parseInt(s), subItems.getSerializable(s, ItemStack.class)));

        effects = new HashSet<>();
        final ConfigurationSection subEffects = section.getConfigurationSection("effects");
        if(subEffects != null) subEffects.getKeys(false).forEach(s -> effects.add(subEffects.getSerializable(s, PotionEffect.class)));
    }

    public void apply(Player player, boolean clear) {
        PlayerInventory inventory = player.getInventory();
        Util.cleanPlayer(player, false);
        items.forEach((i, itemstack) -> {

            if(itemstack.getType() == Material.SHIELD) {
                ItemStack shield = new ItemStack(Material.SHIELD);
                ItemMeta shieldMeta = shield.getItemMeta();
                BlockStateMeta bmeta = (BlockStateMeta) shieldMeta;

                Banner banner = (Banner) bmeta.getBlockState();
                banner.setBaseColor(DyeColor.BROWN);
                banner.addPattern(new Pattern(DyeColor.BLACK, PatternType.MOJANG));
                banner.update();

                bmeta.setBlockState(banner);
                shield.setItemMeta(bmeta);
            }
            inventory.setItem(i, itemstack);
        });

        effects.forEach(player::addPotionEffect);
        if(name != null) player.sendMessage("Du spielst nun als " + name);
    }

    public ItemStack getButton() {
        return button;
    }

    public String getName() {
        return name;
    }

    public Collection<ItemStack> getItems() {
        return items.values();
    }

    public Kit(Map<String, Object> map) {
        items = new HashMap<>();
        effects = new HashSet<>();

        name = (String) map.getOrDefault("name", null);
        button = (ItemStack) map.getOrDefault("button", null);

        System.out.println(map.get("items"));
        Map<Integer, ItemStack> itemMap = (Map<Integer, ItemStack>) map.get("items");
        items.putAll(itemMap);

        System.out.println(map.get("effects"));
        Map<String, Object> effectMap = (Map<String, Object>) map.getOrDefault("effects", null);
        if(effectMap != null) effectMap.forEach((type, amp) -> {
            effects.add(new PotionEffect(PotionEffectType.getByName(type), Integer.MAX_VALUE, (Integer) amp));
        });
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("name", name);
        map.put("button", button);

        map.put("items", items);
        map.put("effects", effects);

        final HashMap<String, Integer> effectMap = new HashMap<>();
        map.put("effects", effectMap);
        effects.forEach(pe -> {
            effectMap.put(pe.getType().getName(), pe.getAmplifier());
        });

        return map;
    }
}
