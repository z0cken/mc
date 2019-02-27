package com.z0cken.mc.shout;

import org.bukkit.Material;

import java.util.HashMap;

public class ShoutGroup {
    private String name;
    private String permission;
    private HashMap<Integer, Shout> shouts;
    private Material material;
    private int id;

    public ShoutGroup(int id, String name, String permission, Material material){
        this.id = id;
        this.name = name;
        this.permission = permission;
        this.material = material;
        this.shouts = new HashMap<>();
    }

    public void addShout(int id, Shout shout){
        if(shout != null){
            shouts.put(id, shout);
        }
    }

    public HashMap<Integer, Shout> getShouts(){
        return this.shouts;
    }

    public String getName(){
        return this.name;
    }

    public boolean hasPermission(){
        if(this.permission == null || this.permission.isEmpty()){
            return false;
        }
        return true;
    }

    public String getPermission(){
        return this.permission;
    }

    public Material getMaterial(){
        return this.material;
    }

    public int getId(){
        return this.id;
    }
}
