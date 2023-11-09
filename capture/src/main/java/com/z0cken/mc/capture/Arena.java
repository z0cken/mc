package com.z0cken.mc.capture;

import com.z0cken.mc.core.bukkit.Menu;
import com.z0cken.mc.core.persona.BoardProfile;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public final class Arena implements Listener {

    private static final Menu kitMenu = loadKits();

    private static final CaptureTeam red = new CaptureTeam("teams.red", "Rot", ChatColor.RED, Color.RED, BarColor.RED);
    private static final CaptureTeam blue = new CaptureTeam("teams.blue", "Blau", ChatColor.BLUE, Color.BLUE, BarColor.BLUE);

    private static final Set<CaptureTeam> teams = Set.of(red, blue);
    private static final Set<BlockState> chests = new HashSet<>();
    private static final Map<Player, CaptureTeam> players = new HashMap<>();
    private static final Map<Player, Kit> playerKits = new HashMap<>();
    private static final Map<CaptureTeam, Queue<Player>> queues = new HashMap<>();

    private static volatile boolean paused, live;
    private static BukkitTask gameTime;

    Arena() {
        Bukkit.getPluginManager().registerEvents(this, PCS_Capture.getInstance());
        teams.forEach(team -> queues.put(team, new LinkedList<>()));
        ScoreboardManager.setText(2, ChatColor.DARK_GRAY + "━━━━━━━━━━━━━");
        ScoreboardManager.setText(3, "Restzeit");
        pushScores();

        new BukkitRunnable() {
            @Override
            public void run() {
                pushQueue();
            }
        }.runTaskTimer(PCS_Capture.getInstance(), 20, 20);
    }

    public static void enter(Player player, BoardProfile.Team boardTeam) {

        CaptureTeam team = boardTeam == BoardProfile.Team.RED ? red : blue;
        players.put(player, team);
        team.addPlayer(player);
        if(Arena.isLive()) new BukkitRunnable() {
            @Override
            public void run() {
                team.spawnPlayer(player);
            }
        }.runTaskLater(PCS_Capture.getInstance(), 10);
    }

    public static CaptureTeam getTeam(Player player) {
        return players.getOrDefault(player, null);
    }


    public static boolean startGame() {
        if(live) return false;

        Arrays.stream(PCS_Capture.getInstance().getWorld().getLoadedChunks()).map(Chunk::getTileEntities).collect(Collectors.toList()).forEach(array -> {
            chests.addAll(Arrays.stream(array).filter(state -> state.getType() == Material.CHEST).collect(Collectors.toSet()));
        });

        players.keySet().forEach(p -> p.setScoreboard(ScoreboardManager.getScoreboard()));
        teams.forEach(team -> team.getPlayers().forEach(team::spawnPlayer));
        gameTime = new GameTimeRunnable(PCS_Capture.getInstance().getConfig().getInt("runtime")).runTaskTimer(PCS_Capture.getInstance(), 20, 20);

        live = true;
        return true;
    }

    public static void resetChests() {
        for(BlockState blockState : chests) {
            final Block block = blockState.getLocation().getBlock();
            if(block.getType() != Material.CHEST) continue;
            ((Chest)block).getBlockInventory().setContents(((Chest)blockState).getBlockInventory().getContents());
        }
    }

    public static void pushQueue() {
        final int max =
                queues.entrySet().stream().mapToInt(entry -> entry.getKey().getBukkitTeam().getSize() + entry.getValue().size()).min().orElseThrow(IllegalStateException::new)
                + PCS_Capture.getInstance().getConfig().getInt("max-imbalance");

        queues.forEach((team, queue) -> {
            int size = team.getBukkitTeam().getSize();
            while(size < max && !queue.isEmpty()) {
                Player player = queue.poll();
                players.put(player, team);
                team.addPlayer(player);
            }
        });
    }

    public static boolean endGame() {
        CaptureTeam winner = getWinningTeam();
        if(winner == null) return false;

        //TODO Announce winner
        Bukkit.broadcastMessage(winner.getDisplayName() + " gewinnt!");
        players.forEach((key, value) -> {
            Util.resetPlayer(key);
            key.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0));
            key.teleport(value.getSpectatorLocation());
            queues.get(value).add(key);
        });

        teams.forEach(CaptureTeam::reset);
        players.keySet().forEach(p -> p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard()));
        gameTime.cancel();
        gameTime = null;

        live = false;
        return true;
    }

    public static boolean isPaused() {
        return paused;
    }

    public static void setPaused(boolean paused) {
        Arena.paused = paused;
    }

    public static boolean isLive() {
        return live;
    }

    public static CaptureTeam getWinningTeam() {
        if(red.getScore() > blue.getScore()) return red;
        else if(blue.getScore() > red.getScore()) return blue;
        else return null;
    }

    public static void pushScores() {
        ScoreboardManager.setText(1, ChatColor.RED.toString() + ChatColor.BOLD + red.getScore() + ChatColor.GRAY + " - " + ChatColor.BLUE.toString() + ChatColor.BOLD + blue.getScore());
    }

    private static Menu loadKits() {
        Menu m =  new Menu(PCS_Capture.getInstance(), 1, "Kits");
        final ConfigurationSection section = PCS_Capture.getInstance().getConfig().getConfigurationSection("kits");
        for(String s : section.getKeys(false)) {

            final ConfigurationSection child = section.getConfigurationSection(s);
            Kit kit = new Kit(child.getConfigurationSection("items"));

            ItemStack itemStack = child.getSerializable("button", ItemStack.class);
            Menu.Button b = new Menu.Button((menu, button, player, inventoryClickEvent) -> {
                final CaptureTeam team = Arena.getTeam(player);
                Kit k = getKit(player);
                setKit(player, kit);
                if(k == null && Arena.isLive()) team.spawnPlayer(player);
            }, itemStack.getType());
            b.setItemMeta(itemStack.getItemMeta());

            m.setItem(Integer.parseInt(s), b);
        }
        return m;
    }

    public static Menu getKitMenu() {
        return kitMenu;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if(isPaused() && players.containsKey(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.BEACON) event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(BlockPlaceEvent event) {
        if(isLive() && event.getBlockPlaced() != null && event.getBlockPlaced().getType() == Material.BEACON) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if(kitMenu.equals(event.getInventory())) {
            if(getKit((Player) event.getPlayer()) == null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        event.getPlayer().openInventory(event.getInventory());
                    }
                }.runTaskLater(PCS_Capture.getInstance(), 1);
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Kit kit = getKit(event.getPlayer());
        if(kit != null && kit.contains(event.getItemDrop().getItemStack().getType())) event.setCancelled(true);
    }

    @Nullable
    public static Kit getKit(Player player) {
        return playerKits.getOrDefault(player, null);
    }

    public static void setKit(Player player, Kit kit) {
        if(!players.containsKey(player)) throw new IllegalArgumentException();
        playerKits.put(player, kit);
    }
}
