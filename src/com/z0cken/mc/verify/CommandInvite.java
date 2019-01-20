package com.pr0gramm.mc.verify;

import com.mashape.unirest.http.exceptions.UnirestException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;


public class CommandInvite extends Command {

    @SuppressWarnings("WeakerAccess")
    public CommandInvite() {
        super("invite");
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if(commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            if(args.length == 1 || args.length == 2) {
                if(PCS_Checkpoint.getInstance().getPersona(player) != null) {
                    int invites = DatabaseHelper.getInvites(player.getUniqueId());

                    if(invites > 0) {

                        try {
                            UUID uuid = Util.getMojangUUID(args[0]);
                            if(uuid.equals(player.getUniqueId())) {
                                //Self invite
                                player.sendMessage("§c§l>_ §7Ob du behindert bist?");
                            } else if(DatabaseHelper.isGuest(uuid) || DatabaseHelper.isVerified(uuid)) {
                                player.sendMessage("§c§l>_ §7§o" + args[0] + "§7 ist bereits Mitglied!");
                            } else {
                                if(args.length == 1) {
                                    player.sendMessage("§8[ ------------------ §l[ §c§l>_ §8§l] §8------------------ ]");
                                    player.sendMessage("§8[ - §7Aktuell verfügbare Invites: §b" + invites);
                                    player.sendMessage("§8[ - §7Für Verstöße deiner Invites wirst Du mitbestraft");

                                    TextComponent message = new TextComponent( "> HIER < " );
                                    message.setColor(ChatColor.getByChar('b'));
                                    message.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/invite " + args[0] + " yes" ) );

                                    player.sendMessage(new ComponentBuilder("§8[ - §7Klicke ").append(message).append("§7um §o" + args[0] + "§7 einzuladen!").create());
                                    player.sendMessage("§8[ ------------------------------------------- ]");

                                } else if(args[1].equalsIgnoreCase("yes")) {
                                    DatabaseHelper.invite(uuid, player.getUniqueId());

                                    player.sendMessage("§a§l>_ §7§o" + args[0] + " §7wurde erfolgreich eingeladen!");
                                    if(--invites > 0) {
                                        player.sendMessage("§f§l>_ §7Du hast noch §b" + invites + "§7 übrig!");

                                    } else {
                                        player.sendMessage("§c§l>_ §7Du hast derzeit keine Invites mehr übrig!");
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            player.sendMessage("§c§l>_ §7ZOMG, Spieler nicht gefunden!");
                            System.err.println(e);
                        } catch (UnirestException e) {
                            e.printStackTrace();
                        }

                    } else {
                        player.sendMessage("§c§l>_ §7Du hast derzeit leider keine Invites mehr!");
                    }
                } else {
                    player.sendMessage("§c§l>_ §7Nur Mitglieder des pr0gramms können Spieler einladen!");
                }
            } else {
                player.sendMessage("§f§c§l>_ §7[Instructions]");
            }
        }
    }
}
