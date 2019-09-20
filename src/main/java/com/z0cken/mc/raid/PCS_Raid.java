package com.z0cken.mc.raid;

import com.z0cken.mc.raid.event.RaidStopEvent;
import com.z0cken.mc.raid.runnable.EndermenSpawnRunnable;
import com.z0cken.mc.raid.runnable.SoundSourceRunnable;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PCS_Raid extends JavaPlugin implements Listener {

    private static PCS_Raid instance;

    private static Raid raid;
    private static Location lobby;
    private static int target;
    private static boolean open = true;
    private static ServerState state = ServerState.WAITING;

    private NamespacedKey statusBarKey = new NamespacedKey(this, "StatusBar");
    private BossBar statusBar;

    public static PCS_Raid getInstance() {
        return instance;
    }
    //TODO Combine points of two rounds and only save after 2nd round

    @Override
    public void onLoad() {
        instance = this;
        ConfigurationSerialization.registerClass(Kit.class);
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        load();

        statusBar = Bukkit.createBossBar(statusBarKey, "", BarColor.WHITE, BarStyle.SOLID);
        new StatusChecker().runTaskTimer(this, 20, 20);
    }

    @Override
    public void onDisable() {
        if(getRaid() != null && getRaid().getState() != GameState.OVER) {
            getRaid().stop(true);
        }

        statusBar.removeAll();
        Bukkit.removeBossBar(statusBarKey);

        instance = null;
    }

    private void load() {
        lobby = PCS_Raid.getInstance().getConfig().getSerializable("lobby", Location.class);
        target = getConfig().getInt("players");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = null;
        if(sender instanceof Player) player = (Player) sender;

        if(command.getName().equalsIgnoreCase("raid")) {
            //TODO Enable
            //if(!sender.isOp()) return false;
            if(args[0].equalsIgnoreCase("start")) {
                try {
                    createRaid(args.length > 1 ? Long.parseLong(args[1]) : null);
                    sender.sendMessage(ChatColor.GREEN + "Raid gestartet");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            } else if(args[0].equalsIgnoreCase("pause")) {
                if(getRaid() != null) {
                    final boolean paused = getRaid().isPaused();
                    getRaid().setPaused(!paused);
                }

            } else if(args[0].equalsIgnoreCase("stop")) {
                if(getRaid() != null && getRaid().getState() == GameState.RUNNING) {
                    getRaid().stop(true);
                    sender.sendMessage(ChatColor.YELLOW + "Raid gestoppt");
                } else sender.sendMessage(ChatColor.RED + "Kein Raid aktiv");

            } else if(args[0].equalsIgnoreCase("reload")) {
                load();
                //TODO Enhance

            } else if(args[0].equalsIgnoreCase("serialize")) {
                if(player == null) return false;
                System.out.println(Util.getGson().toJson(player.getInventory().getItemInMainHand().serialize()));

            } else if(args[0].equalsIgnoreCase("spawnpoint")) {
                if(player == null) return false;

                Team team;
                if(args[1].equalsIgnoreCase("attacker")) {
                    team = getRaid().getAttackers();
                } else if(args[1].equalsIgnoreCase("defender")) {
                    team = getRaid().getDefenders();
                } else {
                    sender.sendMessage(ChatColor.RED + "Team nicht erkannt");
                    return false;
                }
                team.getSpawnPoints().add(player.getLocation());
                sender.sendMessage(ChatColor.GREEN + "Spawnpunkt hinzugefügt");

            } else if(args[0].equalsIgnoreCase("kit")) {
                if(player == null) return false;

                if(getRaid() != null) {
                    if(args[1].equalsIgnoreCase("set")) {
                        Kit kit = new Kit(player, args[3]);
                        Map<Integer, Kit> kits = getRaid().getAttackers().getKits();
                        kits.put(Integer.parseInt(args[2]), kit);
                        sender.sendMessage(ChatColor.GREEN + "Kit gesetzt");
                    } else if(args[1].equalsIgnoreCase("get")) {
                        Kit kit = getRaid().getAttackers().getKits().getOrDefault(Integer.parseInt(args[2]), null);
                        if(kit != null) {
                            kit.apply(player, true);
                            player.getInventory().setItemInOffHand(kit.getButton());
                        } else sender.sendMessage("Kit nicht gefunden");
                    }

                } else sender.sendMessage(ChatColor.RED + "Kein Raid gestartet");

            } else if(args[0].equalsIgnoreCase("players")) {
                target = Integer.parseInt(args[1]);
                sender.sendMessage(ChatColor.GREEN + "Spielerziel auf " + target + " gesetzt");

            } else if(args[0].equalsIgnoreCase("sound")) {
                if(player == null) return false;

                if(getRaid() != null) {
                    getRaid().addSoundSource(
                        new SoundSourceRunnable(
                        player.getLocation(),
                        args[1],
                        Integer.parseInt(args[2]),
                        Float.parseFloat(args[3]),
                        Float.parseFloat(args[4]))
                    );
                    sender.sendMessage(ChatColor.GREEN + "Sound hinzugefügt");
                } else sender.sendMessage(ChatColor.RED + "Kein Raid gestartet");

            } else if(args[0].equalsIgnoreCase("enderman")) {
                if(player == null) return false;

                if(getRaid() != null) {
                    getRaid().addEndermenSpawn(
                        new EndermenSpawnRunnable(
                        player.getLocation(),
                        Short.parseShort(args[1]),
                        Integer.parseInt(args[2]))
                    );
                    sender.sendMessage(ChatColor.GREEN + "Enderman Spawn hinzugefügt");
                } else sender.sendMessage(ChatColor.RED + "Kein Raid gestartet");
            } else if(args[0].equalsIgnoreCase("open")) {
                if(args.length == 1) {
                    sender.sendMessage(""+ !open);
                } else if(args.length == 2) {
                    Bukkit.broadcastMessage(open ? ChatColor.RED + "Raid pausiert" : ChatColor.GREEN + "Raid fortgesetzt");
                    setOpen(!open);
                }
            }
        } else if(command.getName().equalsIgnoreCase("kit")) {
            if(player == null) return false;

            if(getRaid() != null && getRaid().getAttackers().getGamePlayer(player) != null) {
                player.openInventory(getRaid().getAttackers().getKitMenu());
            }
        }
        return true;
    }

    public void createRaid(Long duration) throws FileNotFoundException {
        if(getState() == ServerState.WAITING) {
            PCS_Raid.setOpen(false);
            statusBar.setVisible(false);
            if(getRaid() != null && getRaid().getState() != GameState.OVER) throw new IllegalStateException("There is already a raid in progress!");

            TeamBuilder builder = new TeamBuilder(Bukkit.getOnlinePlayers().stream().filter(Util::isLikelyParticipant).map(Entity::getUniqueId).collect(Collectors.toSet()), true, 2);
            List<UUID>[] teams = builder.build();

            raid = new Raid(teams[0], teams[1], Duration.ofSeconds(duration == null ? getConfig().getLong("duration") : duration));
            raid.start();
            state = ServerState.FIRST_ROUND;
        } else {
            PCS_Raid.setOpen(false);
            raid = new Raid(getRaid().getDefenders().getPlayers().stream().map(GamePlayer::getUniqueId).collect(Collectors.toSet()), getRaid().getAttackers().getPlayers().stream().map(GamePlayer::getUniqueId).collect(Collectors.toSet()), Duration.ofSeconds(duration == null ? getConfig().getLong("duration") : duration));
            raid.start();
            state = ServerState.SECOND_ROUND;
        }
    }

    public boolean isOpen() {
        return open;
    }

    public static void setOpen(boolean open) {
        PCS_Raid.open = open;
    }

    public static Raid getRaid() {
        return raid;
    }

    public static Location getLobby() {
        return lobby;
    }

    public static ServerState getState() {
        return state;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(!open) {
            if(getRaid().getTeam(event.getPlayer()) == null) {
                event.getPlayer().kickPlayer("Runde läuft bereits!");
            } //else player has team, Raid handles join
        } else {
            statusBar.addPlayer(event.getPlayer());
            event.getPlayer().teleport(getLobby());
            event.getPlayer().getInventory().clear();
            event.getPlayer().setGameMode(GameMode.ADVENTURE);
            event.getPlayer().setLevel(0);
            Util.clearPotionEffects(event.getPlayer());
        }
    }

    @EventHandler
    public void onRaidStop(RaidStopEvent event) {
        if(state == ServerState.SECOND_ROUND) {
            setOpen(true);
            state = ServerState.WAITING;
        } else if(state == ServerState.FIRST_ROUND){
            try {
                //Start second round
                createRaid(null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public enum ServerState {
        WAITING, FIRST_ROUND, SECOND_ROUND
    }

    private class StatusChecker extends BukkitRunnable {

        @Override
        public void run() {
            if(getState() != ServerState.WAITING) return;

            final int likelyParticipants = (int) Bukkit.getOnlinePlayers().stream().filter(Util::isLikelyParticipant).count();

            statusBar.setVisible(true);
            statusBar.setTitle("Warte auf " + (target-likelyParticipants) + " Spieler...");
            statusBar.setProgress((double) likelyParticipants / target);

            if(likelyParticipants >= target) {
                try {
                    createRaid(null);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}