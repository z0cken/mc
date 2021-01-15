package com.z0cken.mc.raid.json;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public class BukkitTypeAdapter implements JsonSerializer<ConfigurationSerializable>, JsonDeserializer<ConfigurationSerializable> {

    @Override
    public ConfigurationSerializable deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
        Map<String, Object> map = new Gson().fromJson(jsonelement, new TypeToken<Map<String, Object>>(){}.getType());
        System.out.println("Deserializing...");
        System.out.println(map);
        System.out.println("---------------------------");
        final ConfigurationSerializable result = ConfigurationSerialization.deserializeObject(map);
        System.out.println(result);
        if(result instanceof ItemStack) {
            System.out.println("Meta: " + ((ItemStack)result).getItemMeta().serialize());
        }
        return result;
    }

    @Override
    public JsonElement serialize(ConfigurationSerializable object, Type type, JsonSerializationContext jsonserializationcontext) {
        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        values.put("==", ConfigurationSerialization.getAlias(object.getClass()));
        values.putAll(object.serialize());
        serializeRecursively(values);

        //Need to reparse because nested map values are stringified
        return new JsonParser().parse(new Gson().toJson(values)).getAsJsonObject();
    }

    private static void serializeRecursively(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof ConfigurationSerializable) {
                System.out.println("Match: " + entry.getValue().getClass().getSimpleName());
                final Map<String, Object> child = ((ConfigurationSerializable) entry.getValue()).serialize();
                serializeRecursively(child);
                entry.setValue(child);
                System.out.println(new Gson().toJson(child));
            }
        }
    }
}
