package com.z0cken.mc.Revive.utils;

import net.citizensnpcs.api.npc.NPC;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mauri on 18.07.2017.
 */
public class NMSBridge {

    public final String nmsbase;

    private Map<String, Class> classcache = new HashMap<>();

    private final Class packetClass;

    public NMSBridge(String nmsversion) throws Exception {
        this.nmsbase = nmsversion;

        this.packetClass = getNMSClass("Packet");
    }

    public Class getNMSClass(String name) throws ClassNotFoundException {
        name = "net.minecraft.server." + nmsbase + "." + name;

        Class c = classcache.get(name);

        //using ClassNotFoundException as placeholder for missing classes
        if (c == ClassNotFoundException.class) {
            throw new ClassNotFoundException(name);
        }

        if (c != null) {
            return c;
        }

        try {
            c = getClass().getClassLoader().loadClass(name);

            classcache.put(name, c);

            return c;
        } catch (ClassNotFoundException ex) {
            c = ex.getClass();
            classcache.put(name, c);

            throw ex;
        }
    }

    /**
     * sends a bed packet
     *
     * @param player the player
     * @param npc    the spacken
     * @throws Exception if something goes wrong
     */
    public void sendHumanNPCBed(Player player, NPC npc) throws Exception {
        Object pac = getNMSClass("PacketPlayOutBed").newInstance();

        setFieldValues(pac, new Object[][]{
                {"a", npc.getEntity().getEntityId()},//entity id
                {"b", new BlockPosition(npc.getStoredLocation().getBlockX(), 1, npc.getStoredLocation().getBlockZ())}
        });

        sendPacket(player, pac);
    }

    public void sendBedBlockChange(Player player, NPC npc, boolean destroy) {
        if (!destroy) {
            player.sendBlockChange(getBedLocation(npc.getStoredLocation()), Material.RED_BED, (byte) 0);
        } else {
            player.sendBlockChange(getBedLocation(npc.getStoredLocation()), getBedLocation(npc.getStoredLocation()).getBlock().getType(), (byte) 0);
        }
    }

    public void sendBedRelMove(Player player, NPC npc) throws Exception {
        sendPacket(player, new PacketPlayOutEntity.PacketPlayOutRelEntityMove(npc.getEntity().getEntityId(), (byte) 0, (byte) (-60.8), (byte) 0, false));
    }

    private Location getBedLocation(Location location) {
        location = location.clone();
        location.setY(1);
        return location;
    }

    /**
     * sends a packet to a player
     *
     * @param p   the player
     * @param pac the packet
     * @throws Exception if something goes wrong
     */
    public void sendPacket(Player p, Object pac) throws Exception {
        Object c = ((CraftPlayer) p).getHandle().playerConnection;

        rumParametizedMethode(c, "sendPacket", packetClass, pac);
    }

    /**
     * sets multiple field values
     *
     * @param o    the object to modify
     * @param data the values to set
     * @throws Exception if something goes wrong
     */
    public void setFieldValues(Object o, Object[][] data) throws Exception {
        for (int i = 0; i < data.length; i++) {
            setFieldValue(o, ((String) data[i][0]), data[i][1]);
        }
    }

    /**
     * sets one field value
     *
     * @param o     the object to modify
     * @param name  the field name
     * @param value the new value
     * @throws Exception if something goes wrong
     */
    public void setFieldValue(Object o, String name, Object value) throws Exception {
        Field f = o.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(o, value);
    }

    public void rumParametizedMethode(Object o, String name, Class type, Object argument) throws Exception {
        Method m = o.getClass().getMethod(name, type);

        m.invoke(o, argument);
    }
}
