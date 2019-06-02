package com.z0cken.mc.essentials.modules;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.*;

public class ModuleDonate extends Module implements Listener, CommandExecutor {

    //Untested

    private Hopper hopper;
    private Material material;
    private Map<Player, Integer> donations = new HashMap<>();
    private Map<Item, Player> items = new HashMap<>();

    ModuleDonate(String configPath) {
        super(configPath);
        registerCommand("donate");
    }

    @Override
    protected void load() {

    }

    @Override
    public void onDisable() {
        LinkedHashMap<Player, Integer> reverseSortedMap = new LinkedHashMap<>();
        donations.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));

        System.out.println("------ DONATIONS ------");
        for(Map.Entry<Player, Integer> entry : reverseSortedMap.entrySet()) {
            System.out.println(entry.getKey().getName() + " -> " + entry.getValue());
        }
        System.out.println("SUM:" + sum(donations.values()));
    }

    @Override
    public boolean onCommand(CommandSender commandsender, Command command, String s, String[] args) {
        Player p = (Player) commandsender;
        if(!p.hasPermission("pcs.essentials.donate")) return true;

        if (args[0].equalsIgnoreCase("material")) {
            material = Material.valueOf(args[1].toUpperCase());
            p.sendMessage(material.name());
        }

        else if (args[0].equalsIgnoreCase("hopper")) {
            Block target = p.getTargetBlock(Set.of(Material.AIR, Material.BARRIER), 10);
            if(target.getType() == Material.HOPPER) {
                hopper = (Hopper) target.getState();
                p.sendMessage(hopper.toString());
            }
        }

        else if(args[0].equalsIgnoreCase("top")) {
            LinkedHashMap<Player, Integer> reverseSortedMap = new LinkedHashMap<>();
            donations.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));

            int i = 0;
            for(Map.Entry<Player, Integer> entry : reverseSortedMap.entrySet()) {
                i++;
                p.sendMessage((entry.getKey().getName() + " -> " + entry.getValue()));
                if(i == 10) break;
            }

            p.sendMessage("Summe: " + sum(donations.values()));
        }

        return true;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if(event.getItemDrop().getItemStack().getType() == material) items.put(event.getItemDrop(), event.getPlayer());
    }

    @EventHandler
    public void onDespawn(ItemDespawnEvent event) {
        items.remove(event.getEntity());
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        items.remove(event.getItem());
    }

    @EventHandler
    public void onHopper(InventoryPickupItemEvent event) {
        if(!event.getInventory().getHolder().equals(hopper)) return;

        if(event.getItem().getItemStack().getType() != material) {
            event.setCancelled(true);
            return;
        }

        Player donor = items.getOrDefault(event.getItem(), null);
        if(donor != null) {
            if(donations.putIfAbsent(donor, 1) != null) {
                donations.computeIfPresent(donor, (k, v) ->  v + 1);
            }
            items.remove(event.getItem());
        }
    }

    public static int sum(Collection<Integer> list) {
        int sum = 0;
        for (Integer i : list) {
            sum += i;
        }
        return sum;
    }
}
