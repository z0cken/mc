package com.z0cken.mc.core.persona;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.z0cken.mc.core.CoreBridge;
import com.z0cken.mc.core.Database;
import com.z0cken.mc.core.util.CoreTask;
import com.z0cken.mc.core.util.MessageBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
    private static final Map<UUID, Persona> cache = new ConcurrentHashMap<>();


    public static void init(long updateInterval) {
        if(initialized) throw new IllegalStateException(PersonaAPI.class.getName() + " already initialized!");
        updateInterval = Math.max(updateInterval, 30);
        initialized = true;

        new CoreTask(true, TimeUnit.SECONDS, updateInterval, updateInterval) {
            @Override
            public void run() {
                Iterator<Map.Entry<UUID, Persona>> iterator = cache.entrySet().iterator();

                while(iterator.hasNext()) {
                    Map.Entry<UUID, Persona> entry = iterator.next();
                    if(!CoreBridge.getPlugin().isOnline(entry.getKey())) iterator.remove();
                    else if(!entry.getValue().isGuest()) try {
                        entry.getValue().fetchProfile();
                    } catch (HttpResponseException | UnirestException e) {
                        CoreBridge.getPlugin().getLogger().severe(e.getMessage());
                    }
                }
            }
        }.schedule();

        new CoreTask(true, TimeUnit.SECONDS, 30L, 60L) {
            @Override
            public void run() {
                for(UUID uuid : CoreBridge.getPlugin().getOnlinePlayers()) {
                    if(!cache.containsKey(uuid)) {
                        try {
                            Persona persona = new Persona(uuid);
                            if(persona.getName() != null || persona.isGuest()) {
                                cache.put(uuid, persona);
                            }
                        } catch (SQLException | HttpResponseException | UnirestException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.schedule();
    }

    public static Persona getPersona(UUID uuid) throws SQLException, UnirestException, HttpResponseException {
        Persona persona = cache.getOrDefault(uuid, null);
        if(persona != null) return persona;

        /*
        persona = new Persona(uuid);
        if(persona.getName() != null || persona.isGuest()) {
            if(CoreBridge.getPlugin().isOnline(uuid)) cache.put(uuid, persona);
            return persona;
        }*/

        return null;
    }

    public static void updateCachedPersona(UUID uuid) {
        if(cache.containsKey(uuid)) {
            try {
                final Persona value = new Persona(uuid);
                cache.put(uuid, value);
            } catch (HttpResponseException | UnirestException e) {
                CoreBridge.getPlugin().getLogger().severe(e.getMessage());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static MessageBuilder getPlayerBuilder(UUID uuid, String name) {
        MessageBuilder builder = MessageBuilder.DEFAULT.define("PLAYER", name);

        Persona persona;
        try {
            persona = PersonaAPI.getPersona(uuid);
            if(persona != null) {
                builder = builder.define("PERSONA", persona.getHoverEvent());
                if(!persona.isGuest()) builder = builder.define("MARK", " " + persona.getMark().getSymbol());
            }
            else if(DatabaseHelper.getUsername(uuid) == null)builder = builder.define("PERSONA", new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("§cNutzer nicht verifiziert")}));
            else builder = builder.define("PERSONA", new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("§cDaten nicht verfügbar")}));

        } catch (SQLException | UnirestException | HttpResponseException e) {
            CoreBridge.getPlugin().getLogger().severe(e.getMessage());
            builder = builder.define("PERSONA", new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("§cDaten nicht verfügbar")}));
        }

        return builder;
    }
}
