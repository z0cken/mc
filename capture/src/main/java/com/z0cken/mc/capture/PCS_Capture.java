package com.z0cken.mc.capture;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.z0cken.mc.core.bukkit.Menu;
import com.z0cken.mc.core.persona.Persona;
import com.z0cken.mc.core.persona.PersonaAPI;
import com.z0cken.mc.core.util.MessageBuilder;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

public class PCS_Capture extends JavaPlugin implements Listener {

    private static PCS_Capture instance;

    public static PCS_Capture getInstance() {
        return instance;
    }

    private World world;

    @Override
    public void onLoad() {
        instance = this;
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        world = Bukkit.getWorld("world");
        new Arena();

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        instance = null;
        Arena.endGame();

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        if(command.getName().equalsIgnoreCase("kit")) {
            CaptureTeam team = Arena.getTeam(player);
            if(team != null) {
                player.openInventory(Arena.getKitMenu());
            } else {
                //msg
                player.sendMessage("Kein Team");
            }
        } else if(args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "pause":
                    if(!player.hasPermission("pcs.capture.pause")) break;
                        boolean paused = !Arena.isPaused();
                        Arena.setPaused(paused);
                        player.sendMessage(ChatColor.GREEN + (paused ? "Spiel pausiert" : "Spiel fährt fort"));
                    break;
                case "start":
                    if(!player.hasPermission("pcs.capture.start")) break;
                    player.sendMessage(Arena.startGame() ? (ChatColor.GREEN + "Spiel gestartet") : (ChatColor.RED + "Spiel läuft bereits"));
                    break;
                case "end":
                    if(!player.hasPermission("pcs.capture.end")) break;
                    player.sendMessage(Arena.endGame() ? (ChatColor.GREEN + "Spiel beendet") : (ChatColor.RED + "Es steht kein Gewinner fest"));
                    break;
                case "refill":
                    if(!player.hasPermission("pcs.capture.refill")) break;
                    Arena.resetChests();
                    new BukkitRunnable() {
                        int timer = 60;
                        @Override
                        public void run() {
                            Bukkit.getOnlinePlayers().forEach(p -> {
                                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, MessageBuilder.DEFAULT.define("VALUE", Integer.toString(timer)).build("§7Kistenbefüllung in §f§l{VALUE} §7Sekunden"));
                            });

                            if(timer-- == 0) {
                                cancel();
                                Arena.resetChests();
                            }
                        }
                    }.runTaskTimer(this, 0, 20);
                    break;
                case "reload":
                    if(!player.hasPermission("pcs.capture.reload")) break;
                    reloadConfig();
                    break;
            }
        }

        return true;
    }

    public World getWorld() {
        return world;
    }

    public ProtectedRegion getRegion(String id) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(world)).getRegion(id);
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Persona persona = PersonaAPI.getPersona(event.getPlayer().getUniqueId());
        if(persona.isVerified()) {
            persona.getBoardProfile().thenAccept(profile -> Arena.enter(event.getPlayer(), profile.getTeam()));
        } else {
            event.getPlayer().kickPlayer(ChatColor.RED + "Dieses Event ist Mitgliedern vorbehalten!");
        }
    }

    @EventHandler
    public void onInventory(InventoryClickEvent event) {
        //Block helmet
        if(event.getClickedInventory() == null) return;
        if(event.getClickedInventory().equals(event.getWhoClicked().getInventory()) && event.getSlot() == 39) event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(event.getItem() != null && event.getItem().getType() == Material.FIRE_CHARGE) {
            event.setCancelled(true);
            LargeFireball fireball = event.getPlayer().launchProjectile(LargeFireball.class);
            fireball.setInvulnerable(false);
            fireball.setYield(0);
            event.getPlayer().getInventory().remove(new ItemStack(Material.FIRE_CHARGE, 1));
        }
    }
}
