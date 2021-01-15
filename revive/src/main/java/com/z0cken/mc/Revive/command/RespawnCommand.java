package com.z0cken.mc.Revive.command;

import com.z0cken.mc.Revive.RespawnHandler;
import com.z0cken.mc.Revive.Revive;
import com.z0cken.mc.core.util.MessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RespawnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                if (RespawnHandler.getHandler().isRespawning(((Player) sender).getUniqueId())) {
                    RespawnHandler.getHandler().getRespawnInstance(((Player) sender)).respawnPlayer();
                } else {
                    sender.spigot().sendMessage(new MessageBuilder().build(Revive.getPlugin().getConfig().getString("nicht-tot-error")));
                    //sender.sendMessage(ChatColor.RED + "Du kannst diesen Befehl nur nutzen, wenn du tot bist.");
                }
            } else {
                sender.spigot().sendMessage(new MessageBuilder().build(Revive.getPlugin().getConfig().getString("force-ohne-ziel-error")));
                //sender.sendMessage(ChatColor.RED + "Du kannst diesen Befehl ohne Ziel nicht von der Konsole aus benutzen. Bitte geben einen Spieler an den du wiederbeleben willst. /respawn <player>");
            }
        } else if (args.length == 1 && sender.hasPermission("z0cken.revive.others")) {
            Player target = Bukkit.getPlayer(args[0]);

            if (target != null) {
                if (RespawnHandler.getHandler().isRespawning(target)) {
                    RespawnHandler.getHandler().getRespawnInstance(target).respawnPlayer();

                    target.spigot().sendMessage(new MessageBuilder().define("PLAYER", sender.getName()).build(Revive.getPlugin().getConfig().getString("respawn-toter-force")));
                    //target.sendMessage(ChatColor.GRAY + sender.getName() + ChatColor.GREEN + " hat deinen Respawn erzwungen.");
                    sender.spigot().sendMessage(new MessageBuilder().define("PLAYER", target.getName()).build(Revive.getPlugin().getConfig().getString("respawn-sender-force")));
                    //sender.sendMessage(ChatColor.GREEN + "Du hast den Respawn von " + ChatColor.GRAY + target.getName() + ChatColor.GREEN + " erfolgreich erzwungen.");
                } else {
                    sender.spigot().sendMessage(new MessageBuilder().define("PLAYER", target.getName()).build(Revive.getPlugin().getConfig().getString("force-ziel-lebend-error")));
                    //sender.sendMessage(ChatColor.GRAY + target.getName() + ChatColor.RED + " ist nicht tot.");
                }
            } else {
                sender.spigot().sendMessage(new MessageBuilder().define("ARG0", args[0]).build(Revive.getPlugin().getConfig().getString("force-ziel-unbekannt-error")));
                //sender.sendMessage(ChatColor.RED + "Spieler " + ChatColor.GRAY + args[0] + ChatColor.RED + " existiert nicht.");
            }
        }

        return true;
    }
}
