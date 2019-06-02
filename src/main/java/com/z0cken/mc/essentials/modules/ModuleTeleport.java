package com.z0cken.mc.essentials.modules;

import com.z0cken.mc.core.FriendsAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class ModuleTeleport extends Module implements CommandExecutor {

    private static Map<UUID, Integer> cooldowns = new HashMap<>();
    private static Map<Player, Map<Player, Request>> requests = new WeakHashMap<>();

    private static int cooldown, timeout;

    ModuleTeleport(String configPath) {
        super(configPath);
        registerCommand("tpa");
    }

    @Override
    protected void load() {
        cooldown = getConfig().getInt("cooldown");
        timeout = getConfig().getInt("timeout");
    }

    @Override
    public boolean onCommand(CommandSender commandsender, Command command, String s, String[] args) {
        Player player = (Player) commandsender;
        if(command.getName().equalsIgnoreCase("tpa")) {
            if(args.length == 1) {
                Player target = Bukkit.getPlayer(args[0]);
                if(target != null) {

                    try {
                        if(!FriendsAPI.areFriends(player.getUniqueId(), target.getUniqueId())) {
                            //TODO Msg no friends
                            return true;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        //TODO Error msg
                        return true;
                    }

                } else {
                    //Not found
                }
            } else {
                //Info
            }
        } else if(command.getName().equalsIgnoreCase("tpaccept")) {

        }

        return false;
    }

    static class Request {
        Player player;
        boolean toSelf;
        int time;

        Request(Player sender, boolean toSelf) {

        }

        Player getPlayer() {
            return player;
        }

        boolean isToSelf() {
            return toSelf;
        }

        int getTime() {
            return time;
        }

        void tick() {
            time--;
        }
    }
}
