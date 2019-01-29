package com.z0cken.mc.Revive.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public class PacketUtils {

    public static void setGlowing(Player viewer, LivingEntity target, boolean glowing) {
        WrappedDataWatcher watcher = new WrappedDataWatcher(target);
        PacketContainer wrapper = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);

        byte flag = WrappedDataWatcher.getEntityWatcher(target).getByte(0);
        if(glowing) flag |= 0x40;
        else flag &= ~0x40;

        watcher.setObject(0, WrappedDataWatcher.Registry.get(Byte.class), flag);
        watcher.setEntity(target);
        wrapper.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, wrapper);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
