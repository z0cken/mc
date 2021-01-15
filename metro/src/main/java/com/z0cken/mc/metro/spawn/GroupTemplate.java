package com.z0cken.mc.metro.spawn;

import com.z0cken.mc.metro.PCS_Metro;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class GroupTemplate implements Template {

    private Map<EntityTemplate, Integer> map = new HashMap<>();

    public GroupTemplate(@Nonnull YamlConfiguration configuration, @Nonnull Map<String, EntityTemplate> templateMap) {
        configuration.getValues(false).forEach(((id, i) -> {
            final EntityTemplate template = templateMap.getOrDefault(id, null);
            if(template == null) PCS_Metro.getInstance().getLogger().warning(String.format("Template missing: %s", id));
            else map.put(template, (Integer) i);
        }));
    }

    @Override
    public void spawn(Location location) {
        map.forEach((template, integer) -> {
            for(int i = 0; i < integer; i++) {
                template.spawn(location);
            }
        });
    }
}
