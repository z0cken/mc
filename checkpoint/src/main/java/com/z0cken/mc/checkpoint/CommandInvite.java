package com.z0cken.mc.checkpoint;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.z0cken.mc.core.Shadow;
import com.z0cken.mc.core.persona.Persona;
import com.z0cken.mc.core.persona.PersonaAPI;
import com.z0cken.mc.core.util.CoreUtil;
import com.z0cken.mc.core.util.MessageBuilder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


class CommandInvite extends Command {

    private static Configuration cfg;

    CommandInvite() {
        super("invite");
        load();
    }

    static void load() {
        cfg = PCS_Checkpoint.getConfig().getSection("messages.invite");
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (!(commandSender instanceof ProxiedPlayer)) return;

        final ProxiedPlayer player = (ProxiedPlayer) commandSender;
        final Persona persona = PersonaAPI.getPersona(player.getUniqueId());

        if (persona != null && !persona.isGuest()) {
            MessageBuilder builder = MessageBuilder.DEFAULT;

            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("list")) {
                    try {
                        Map<UUID, Timestamp> guests = DatabaseHelper.getGuests(player.getUniqueId());

                        if (guests.isEmpty())
                            player.sendMessage(MessageBuilder.DEFAULT.build(cfg.getString("no-guests")));
                        else {
                            String s = cfg.getString("list");

                            List<BaseComponent[]> set = new ArrayList<>(guests.size());
                            for (Map.Entry<UUID, Timestamp> entry : guests.entrySet()) {
                                try {
                                    ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(entry.getKey());
                                    set.add(MessageBuilder.DEFAULT.define("NAME", proxiedPlayer != null ? proxiedPlayer.getName() : Shadow.NAME.getString(entry.getKey())).define("DATE", entry.getValue().toLocalDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yy").withZone(ZoneId.ofOffset("UTC", ZoneOffset.of("+1"))))).build(s));
                                } catch (SQLException e) {
                                    player.sendMessage(MessageBuilder.DEFAULT.build(PCS_Checkpoint.getConfig().getString("messages.error")));
                                    e.printStackTrace();
                                    return;
                                }
                            }
                            player.sendMessage(MessageBuilder.DEFAULT.build(cfg.getString("list-header")));
                            set.forEach(player::sendMessage);
                        }

                    } catch (SQLException e) {
                        player.sendMessage(builder.build(PCS_Checkpoint.getConfig().getString("messages.error")));
                        e.printStackTrace();
                    }

                } else if (args[0].equalsIgnoreCase("add") && player.hasPermission("pcs.checkpoint.invite.add")) {
                    if (args.length > 2) {
                        final ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[1]);
                        final int invites = Integer.parseInt(args[2]);
                        try {
                            DatabaseHelper.giveInvites(target.getUniqueId(), invites);
                            player.sendMessage(builder
                                    .define("NAME", target.getName())
                                    .define("INVITES", String.valueOf(invites))
                                    .build(PCS_Checkpoint.getConfig().getString("messages.invite.add"))
                            );
                        } catch (SQLException e) {
                            player.sendMessage(builder.build(PCS_Checkpoint.getConfig().getString("messages.error")));
                            e.printStackTrace();
                        }
                    }
                } else {
                    final int invites = DatabaseHelper.getInvites(player.getUniqueId());
                    builder = builder.define("INVITES", String.valueOf(invites));

                    if (args.length > 1 && args[0].equalsIgnoreCase("get") && player.hasPermission("pcs.checkpoint.invite.get")) {
                        final ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[1]);
                        player.sendMessage(builder
                                .define("NAME", target.getName())
                                .build(PCS_Checkpoint.getConfig().getString("messages.invite.get"))
                        );
                    } else if (invites > 0) {
                        builder = builder.define("NAME", args[0]);

                        try {
                            UUID uuid = CoreUtil.getMojangUUID(args[0]);
                            if (uuid.equals(player.getUniqueId())) {
                                player.sendMessage(builder.build(cfg.getString("denied-self")));
                            } else if (DatabaseHelper.isGuest(uuid) || DatabaseHelper.isVerified(uuid)) {
                                player.sendMessage(builder.build(cfg.getString("denied-exists")));
                            } else {
                                if (args.length > 1 && args[1].equalsIgnoreCase("yes")) {
                                    DatabaseHelper.invite(uuid, player.getUniqueId());
                                    PersonaAPI.invalidate(uuid);

                                    player.sendMessage(builder.build(cfg.getString("confirmed")));

                                    ProxiedPlayer guest = ProxyServer.getInstance().getPlayer(uuid);
                                    if (guest != null) {
                                        guest.sendMessage(MessageBuilder.DEFAULT.define("NAME", player.getName()).build(PCS_Checkpoint.getConfig().getString("messages.invite.confirmed-guest")));
                                    }
                                } else {
                                    for (String s : cfg.getStringList("prompt")) {
                                        player.sendMessage(builder.build(s));
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
                }
            } else {
                for (String s : cfg.getStringList("instructions")) {
                    player.sendMessage(builder.build(s));
                }
            }
        } else {
            player.sendMessage(MessageBuilder.DEFAULT.build(cfg.getString("denied-nomember")));
        }
    }

}
