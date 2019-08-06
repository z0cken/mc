package com.z0cken.mc.end;

import com.z0cken.mc.core.util.MessageBuilder;
import com.z0cken.mc.end.egg.MagicEggType;
import com.z0cken.mc.end.phase.PhaseType;
import com.z0cken.mc.progression.PCS_Progression;
import net.minecraft.server.v1_13_R2.DamageSource;
import net.minecraft.server.v1_13_R2.EntityEnderDragon;
import net.minecraft.server.v1_13_R2.EntityHuman;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class EndCommand implements CommandExecutor {

    private BukkitTask scanTask;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        if(args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "list":
                    List<Player> list = PCS_End.getInstance().getEnd().getWorld().getPlayers();
                    StringBuilder stringBuilder = new StringBuilder();
                    int i = 0;
                    for(Player p : list) {
                        i++;
                        stringBuilder.append(p.getName());
                        if(i < list.size()) stringBuilder.append(", ");
                    }
                    if(stringBuilder.length() > 0) player.spigot().sendMessage(PCS_End.getInstance().getMessageBuilder().define("VALUE", Integer.toString(list.size())).define("LIST", stringBuilder.toString()).build(PCS_End.getInstance().getConfig().getString("messages.list")));
                    break;
                case "top":
                    try {
                        int j = 0;
                        final Map<OfflinePlayer, Integer> leaderboard = PCS_Progression.getLeaderboard("end_damage", 10);
                        final Map<OfflinePlayer, Integer> lapis = PCS_Progression.getLeaderboard("end_crystals", 10);
                        final Map<OfflinePlayer, Integer> kills = PCS_Progression.getLeaderboard("end_kills", 10);
                        final Map<OfflinePlayer, Integer> time = PCS_Progression.getLeaderboard("end_time", 10);

                        player.spigot().sendMessage(MessageBuilder.DEFAULT.define("TITLE", "Rangliste").build(PCS_End.getInstance().getConfig().getString("messages.header")));
                        for(Map.Entry<OfflinePlayer, Integer> entry : leaderboard.entrySet()) {
                            j++;
                            player.spigot().sendMessage(MessageBuilder.DEFAULT
                                    .define("INDEX", Integer.toString(j))
                                    .define("PLAYER", entry.getKey().getName())
                                    .define("DAMAGE", entry.getValue().toString())
                                    .define("CRYSTALS", lapis.getOrDefault(entry.getKey(), PCS_Progression.getProgression(entry.getKey(), "end_crystals")).toString())
                                    .define("KILLS", kills.getOrDefault(entry.getKey(), PCS_Progression.getProgression(entry.getKey(), "end_kills")).toString())
                                    .define("TIME", Integer.toString(time.getOrDefault(entry.getKey(), PCS_Progression.getProgression(entry.getKey(), "end_time")) / 60))
                                    .build(PCS_End.getInstance().getConfig().getString("messages.leaderboard")));
                        }
                    } catch (SQLException e) {
                        player.spigot().sendMessage(PCS_End.getInstance().getMessageBuilder().build(PCS_End.getInstance().getConfig().getString("messages.error")));
                        e.printStackTrace();
                    }
                    break;
                case "kill":
                    if(!player.hasPermission("pcs.end.kill")) break;

                    for(Entity entity : player.getWorld().getEntitiesByClass(EnderDragon.class)) {
                        System.out.println(entity.getLocation());
                        EntityEnderDragon dragon = (EntityEnderDragon) ((CraftEntity)entity).getHandle();
                        ((LivingEntity) entity).damage(Integer.MAX_VALUE);
                        //if(true) continue;
                        try {
                            Method method = EntityEnderDragon.class.getDeclaredMethod("dealDamage", DamageSource.class, float.class);
                            method.setAccessible(true);
                            method.invoke(dragon, DamageSource.playerAttack((EntityHuman) ((CraftEntity)player).getHandle()), 200);
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                case "rollback":
                    if(!player.hasPermission("pcs.end.rollback")) break;
                    PCS_End.getInstance().getEnd().rollback();
                    break;
                case "phase":
                    if(!player.hasPermission("pcs.end.phase")) break;
                    PCS_End.getInstance().getEnd().runPhase(PhaseType.valueOf(args[1].toUpperCase()));
                    break;
                case "save":
                    if(!player.hasPermission("pcs.end.save")) break;
                    PCS_End.getInstance().getEnd().save();
                    break;
                case "egg":
                    if(!player.hasPermission("pcs.end.egg")) break;
                    player.getInventory().addItem(MagicEggType.valueOf(args[1]).getItemStack());
                    break;
                case "scan":
                    if(!player.hasPermission("pcs.end.scan")) break;
                    if(scanTask == null || scanTask.isCancelled()) {
                        scanTask = new ManagedEndCreator(PCS_End.getInstance().getEnd().getWorld()).scan(Integer.parseInt(args[1]));
                    } else scanTask.cancel();
                    break;
            }
        } else {
            String status = "Status: ";
            switch (PCS_End.getInstance().getEnd().getPhase().getType()) {
                case DRAGON:
                    status += "Drache ist " + ChatColor.GREEN + "aktiv!";
                    break;
                case COMBAT:
                    status += "Drache spawnt in " + PCS_End.getInstance().getEnd().getRespawnRunnable().getFormattedDuration("m") + " Minuten";
                    break;
                case MAINTENANCE:
                    status += ChatColor.YELLOW + "In Wartung";
                    break;
            }

            MessageBuilder builder = MessageBuilder.DEFAULT.define("STATUS", status).define("ELYTRA", Integer.toString(PCS_End.getInstance().getEnd().getAvailableElytra()));

            try {
                builder = builder.define("DAMAGE-G", Integer.toString(PCS_Progression.getSum("end_damage")))
                        .define("CRYSTALS-G", Integer.toString(PCS_Progression.getSum("end_crystals")))
                        .define("KILLS-G", Integer.toString(PCS_Progression.getSum("end_kills")))
                        .define("TIME-G", Integer.toString(PCS_Progression.getSum("end_time") / 60));
            } catch (SQLException e) {
                String s1 = net.md_5.bungee.api.ChatColor.RED + " --- ";
                builder = builder.define("DAMAGE-G", s1)
                        .define("CRYSTALS-G", s1)
                        .define("KILLS-G", s1)
                        .define("TIME-G", s1);
                e.printStackTrace();
            }

            try {
                builder = builder.define("DAMAGE", Integer.toString(PCS_Progression.getProgression(player, "end_damage")))
                        .define("CRYSTALS", Integer.toString(PCS_Progression.getProgression(player, "end_crystals")))
                        .define("KILLS", Integer.toString(PCS_Progression.getProgression(player, "end_kills")))
                        .define("TIME", Integer.toString(PCS_Progression.getProgression(player, "end_time") / 60));
            } catch (SQLException e) {
                String s1 = net.md_5.bungee.api.ChatColor.RED + " --- ";
                builder = builder.define("DAMAGE", s1)
                        .define("CRYSTALS", s1)
                        .define("KILLS", s1)
                        .define("TIME", s1);
                e.printStackTrace();
            }

            for(String msg : PCS_End.getInstance().getConfig().getStringList("messages.info")) {
                player.spigot().sendMessage(builder.build(msg));
            }
        }


        return false;
    }
}
