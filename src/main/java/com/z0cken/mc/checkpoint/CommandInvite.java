package com.z0cken.mc.checkpoint;

import com.mashape.unirest.http.exceptions.UnirestException;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

import java.util.UUID;


public class CommandInvite extends Command {

    /*
     * TODO /invite list
     */

    private Configuration cfg;

    @SuppressWarnings("WeakerAccess")
    public CommandInvite() {
        super("invite");
        cfg = PCS_Checkpoint.getInstance().getConfig().getSection("messages");
        cfg = cfg.getSection("invite");
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            Util.MessageBuilder builder = new Util.MessageBuilder();
            boolean isMember = PCS_Checkpoint.getInstance().getPersona(player) != null;

            if (isMember) {
                int invites = DatabaseHelper.getInvites(player.getUniqueId());
                builder = new Util.MessageBuilder(null, invites);

                if (args.length == 1 || args.length == 2) {
                    builder = new Util.MessageBuilder(args[0], invites);

                    if (invites > 0) {

                        try {
                            UUID uuid = Util.getMojangUUID(args[0]);
                            if (uuid.equals(player.getUniqueId())) {
                                player.sendMessage(builder.digest(cfg.getString("denied-self")));
                            } else if (DatabaseHelper.isGuest(uuid) || DatabaseHelper.isVerified(uuid)) {
                                player.sendMessage(builder.digest(cfg.getString("denied-exists")));
                            } else {
                                if (args.length == 1) {

                                    TextComponent button = new TextComponent("> HIER < ");
                                    button.setColor(Util.MessageBuilder.getAccentColor());
                                    button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/invite " + args[0] + " yes"));

                                    for (String s : cfg.getStringList("confirmed")) {

                                        s = builder.digest(s);

                                        String[] strings = s.split("\\{BUTTON}");

                                        if (strings.length > 1) {
                                            player.sendMessage(new ComponentBuilder(strings[0]).append(button).append(strings[1]).create());
                                        } else {
                                            player.sendMessage(s);
                                        }
                                    }

                                } else if (args[1].equalsIgnoreCase("yes")) {
                                    DatabaseHelper.invite(uuid, player.getUniqueId());

                                    player.sendMessage(builder.digest(cfg.getString("confirmed-yes")));
                                    /*if (--invites > 0) {
                                        player.sendMessage(builder.digest(cfg.getString("confirmed-invites")));

                                    } else {
                                        player.sendMessage(builder.digest(cfg.getString("confirmed-invites-none")));
                                    }*/
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            player.sendMessage(builder.digest(cfg.getString("denied-notfound")));
                            System.err.println(e);
                        } catch (UnirestException e) {
                            e.printStackTrace();
                        }

                    } else {
                        player.sendMessage(builder.digest(cfg.getString("denied-noinvites")));
                    }
                } else {
                    for (String s : cfg.getStringList("instructions")) {
                        player.sendMessage(builder.digest(s));
                    }
                }
            } else {
                player.sendMessage(builder.digest(cfg.getString("denied-nomember")));
            }

        }
    }

}
