package com.z0cken.mc.raid;

import com.google.gson.reflect.TypeToken;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Predicate;

public abstract class Team implements Listener {

    protected final WeakReference<Raid> raid;
    private final String name;
    private ConfigurationSection config;

    private final Map<UUID, GamePlayer> players = Collections.synchronizedMap(new HashMap<>());

    private final List<Location> spawnPoints = new ArrayList<>();
    private final File spawnPointsFile;

    private final org.bukkit.scoreboard.Team scoreboardTeam;

    public Team(Raid raid, String name, Color color, ChatColor chatColor) throws FileNotFoundException {
        this.raid = new WeakReference<>(raid);
        this.name = name;

        config = PCS_Raid.getInstance().getConfig().getConfigurationSection(name);

        scoreboardTeam = raid.getScoreboard().registerNewTeam(name);
        scoreboardTeam.setColor(chatColor);
        scoreboardTeam.setAllowFriendlyFire(false);

        spawnPointsFile = new File(PCS_Raid.getInstance().getDataFolder(), name + "-spawns.json");
        System.out.println("Loading spawnpoints for team " + name);
        Util.loadCollection(spawnPoints, spawnPointsFile, new TypeToken<Collection<Location>>() {});
        if(spawnPoints.isEmpty()) {
            throw new IllegalStateException("No spawnpoints found for team " + name);
        }
    }

    public void spawn(Player player) {
        GamePlayer gp = getGamePlayer(player);
        if(gp == null) throw new IllegalArgumentException();
        Util.cleanPlayer(player, false);
        player.setExp(1F);

        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 10, true, true));

        //TODO Remove if doesnt fix stuck glitch
        raid.get().registerTask(new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(getRandomSpawn());
            }
        }.runTaskLater(PCS_Raid.getInstance(), 2));
    }

    public void spawnAll() {
        getPlayers().stream().map(GamePlayer::getPlayer).filter(Predicate.not(Objects::isNull)).forEach(this::spawn);
    }

    public GamePlayer getGamePlayer(Player player) {
        return players.getOrDefault(player.getUniqueId(), null);
    }

    public void addPlayer(UUID uuid) {
        scoreboardTeam.addEntry(Bukkit.getOfflinePlayer(uuid).getName());
        players.put(uuid, new GamePlayer(uuid));
    }

    public ConfigurationSection getConfig() {
        return config;
    }

    public org.bukkit.scoreboard.Team getScoreboardTeam() {
        return scoreboardTeam;
    }

    public Collection<GamePlayer> getPlayers() {
        return players.values();
    }

    public void handleDeath(Player player) {
        player.getInventory().clear();
        player.getActivePotionEffects().forEach(pe -> player.removePotionEffect(pe.getType()));
        player.setGameMode(GameMode.SPECTATOR);
        player.playSound(player.getLocation(), Sound.ENTITY_BAT_DEATH, 0.25F, 1.1F);

        raid.get().registerTask(new BukkitRunnable() {
            int t = getConfig().getInt("respawn-time");
            @Override
            public void run() {
                if(!player.isOnline()) {
                    cancel();
                    return;
                }

                if(t == 0) {
                    spawn(player);
                    cancel();
                    return;
                }
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Respawn in " + t + " Sekunden"));
                t--;
            }
        }.runTaskTimer(PCS_Raid.getInstance(), 0, 20));
    }

    private Location getRandomSpawn() {
        return getSpawnPoints().get(new Random().nextInt(spawnPoints.size()));
    }

    public List<Location> getSpawnPoints() {
        return spawnPoints;
    }

    public void save() throws IOException {
        System.out.println("Saving spawn points");
        Util.saveCollection(spawnPoints, spawnPointsFile);
    }

    protected String getName() {
        return name;
    }
}
