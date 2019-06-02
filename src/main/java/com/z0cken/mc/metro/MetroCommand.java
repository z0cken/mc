package com.z0cken.mc.metro;

import com.z0cken.mc.core.util.MessageBuilder;
import com.z0cken.mc.progression.PCS_Progression;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                                .define("LAPIS", lapis.getOrDefault(entry.getKey(), PCS_Progression.getProgression(entry.getKey(), "metro_lapis")).toString())
                                .define("KILLS", kills.getOrDefault(entry.getKey(), PCS_Progression.getProgression(entry.getKey(), "metro_kills")).toString())
                                .define("TIME", Integer.toString(time.getOrDefault(entry.getKey(), PCS_Progression.getProgression(entry.getKey(), "metro_time")) / 60))
                                .build(PCS_Metro.getInstance().getConfig().getString("messages.leaderboard")));
                        }
                    } catch (SQLException e) {
                        player.spigot().sendMessage(PCS_Metro.getInstance().getMessageBuilder().build(PCS_Metro.getInstance().getConfig().getString("messages.error")));
                        e.printStackTrace();
                    }

                    break;
                case "list":
                    Set<Player> set = Metro.getInstance().getPlayersInside();
                    StringBuilder stringBuilder = new StringBuilder();
                    int i = 0;
                    for(Player p : set) {
                        i++;
                        stringBuilder.append(p.getName());
                        if(i < set.size()) stringBuilder.append(", ");
                    }
                    if(stringBuilder.length() > 0) player.spigot().sendMessage(PCS_Metro.getInstance().getMessageBuilder().define("VALUE", Integer.toString(set.size())).define("LIST", stringBuilder.toString()).build(PCS_Metro.getInstance().getConfig().getString("messages.list")));
                    break;
                case "toggle":
                    boolean value = Metro.getInstance().isExcluded(player);
                    Metro.getInstance().setExcluded(player, !value);
                    player.spigot().sendMessage(PCS_Metro.getInstance().getMessageBuilder().define("VALUE", !value ? "§causgeschaltet" : "§aeingeschaltet").build(PCS_Metro.getInstance().getConfig().getString("messages.toggle")));
                    break;
                case "map":
                    ItemStack is = player.getInventory().getItemInMainHand();
                    if(is == null || (is.getType() != Material.FILLED_MAP && is.getType() != Material.MAP)) {
                        player.spigot().sendMessage(PCS_Metro.getInstance().getMessageBuilder().build(PCS_Metro.getInstance().getConfig().getString("messages.map")));
                        break;
                    }

                    if(is.getType() == Material.MAP) is.setType(Material.FILLED_MAP);

                    if(args.length == 1) {
                        MapMeta mapMeta = (MapMeta) is.getItemMeta();
                        mapMeta.setMapId(1);
                        mapMeta.setLore(List.of("§6Metro Minimap"));
                        is.setItemMeta(mapMeta);
                    } else if(player.hasPermission("pcs.metro.map.all")){
                        final MetroMapRenderer renderer = MetroMapRenderer.getByName(args[1]);
                        MapMeta mapMeta = (MapMeta) is.getItemMeta();
                        renderer.subscribe(Bukkit.getMap((short) mapMeta.getMapId()));
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
                builder = builder.define("XP-G", Integer.toString(PCS_Progression.getSum("metro_xp")))
                        .define("LAPIS-G", Integer.toString(PCS_Progression.getSum("metro_lapis")))
                        .define("KILLS-G", Integer.toString(PCS_Progression.getSum("metro_kills")))
                        .define("TIME-G", Integer.toString(PCS_Progression.getSum("metro_time") / 60));
            } catch (SQLException e) {
                String s1 = ChatColor.RED + " --- ";
                builder = builder.define("XP-G", s1)
                        .define("LAPIS-G", s1)
                        .define("KILLS-G", s1)
                        .define("TIME-G", s1);
                e.printStackTrace();
            }

            try {
                builder = builder.define("XP", Integer.toString(PCS_Progression.getProgression(player, "metro_xp")))
                        .define("LAPIS", Integer.toString(PCS_Progression.getProgression(player, "metro_lapis")))
                        .define("KILLS", Integer.toString(PCS_Progression.getProgression(player, "metro_kills")))
                        .define("TIME", Integer.toString(PCS_Progression.getProgression(player, "metro_time") / 60));
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
