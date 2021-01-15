package com.z0cken.mc.raid.event;

import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

public class EndermanCaptureEvent extends EntityEvent {

    private static final HandlerList handlers = new HandlerList();

    public EndermanCaptureEvent(Entity what) {
        super(what);
    }

    @Override
    public Enderman getEntity() {
        return (Enderman) entity;
    }
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
