package com.z0cken.mc.raid;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GamePlayer {

    private final UUID uuid;
    private Kit kit;
    private int score;

    public GamePlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public Kit getKit() {
        return kit;
    }

    public void setKit(Kit kit) {
        this.kit = kit;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int score) {
        this.score += score;
        Player player = getPlayer();
        if(player != null) {
            System.out.println("Level " + this.score);
            player.setLevel(this.score);
        }
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }
}
