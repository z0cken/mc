package com.z0cken.mc.metro.listener;

import com.z0cken.mc.core.util.MessageBuilder;
import com.z0cken.mc.metro.PCS_Metro;
import com.z0cken.mc.metro.event.StationActivateEvent;
import com.z0cken.mc.metro.event.StationDeactivateEvent;
import com.z0cken.mc.progression.PCS_Progression;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MetroListener implements Listener {

    private static boolean instantiated;

    public MetroListener() {
        if(instantiated) throw new IllegalStateException(this.getClass().getName() + " cannot be instantiated twice!");
        instantiated = true;
    }

    @EventHandler
    public void onActivate(StationActivateEvent event) {
        //TODO Broadcast message & sound
        //TODO Give XP
        final MessageBuilder messageBuilder = PCS_Metro.getInstance().getMessageBuilder();
        event.getPlayers().forEach(p -> {
            final int xp = PCS_Metro.getInstance().getConfig().getInt("experience.per-activation");
            p.spigot().sendMessage(messageBuilder.define("AMOUNT", Integer.toString(xp)).define("REASON", "die Aktivierung").build(PCS_Metro.getInstance().getConfig().getString("messages.experience")));
            PCS_Progression.progress(p, "metro_xp", xp);
        });

        String s = event.getPlayers().size() > 1 ? event.getPlayers().size() + " Spielern" : event.getPlayers().get(0).getName();
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, event.getPlayers().stream().map(player -> new TextComponent(player.getName()+"\n")).toArray(BaseComponent[]::new));

        final BaseComponent[] msg = messageBuilder.define("STATION", event.getStation().getName()).define("PLAYERTEXT", s).define("PLAYERLIST", hoverEvent).build(PCS_Metro.getInstance().getConfig().getString("messages.activation"));
        Bukkit.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(msg));
    }

    @EventHandler
    public void onDeactivate(StationDeactivateEvent event) {
        //TODO Broadcast message & sound
    }

}
