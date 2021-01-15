package com.z0cken.mc.metro.spawn;

import com.z0cken.mc.metro.Metro;
import com.z0cken.mc.metro.PCS_Metro;
import com.z0cken.mc.metro.util.RandomSupplier;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SpawnProfile {

    private Map<EntityType, RandomSupplier<Template>> templateSuppliers = new HashMap<>();
    private Map<EntityType, RandomSupplier<EntityType>> substitutes = new HashMap<>();

    public SpawnProfile(@Nonnull Metro metro, @Nonnull YamlConfiguration configuration) {

        ConfigurationSection section;

        section = configuration.getConfigurationSection("mobs");
        if(section != null) {
            for(String s : section.getKeys(false)) {
                EntityType type = EntityType.valueOf(s.toUpperCase());
                if (!type.isAlive()) throw new IllegalArgumentException();

                templateSuppliers.put(type, new RandomSupplier<>(
                    section.getConfigurationSection(s).getValues(false).entrySet()
                        .stream().collect(Collectors.toMap(entry -> {
                            Template template = metro.getEntityTemplate(entry.getKey());
                            if(template == null) template = metro.getGroupTemplate(entry.getKey());
                            if(template == null) PCS_Metro.getInstance().getLogger().warning(String.format("Template missing: %s", entry.getKey()));
                            return template;
                    }, entry -> ((Integer)entry.getValue()).doubleValue())
                )));
            }
        }

        section = configuration.getConfigurationSection("substitutes");
        if(section != null) {
            for(String s : section.getKeys(false)) {
                EntityType type = EntityType.valueOf(s.toUpperCase());
                if(!type.isAlive()) throw new IllegalArgumentException();

                substitutes.put(type, new RandomSupplier<>(
                    section.getConfigurationSection(s).getValues(false).entrySet()
                        .stream().collect(Collectors.toMap(entry -> EntityType.valueOf(entry.getKey().toUpperCase()), entry -> ((Integer)entry.getValue()).doubleValue()))
                ));
            }
        }
    }

    public boolean handleSpawn(EntityType type, Location location) {
        if(!type.isAlive()) return false;

        RandomSupplier<Template> supplier = templateSuppliers.getOrDefault(type, null);
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

        Template template = supplier.get();
        template.spawn(location);
        return true;
    }
}
