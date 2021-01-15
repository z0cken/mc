package com.z0cken.mc.core.bungee;

import com.z0cken.mc.core.Shadow;
import com.z0cken.mc.core.persona.Persona;
import com.z0cken.mc.core.persona.PersonaAPI;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ShadowListener implements Listener {

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {

        Shadow.NAME.setString(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        Shadow.IP.setString(event.getPlayer().getUniqueId(), event.getPlayer().getAddress().getAddress().getHostAddress());

        final Persona persona = PersonaAPI.getPersona(event.getPlayer().getUniqueId());
        if(persona.isVerified()) persona.getBoardProfile().thenAcceptAsync(profile -> {
            if(profile != null && !persona.isGuest()) Shadow.MARK.setInt(event.getPlayer().getUniqueId(), profile.getMark().ordinal());
        });


    }

}
