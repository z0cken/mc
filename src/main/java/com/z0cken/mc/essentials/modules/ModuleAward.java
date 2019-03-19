package com.z0cken.mc.essentials.modules;

import com.z0cken.mc.core.persona.Persona;
import com.z0cken.mc.core.persona.PersonaAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.sql.SQLException;

public class ModuleAward extends Module implements Listener, CommandExecutor {

    ModuleAward(String configPath) {
        super(configPath);
        registerCommand("award");
    }

    @Override
    protected void load() {

    }

    @Override
    public boolean onCommand(CommandSender commandsender, Command command, String s, String[] args) {
        if(command.getName().equalsIgnoreCase("award")) {
            Player target = Bukkit.getPlayer(args[0]);
            if(target != null) {
                try {
                    Persona.Badge badge = Persona.Badge.valueOf(args[1].toUpperCase());

                    PersonaAPI.getPersona(target.getUniqueId()).awardBadge(badge);
                    commandsender.sendMessage("Badge freigeschaltet!");

                } catch (IllegalArgumentException e) {
                    commandsender.sendMessage("Badge nicht gefunden!");
                } catch (SQLException e) {
                    commandsender.sendMessage("Datenbankfehler!");
                    e.printStackTrace();
                }

            } else {
                commandsender.sendMessage("Spieler nicht gefunden!");
            }
        }
        return false;
    }
}
