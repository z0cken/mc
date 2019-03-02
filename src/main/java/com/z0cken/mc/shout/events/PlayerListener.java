package com.z0cken.mc.shout.events;

import com.z0cken.mc.shout.PCS_Shout;
import com.z0cken.mc.shout.gui.ShoutFavoriteManager;
import com.z0cken.mc.shout.gui.ShoutFavorites;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        ShoutFavoriteManager manager = PCS_Shout.getInstance().getFavoriteManager();
        if(manager.getFavorites(p.getUniqueId().toString()) == null){
            ShoutFavorites favorites = new ShoutFavorites();
            manager.addFavorites(p.getUniqueId().toString(), favorites);
        }
    }
}
