package com.z0cken.mc.core.bungee;

import com.z0cken.mc.core.FriendsAPI;
import com.z0cken.mc.core.Shadow;
import com.z0cken.mc.core.persona.PersonaAPI;
import com.z0cken.mc.core.util.ConfigurationType;
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
import java.util.*;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class CommandFriend extends Command {

    private static final Configuration config = PCS_Core.getConfig(ConfigurationType.PLUGIN);
    private static final HashMap<ProxiedPlayer, Collection<ProxiedPlayer>> requests = new HashMap<>();

    public CommandFriend() {
        super("friend", null, "f");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length > 0) {
            //TODO Offline target for remove
            if (args[0].equalsIgnoreCase("add")) {
                if (args.length == 2) {
                    ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[1]);
                    if (target != null) {
                        if(target.equals(player)) {
                            player.sendMessage(MessageBuilder.DEFAULT.build(config.getString("messages.add-self")));
                            return;
                        }
                        
                        MessageBuilder playerBuilder = PersonaAPI.getPlayerBuilder(player.getUniqueId(), player.getName());
                        MessageBuilder targetBuilder = PersonaAPI.getPlayerBuilder(target.getUniqueId(), target.getName());

                        try {
                            if (FriendsAPI.areFriends(player.getUniqueId(), target.getUniqueId())) {
                                player.sendMessage(targetBuilder.define("PLAYER", target.getName()).build(config.getString("messages.duplicate")));
                                return;
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                            player.sendMessage(MessageBuilder.DEFAULT.build(config.getString("messages.error")));
                            return;
                        }

                        Collection<ProxiedPlayer> targetRequests = requests.getOrDefault(target, null);
                        if (targetRequests != null && targetRequests.contains(player)) {

                            try {
                                FriendsAPI.friend(player.getUniqueId(), target.getUniqueId());
                                requests.get(target).remove(player);
                                player.sendMessage(targetBuilder.build(config.getString("messages.accept-first")));
                                target.sendMessage(playerBuilder.build(config.getString("messages.accept-third")));
                            } catch (SQLException e) {
                                e.printStackTrace();
                                player.sendMessage(MessageBuilder.DEFAULT.build(config.getString("messages.error")));
                            }
                            return;
                        }

                        Collection<ProxiedPlayer> playerRequests = requests.getOrDefault(sender, null);
                        if (playerRequests == null) {
                            playerRequests = new ArrayList<>();
                            requests.put(player, playerRequests);
                        }
                        playerRequests.add(target);

                        player.sendMessage(targetBuilder.build(config.getString("messages.add-first")));
                        target.sendMessage(playerBuilder.build(config.getString("messages.add-third")));

                    } else {
                        player.sendMessage(MessageBuilder.DEFAULT.build(config.getString("messages.not-found")));
                    }
                } else {
                    player.sendMessage(MessageBuilder.DEFAULT.build(config.getString("messages.syntax-add")));
                }
            } else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete")) {
                if (args.length == 2) {
                    ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[1]);

                    UUID uuid;
                    String name;
                    if (target == null) {
                        //TODO Set offline target
                        try {
                            uuid = Shadow.getByName(args[1]);
                            if (uuid == null) {
                                player.sendMessage(MessageBuilder.DEFAULT.build(config.getString("messages.not-found")));
                                return;
                            }
                            name = Shadow.NAME.getString(uuid);
                        } catch (SQLException e) {
                            player.sendMessage(MessageBuilder.DEFAULT.build(config.getString("messages.error")));
                            e.printStackTrace();
                            return;
                        }
                    } else {
                        uuid = target.getUniqueId();
                        name = target.getName();
                    }

                    try {
                        if (!FriendsAPI.areFriends(player.getUniqueId(), uuid)) {
                            player.sendMessage(MessageBuilder.DEFAULT.define("PLAYER", name).build(config.getString("messages.not-friends")));
                            return;
                        }

                        FriendsAPI.unfriend(player.getUniqueId(), uuid);
                        player.sendMessage(MessageBuilder.DEFAULT.define("PLAYER", name).build(config.getString("messages.remove-first")));
                        if(target != null) target.sendMessage(MessageBuilder.DEFAULT.define("PLAYER", player.getName()).build(config.getString("messages.remove-third")));

                    } catch (SQLException e) {
                        player.sendMessage(MessageBuilder.DEFAULT.build(config.getString("messages.error")));
                        e.printStackTrace();
                    }
                } else {
                    player.sendMessage(MessageBuilder.DEFAULT.build(config.getString("messages.syntax-add")));
                }
            } else if (args[0].equalsIgnoreCase("list")) {
                Map<UUID, Timestamp> friends;
                try {
                    friends = FriendsAPI.getFriends(player.getUniqueId());
                } catch (SQLException e) {
                    player.sendMessage(MessageBuilder.DEFAULT.build(config.getString("messages.error")));
                    e.printStackTrace();
                    return;
                }

                if (friends.isEmpty()) player.sendMessage(MessageBuilder.DEFAULT.build(config.getString("messages.no-friends")));
                else {
                    String s = config.getString("messages.list");

                    List<BaseComponent[]> set = new ArrayList<>(friends.size());
                    for(Map.Entry<UUID, Timestamp> entry : friends.entrySet()) {
                        try {
                            ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(entry.getKey());
                            set.add(MessageBuilder.DEFAULT.define("PLAYER", proxiedPlayer != null ? proxiedPlayer.getName() : Shadow.NAME.getString(entry.getKey())).define("DATE", entry.getValue().toLocalDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yy").withZone(ZoneId.ofOffset("UTC", ZoneOffset.of("+1"))))).build(s));
                        } catch (SQLException e) {
                            player.sendMessage(MessageBuilder.DEFAULT.build(config.getString("messages.error")));
                            e.printStackTrace();
                            return;
                        }
                    }
                    player.sendMessage(MessageBuilder.DEFAULT.build(config.getString("messages.list-header")));
                    set.forEach(player::sendMessage);
                }
            }
        } else {
            MessageBuilder builder = MessageBuilder.DEFAULT;
            config.getStringList("messages.help").forEach(s -> player.sendMessage(builder.build(s)));
        }
    }
}