package com.z0cken.mc.core.bungee;

import com.z0cken.mc.core.Shadow;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.SQLException;

public class ShadowListener implements Listener {

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        try {
            Shadow.NAME.setString(event.getPlayer().getUniqueId(), event.getPlayer().getName());
            Shadow.IP.setString(event.getPlayer().getUniqueId(), event.getPlayer().getAddress().getAddress().getHostAddress());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
