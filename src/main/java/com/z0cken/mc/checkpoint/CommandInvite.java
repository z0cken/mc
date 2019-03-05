package com.z0cken.mc.checkpoint;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.z0cken.mc.core.persona.Persona;
import com.z0cken.mc.core.persona.PersonaAPI;
import com.z0cken.mc.core.util.MessageBuilder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import org.apache.http.client.HttpResponseException;

import java.sql.SQLException;
import java.util.UUID;


class CommandInvite extends Command {

    /*
     * TODO /invite list
     */

    private static final Configuration cfg = PCS_Checkpoint.getConfig().getSection("messages.invite");

    CommandInvite() {
        super("invite");
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;

            MessageBuilder builder = new MessageBuilder()
                    .define("A", PCS_Checkpoint.getConfig().getString("messages.accent-color"));


            Persona persona;

            //noinspection Duplicates
            try {
                persona = PersonaAPI.getPersona(player.getUniqueId());
            } catch (SQLException | HttpResponseException | UnirestException e) {
                e.printStackTrace();

                player.sendMessage(builder.build(PCS_Checkpoint.getConfig().getString("messages.error")));
                return;
            }

            if (persona != null && !persona.isGuest()) {
                int invites = DatabaseHelper.getInvites(player.getUniqueId());
                builder = builder.define("INVITES", Integer.toString(invites));

                if (args.length == 1 || args.length == 2) {
                    if (invites > 0) {
                        builder = builder.define("NAME", args[0]);

                        try {
                            UUID uuid = Util.getMojangUUID(args[0]);
                            if (uuid.equals(player.getUniqueId())) {
                                player.sendMessage(builder.build(cfg.getString("denied-self")));
                            } else if (DatabaseHelper.isGuest(uuid) || DatabaseHelper.isVerified(uuid)) {
                                player.sendMessage(builder.build(cfg.getString("denied-exists")));
                            } else {
                                if (args.length == 1) {
                                    for (String s : cfg.getStringList("prompt")) {
                                        player.sendMessage(builder.build(s));
                                    }

                                } else if (args[1].equalsIgnoreCase("yes")) {
                                    DatabaseHelper.invite(uuid, player.getUniqueId());
                                    PersonaAPI.updateCachedPersona(uuid);

                                    player.sendMessage(builder.build(cfg.getString("confirmed")));

                                    ProxiedPlayer guest = ProxyServer.getInstance().getPlayer(uuid);
                                    if(guest != null) {
                                        guest.sendMessage(MessageBuilder.DEFAULT.build(PCS_Checkpoint.getConfig().getString("messages.invite.confirmed-guest")));
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            player.sendMessage(builder.build(cfg.getString("denied-notfound")));
                        } catch (UnirestException e) {
                            player.sendMessage(builder.build(PCS_Checkpoint.getConfig().getString("messages.error")));
                            e.printStackTrace();
                        }

                    } else {
                        player.sendMessage(builder.build(cfg.getString("denied-noinvites")));
                    }
                } else {
                    for (String s : cfg.getStringList("instructions")) {
                        player.sendMessage(builder.build(s));
                    }
                }
            } else {
                player.sendMessage(builder.build(cfg.getString("denied-nomember")));
            }

        }
    }

}
