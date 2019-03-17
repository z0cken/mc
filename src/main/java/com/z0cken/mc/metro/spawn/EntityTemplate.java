package com.z0cken.mc.metro.spawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;
import org.bukkit.potion.PotionEffect;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EntityTemplate implements Template {

    private EntityType type;
    private int xp;
    private Map<Attribute, Integer> attributes = new HashMap<>();
    private Map<EquipmentSlot, MetroEquipment> equipment = new HashMap<>();
    private Set<PotionEffect> effects = new HashSet<>();
    private LootTable lootTable;

    public EntityTemplate(@Nonnull YamlConfiguration configuration) {
        this.type = EntityType.valueOf(configuration.getString("type").toUpperCase());
        if(!type.isAlive()) throw new IllegalArgumentException(String.format("Type %s is not alive", type.name()));
        xp = configuration.getInt("xp");
        ConfigurationSection equipmentSection = configuration.getConfigurationSection("equipment");
        if(equipmentSection != null) {
            for(String s : equipmentSection.getKeys(false)) {
                EquipmentSlot slot = EquipmentSlot.valueOf(s.toUpperCase());
                ConfigurationSection sec = equipmentSection.getConfigurationSection(s);
                equipment.put(slot, new MetroEquipment(sec.getSerializable("itemstack", ItemStack.class), sec.getDouble("probability"), (float) sec.getDouble("drop-chance")));
            }
        }

        ConfigurationSection attributeSection = configuration.getConfigurationSection("attributes");
        if(attributeSection != null) attributeSection.getValues(false).forEach((string, object) -> attributes.put(Attribute.valueOf(string.toUpperCase()), (Integer)object));

        ConfigurationSection effectSection = configuration.getConfigurationSection("effects");
        if(effectSection != null) effectSection.getKeys(false).forEach(s -> effects.add(effectSection.getSerializable(s, PotionEffect.class)));

        final String s = configuration.getString("loot-table");
        if(s != null) lootTable = Bukkit.getLootTable(new NamespacedKey("metro", s));
    }

    @Override
    public void spawn(Location location) {
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, type);
        entity.getScoreboardTags().add("metro");

        EntityEquipment entityEquipment = entity.getEquipment();

        equipment.forEach((slot, metroEquipment) -> {
            if(Math.random() > metroEquipment.getProbability()) return;

            final ItemStack itemStack = metroEquipment.getItemStack();
            final float dropChance = metroEquipment.getDropChance();

            switch (slot) {
                case HEAD:
                    entityEquipment.setHelmet(itemStack);
                    entityEquipment.setHelmetDropChance(dropChance);
                    break;
                case CHEST:
                    entityEquipment.setChestplate(itemStack);
                    entityEquipment.setChestplateDropChance(dropChance);
                    break;
                case LEGS:
                    entityEquipment.setLeggings(itemStack);
                    entityEquipment.setLeggingsDropChance(dropChance);
                    break;
                case FEET:
                    entityEquipment.setBoots(itemStack);
                    entityEquipment.setBootsDropChance(dropChance);
                    break;
                case HAND:
                    entityEquipment.setItemInMainHand(itemStack);
                    entityEquipment.setItemInMainHandDropChance(dropChance);
                    break;
                case OFF_HAND:
                    entityEquipment.setItemInOffHand(itemStack);
                    entityEquipment.setItemInOffHandDropChance(dropChance);
            }
        });

        effects.forEach(entity::addPotionEffect);

        attributes.forEach((attribute, integer) -> entity.getAttribute(attribute).setBaseValue(integer));

        if(xp > 0) entity.getScoreboardTags().add("metro-xp:" + xp);
        entity.setCanPickupItems(false);

        if(lootTable != null && entity instanceof Lootable) {
           ((Lootable)entity).setLootTable(lootTable);
        }
    }

    private static class MetroEquipment {

        private ItemStack itemStack;
        private double probability;
        private float dropChance;

        private MetroEquipment(ItemStack itemStack, double probability, float dropChance) {
            this.itemStack = itemStack;
            this.probability = probability > 0 ? probability : 1;
            this.dropChance = dropChance;
        }

        private ItemStack getItemStack() {
            return itemStack;
        }

        private double getProbability() {
            return probability;
        }

        private float getDropChance() {
            return dropChance;
        }
    }

}
