package com.z0cken.mc.shout;

import org.bukkit.Material;

public class Shout {
    private String name;
    private String path;
    private String permission;
    private Material mat;
    private double price;
    private float volume;
    private float pitch;
    private int id;

    public Shout(int id, String name, String path, String permission, Material mat, double price, float volume, float pitch){
        this.id = id;
        this.name = name;
        this.path = path;
        this.permission = permission;
        this.mat = mat;
        this.price = price;
        this.volume = volume;
        this.pitch = pitch;
    }

    public String getName(){
        return this.name;
    }

    public String getPath(){
        return this.path;
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
        return this.mat;
    }

    public double getPrice(){
        return this.price;
    }

    public float getVolume(){
        return this.volume;
    }

    public float getPitch(){
        return this.pitch;
    }

    public int getId(){
        return this.id;
    }
}
