package com.z0cken.mc.metro.event;

import com.z0cken.mc.metro.Metro;
import com.z0cken.mc.metro.Station;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class StationActivateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private Metro metro;
    private Station station;
    private List<Player> players;

    public StationActivateEvent(@Nonnull Metro metro, @Nonnull Station station, @Nullable List<Player> players) {
        this.metro = metro;
        this.station = station;
        this.players = players == null ? new ArrayList<>() : players;
        this.players = Collections.unmodifiableList(this.players);
    }

    public Metro getMetro() {
        return metro;
    }

    public Station getStation() {
        return station;
    }

    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
