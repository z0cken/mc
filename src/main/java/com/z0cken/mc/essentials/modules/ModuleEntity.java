package com.z0cken.mc.essentials.modules;

import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.*;

public class ModuleEntity extends Module implements CommandExecutor, Listener {

    ModuleEntity(String configPath) {
        super(configPath);
        registerCommand("entity");
    }

    @Override
    protected void load() {

    }

    @Override
    public boolean onCommand(CommandSender commandsender, Command command, String s, String[] args) {
        Player player = (Player) commandsender;
        if(commandsender.hasPermission("pcs.essentials.inactive")) {
            EntityType type = EntityType.valueOf(args[0].toUpperCase());
            Map<Chunk, Integer> chunks = new HashMap<>();
            for (Chunk chunk : player.getWorld().getLoadedChunks()) {
                chunks.put(chunk, Math.toIntExact(Arrays.stream(chunk.getEntities()).filter(entity -> entity.getType() == type).count()));
            }

            LinkedHashMap<Chunk, Integer> reverseSortedMap = new LinkedHashMap<>();
            chunks.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));

            int i = 0;
            for(Map.Entry<Chunk, Integer> entry : reverseSortedMap.entrySet()) {
                player.sendMessage((entry.getKey().getX() << 4) + " | " + (entry.getKey().getZ() << 4) + " -> " + entry.getValue());
                if(++i == 10) break;
            }
        }
        return true;
    }
}
