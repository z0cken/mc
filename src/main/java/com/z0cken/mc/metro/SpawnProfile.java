package com.z0cken.mc.metro;

import com.z0cken.mc.metro.util.RandomSupplier;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SpawnProfile {

    private Map<EntityType, RandomSupplier<EntityTemplate>> templateSuppliers = new HashMap<>();
    private Map<EntityType, RandomSupplier<EntityType>> substitutes = new HashMap<>();

    public SpawnProfile(Metro metro, YamlConfiguration configuration) {

        ConfigurationSection section;

        section = configuration.getConfigurationSection("templates");
        if(section != null) {
            for(String s : section.getKeys(false)) {
                EntityType type = EntityType.valueOf(s.toUpperCase());
                if (!type.isAlive()) throw new IllegalArgumentException();

                templateSuppliers.put(type, new RandomSupplier<>(
                        section.getConfigurationSection(s).getValues(false).entrySet()
                                .stream().collect(Collectors.toMap(entry -> metro.getTemplate(type, entry.getKey()), entry -> ((Integer)entry.getValue()).doubleValue()))
                ));
            }
        }

        section = configuration.getConfigurationSection("substitutes");
        if(section != null) {
            for(String s : section.getKeys(false)) {
                EntityType type = EntityType.valueOf(s.toUpperCase());
                if(!type.isAlive()) throw new IllegalArgumentException();

                substitutes.put(type, new RandomSupplier<>(
                        configuration.getConfigurationSection(s).getValues(false).entrySet()
                                .stream().collect(Collectors.toMap(entry -> EntityType.valueOf(entry.getKey().toUpperCase()), entry -> ((Integer)entry.getValue()).doubleValue()))
                ));
            }
        }
    }

    public boolean spawnMob(EntityType type, Location location) {
        if(!type.isAlive()) throw new IllegalArgumentException();

        RandomSupplier<EntityTemplate> supplier = templateSuppliers.getOrDefault(type, null);
        if(supplier == null && !substitutes.isEmpty()) {
            RandomSupplier<EntityType> substituteSupplier = substitutes.getOrDefault(type, null);
            if(substituteSupplier == null) return false;
            else {
                type = substituteSupplier.get();
                if(type == null) return false;
                supplier = templateSuppliers.getOrDefault(type, null);
            }
        }

        if(supplier == null) return false;

        EntityTemplate profile = supplier.get();
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, type);
        profile.apply(entity);
        return true;
    }
}
