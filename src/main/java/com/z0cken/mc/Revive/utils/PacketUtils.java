package com.z0cken.mc.Revive.utils;

import net.minecraft.server.v1_13_R2.DataWatcher;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.PacketPlayOutEntityMetadata;
import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Map;

public class PacketUtils {

    public static void setGlowing(Player viewer, Player target, boolean glowing) {
        /*WrappedDataWatcher watcher = new WrappedDataWatcher(target);
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
        }*/

        try {
            EntityPlayer entityPlayer = ((CraftPlayer) target).getHandle();

            DataWatcher dataWatcher = entityPlayer.getDataWatcher();

            entityPlayer.glowing = glowing; // For the update method in EntityPlayer to prevent switching back.

            // The map that stores the DataWatcherItems is private within the DataWatcher Object.
            // We need to use Reflection to access it from Apache Commons and change it.
            Map<Integer, DataWatcher.Item<?>> map = (Map<Integer, DataWatcher.Item<?>>) FieldUtils.readDeclaredField(dataWatcher, "d", true);

            // Get the 0th index for the BitMask value. http://wiki.vg/Entities#Entity
            DataWatcher.Item item = map.get(0);

            byte initialBitMask = (Byte) item.b(); // Gets the initial bitmask/byte value so we don't overwrite anything.
            byte bitMaskIndex = (byte) 0x40; // The index as specified in wiki.vg/Entities
            if (glowing) {
                item.a((byte) (initialBitMask|bitMaskIndex));
            } else {
                item.a((byte) (initialBitMask & ~(bitMaskIndex))); // Inverts the specified bit from the index.
            }

            PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(target.getEntityId(), dataWatcher, true);

            ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(metadataPacket);
        } catch (IllegalAccessException e) { // Catch statement necessary for FieldUtils.readDeclaredField()
            e.printStackTrace();
        }
    }
}
