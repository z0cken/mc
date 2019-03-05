package com.z0cken.mc.essentials.modules;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.z0cken.mc.core.persona.Persona;
import com.z0cken.mc.core.persona.PersonaAPI;
import org.apache.commons.lang.time.DateUtils;
import org.apache.http.client.HttpResponseException;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class ModuleAward extends Module implements Listener, CommandExecutor {

    ModuleAward(String configPath) {
        super(configPath);
        registerCommand("award");
    }

    @Override
    protected void load() {

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Calendar calendar = new GregorianCalendar();
        calendar.set(2019, Calendar.MARCH, 9);
        if(DateUtils.isSameDay(Calendar.getInstance(), calendar) && !event.getPlayer().hasPermission("pcs.essentials.badge.bypass")) {
            Persona persona = null;
            try {
                persona = PersonaAPI.getPersona(event.getPlayer().getUniqueId());
            } catch (SQLException | UnirestException | HttpResponseException e) {
                e.printStackTrace();
            }

            if(persona != null) {
                try {
                    persona.awardBadge(Persona.Badge.EARLY_SUPPORTER);
                } catch (SQLException e) {
                    getLogger().severe("Failed to award " + Persona.Badge.EARLY_SUPPORTER.name() + " badge to " + event.getPlayer().getName());
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender commandsender, Command command, String s, String[] args) {
        if(command.getName().equalsIgnoreCase("award")) {
            Player target = Bukkit.getPlayer(args[0]);
            if(target != null) {
                try {
                    Persona.Badge badge = Persona.Badge.valueOf(args[1].toUpperCase());
                    Persona persona = null;

                    try {
                        persona = PersonaAPI.getPersona(target.getUniqueId());
                    } catch (UnirestException | HttpResponseException e) {
                        e.printStackTrace();
                    }

                    if(persona != null) {
                        persona.awardBadge(badge);
                        commandsender.sendMessage("Badge freigeschaltet!");
                    } else {
                        commandsender.sendMessage("Persona nicht gefunden!");
                    }

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
