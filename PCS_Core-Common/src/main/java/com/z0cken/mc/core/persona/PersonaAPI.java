package com.z0cken.mc.core.persona;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.z0cken.mc.core.CoreBridge;
import com.z0cken.mc.core.util.CoreTask;
import org.apache.http.client.HttpResponseException;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class PersonaAPI {

    private static boolean initialized = false;
    private static long cacheInterval;
    private static final Map<UUID, Persona> cache = new ConcurrentHashMap<>();


    public static void init(long cacheInterval, long updateInterval) {
        if(initialized) throw new IllegalStateException(PersonaAPI.class.getName() + " already initialized!");
        PersonaAPI.cacheInterval = Math.max(cacheInterval, 5);
        updateInterval = Math.max(updateInterval, 30);
        initialized = true;

        new CoreTask(true, TimeUnit.SECONDS, updateInterval, updateInterval) {
            @Override
            public void run() {
                Iterator<Map.Entry<UUID, Persona>> iterator = cache.entrySet().iterator();

                while(iterator.hasNext()) {
                    Map.Entry<UUID, Persona> entry = iterator.next();
                    if(!CoreBridge.getPlugin().isOnline(entry.getKey())) iterator.remove();
                    else try {
                        entry.getValue().fetchProfile();
                    } catch (HttpResponseException | UnirestException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.schedule();
    }

    public static Persona getPersona(UUID uuid) {
        Persona persona = cache.getOrDefault(uuid, null);
        if(persona != null) return persona;

        try {
            persona = new Persona(uuid);
        } catch (HttpResponseException | UnirestException | SQLException e) {
            e.printStackTrace();
        }

        return persona;
    }

    public static void cachePlayer(UUID uuid) {
        if(!initialized) throw new IllegalStateException(PersonaAPI.class.getName() + " not yet initialized!");

        new CoreTask(true, TimeUnit.SECONDS, 0L, cacheInterval) {
            @Override
            public void run() {
                if(CoreBridge.getPlugin().isOnline(uuid)) this.cancel();

                else {
                    Persona persona = getPersona(uuid);

                    if(persona != null) {
                        cache.put(uuid, persona);
                        this.cancel();
                    }
                }
            }
        }.schedule();
    }

}
