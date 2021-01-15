package com.z0cken.mc.shout;

import com.z0cken.mc.shout.config.ShoutManager;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class Shout {
    private String name;
    private String path;
    private String permission;
    private Material mat;
    private double price;
    private float volume;
    private float pitch;
    private int groupID;
    private int id;
    private int timeInSeconds;
    private int cooldown;

    public Shout(int groupID, int id, String name, String path, String permission, Material mat, double price, float volume, float pitch, int timeInSeconds, int cooldown){
        this.groupID = groupID;
        this.id = id;
        this.name = name;
        this.path = path;
        this.permission = permission;
        this.mat = mat;
        this.price = price;
        this.volume = volume;
        this.pitch = pitch;
        this.timeInSeconds = timeInSeconds;
        this.cooldown = cooldown;
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

    public int getGroupID(){
        return this.groupID;
    }

    public void play(Player p){
        if(p != null){
            if(ShoutManager.MAIN_COOLDOWN.contains(p.getUniqueId().toString())) return;
            if(!p.hasPermission(ShoutManager.getBypassPermission()) || !p.isOp()){
                ShoutManager.MAIN_COOLDOWN.add(p.getUniqueId().toString());
            }
            p.getWorld().playSound(p.getLocation(), this.getPath(), this.getVolume(), this.getPitch());
            int time = this.timeInSeconds != -1 ? this.timeInSeconds : 5;
            int taskID = PCS_Shout.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(PCS_Shout.getInstance(), () -> {
                Player player = p;
                player.spawnParticle(Particle.NOTE, p.getLocation(), 4, 0, player.getHeight(), 0);
            }, 0, 3);
            PCS_Shout.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(PCS_Shout.getInstance(), r -> {
                PCS_Shout.getInstance().getServer().getScheduler().cancelTask(taskID);
            }, time * 20);
            PCS_Shout.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(PCS_Shout.getInstance(), r -> {
                ShoutManager.MAIN_COOLDOWN.remove(p.getUniqueId().toString());
            }, cooldown * 20);
        }
    }

    @Override
    public boolean equals(Object other){
        if(other instanceof Shout){
            Shout sOther = (Shout)other;
            if(sOther.groupID == this.groupID && sOther.id == this.id){
                return true;
            }
        }
        return false;
    }
}
