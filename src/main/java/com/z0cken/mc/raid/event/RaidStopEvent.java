package com.z0cken.mc.raid.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RaidStopEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final boolean manual;

    public RaidStopEvent(boolean manual) {
        this.manual = manual;
    }

    public boolean isManual() {
        return manual;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
