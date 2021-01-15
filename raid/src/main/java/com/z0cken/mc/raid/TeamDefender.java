package com.z0cken.mc.raid;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import java.io.FileNotFoundException;
import java.util.UUID;

public class TeamDefender extends Team {
    private final Kit kit = getConfig().getSerializable("kit", Kit.class);

    public TeamDefender(Raid raid) throws FileNotFoundException {
        super(raid, "defender", Color.BLUE, ChatColor.BLUE);
    }

    @Override
    public void spawn(Player player) {
        super.spawn(player);

        kit.apply(player, true);
    }

    @Override
    public void addPlayer(UUID uuid) {
        super.addPlayer(uuid);
        final Player player = Bukkit.getPlayer(uuid);
        if(player != null) {
            player.sendTitle(ChatColor.BLUE + "Verteidiger", "Schütze Area 51", 5, 60, 5);
        }
    }
}
