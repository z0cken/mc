package com.z0cken.mc.metro;

import com.z0cken.mc.core.util.MessageBuilder;
import com.z0cken.mc.progression.PCS_Progression;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Map;

public class MetroCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        if(args.length > 0) {
            switch (args[0].toLowerCase()) {

                case "top":
                    try {
                        int i = 0;
                        final Map<OfflinePlayer, Integer> leaderboard = PCS_Progression.getLeaderboard("metro_xp", 10);
                        final Map<OfflinePlayer, Integer> lapis = PCS_Progression.getLeaderboard("metro_lapis", 10);
                        final Map<OfflinePlayer, Integer> kills = PCS_Progression.getLeaderboard("metro_kills", 10);
                        final Map<OfflinePlayer, Integer> time = PCS_Progression.getLeaderboard("metro_time", 10);

                        player.spigot().sendMessage(PCS_Metro.getInstance().getMessageBuilder().define("TITLE", "Rangliste").build(PCS_Metro.getInstance().getConfig().getString("messages.header")));
                        for(Map.Entry<OfflinePlayer, Integer> entry : leaderboard.entrySet()) {
                            i++;
                            player.spigot().sendMessage(PCS_Metro.getInstance().getMessageBuilder()
                                .define("INDEX", Integer.toString(i))
                                .define("PLAYER", entry.getKey().getName())
                                .define("XP", entry.getValue().toString())
                                .define("LAPIS", lapis.get(entry.getKey()).toString())
                                .define("KILLS", kills.get(entry.getKey()).toString())
                                .define("TIME", Integer.toString(time.get(entry.getKey()) / 60))
                                .build(PCS_Metro.getInstance().getConfig().getString("messages.leaderboard")));
                        }
                    } catch (SQLException e) {
                        player.spigot().sendMessage(PCS_Metro.getInstance().getMessageBuilder().build(PCS_Metro.getInstance().getConfig().getString("messages.error")));
                        e.printStackTrace();
                    }

                    break;
                case "reload":
                case "rl":
                    if(sender.hasPermission("pcs.metro.reload")) {
                        Metro.getInstance().reload();
                        sender.spigot().sendMessage(PCS_Metro.getInstance().getMessageBuilder().build("{PREFIX} Metro neu geladen"));
                    }
                    break;

            }
        } else {
            //Main#
            StringBuilder activeSb = new StringBuilder();
            StringBuilder inactiveSb = new StringBuilder();

            for(Station station : Metro.getInstance().getStations()) {
                if(station.isActive()) {
                    activeSb.append(ChatColor.GREEN).append(station.getName()).append("\n");
                } else {
                    inactiveSb.append(ChatColor.RED).append(station.getName()).append("\n");
                }
            }

            activeSb.setLength(Math.max(0, activeSb.length() - 1));
            inactiveSb.setLength(Math.max(0, inactiveSb.length() - 1));

            MessageBuilder builder = PCS_Metro.getInstance().getMessageBuilder()
                    .define("RATE", Integer.toString(Metro.getInstance().getRate()))
                    .define("INTERVAL", Integer.toString(Metro.getInstance().getInterval()))
                    .define("DAILYRATE", Integer.toString(Metro.getInstance().getInterval() * Metro.getInstance().getRate()))
                    .define("STATIONS-ACTIVE", new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(activeSb.toString())}))
                    .define("STATIONS-INACTIVE", new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(inactiveSb.toString())}));

            try {
                builder = builder.define("XP", Integer.toString(PCS_Progression.getSum("metro_xp")))
                        .define("LAPIS", Integer.toString(PCS_Progression.getSum("metro_lapis")))
                        .define("KILLS", Integer.toString(PCS_Progression.getSum("metro_kills")))
                        .define("TIME", Integer.toString(PCS_Progression.getSum("metro_time")));
            } catch (SQLException e) {
                String s1 = ChatColor.RED + " --- ";
                builder = builder.define("XP", s1)
                        .define("LAPIS", s1)
                        .define("KILLS", s1)
                        .define("TIME", s1);
                e.printStackTrace();
            }

            for(String msg : PCS_Metro.getInstance().getConfig().getStringList("messages.info")) {
                player.spigot().sendMessage(builder.build(msg));
            }
        }


        return true;
    }
}
