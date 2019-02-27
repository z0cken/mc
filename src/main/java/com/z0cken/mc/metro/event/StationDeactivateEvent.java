package com.z0cken.mc.metro.event;

import com.z0cken.mc.metro.Metro;
import com.z0cken.mc.metro.Station;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class StationDeactivateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Metro metro;
    private Station station;

    public StationDeactivateEvent(@Nonnull Metro metro, @Nonnull Station station) {
        this.metro = metro;
        this.station = station;
    }

    public Metro getMetro() {
        return metro;
    }

    public Station getStation() {
        return station;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
