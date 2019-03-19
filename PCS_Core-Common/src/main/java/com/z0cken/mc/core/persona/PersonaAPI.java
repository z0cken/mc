package com.z0cken.mc.core.persona;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.mashape.unirest.http.Unirest;
import com.z0cken.mc.core.CoreBridge;
import com.z0cken.mc.core.util.ConfigurationBridge;
import com.z0cken.mc.core.util.ConfigurationType;

import java.util.UUID;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class PersonaAPI {

    private static LoadingCache<UUID, Persona> cache;

    static {
        init();
    }

    private static void init() {
        final ConfigurationBridge configBridge = CoreBridge.getPlugin().getConfigBridge(ConfigurationType.CORE);

        LoadingCache<UUID, Persona> old = cache;
        cache = Caffeine.newBuilder().build(Persona::new);
        if(old != null) {
            cache.putAll(old.asMap());
            old.invalidateAll();
        }

        Unirest.setTimeouts(configBridge.getInt("persona.connection-timeout"), configBridge.getInt("persona.socket-timeout"));
    }

    public static Persona getPersona(UUID uuid) {
        return cache.get(uuid);
    }

    public static void invalidate(UUID uuid) {
        cache.invalidate(uuid);
    }
}
