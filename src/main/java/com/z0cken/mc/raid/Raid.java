package com.z0cken.mc.raid;

import com.google.gson.reflect.TypeToken;
import com.z0cken.mc.raid.event.EndermanCaptureEvent;
import com.z0cken.mc.raid.event.RaidStopEvent;
import com.z0cken.mc.raid.runnable.EndermanCaptureRunnable;
import com.z0cken.mc.raid.runnable.EndermenSpawnRunnable;
import com.z0cken.mc.raid.runnable.GameTimeRunnable;
import com.z0cken.mc.raid.runnable.SoundSourceRunnable;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

public class Raid implements Listener {

    private static final File endermanSpawnFile = new File(PCS_Raid.getInstance().getDataFolder(), "endermen-spawns.json");
    private static final Set<EndermenSpawnRunnable> endermenSpawns = new HashSet<>();

    private static final File soundSourceFile = new File(PCS_Raid.getInstance().getDataFolder(), "sound-sources.json");
    private static final Set<SoundSourceRunnable> soundSources = new HashSet<>();

    private final Set<Listener> listeners = new HashSet<>();
    private final Set<BukkitTask> tasks = new HashSet<>();

    private final Duration duration;

    //Holds all game endermen as keys, current leashholder as value
    private final Map<Enderman, Player> endermen = new HashMap<>();

    private final BossBar bossBar = Bukkit.createBossBar(new NamespacedKey(PCS_Raid.getInstance(), "TimeBar"), "", BarColor.WHITE, BarStyle.SOLID);
    private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

    private final TeamAttacker attackers = new TeamAttacker(this);
    private final TeamDefender defenders = new TeamDefender(this);

    private GameState state;
    private int aliensCaptured;

    public Raid(Collection<UUID> attackers, Collection<UUID> defenders, Duration duration) throws FileNotFoundException {
        attackers.forEach(this.attackers::addPlayer);
        defenders.forEach(this.defenders::addPlayer);

        Bukkit.getOnlinePlayers().forEach(getBossBar()::addPlayer);

        this.duration = duration;

        registerListener(this.attackers);
        registerListener(this.defenders);
        registerListener(this);

        System.out.println("Loading endermen spawns");
        Util.runAndCatch(() -> Util.loadCollection(endermenSpawns, endermanSpawnFile, new TypeToken<HashSet<EndermenSpawnRunnable>>() {}));
        System.out.println("Loading sound sources");
        Util.runAndCatch(() -> Util.loadCollection(soundSources, soundSourceFile, new TypeToken<HashSet<SoundSourceRunnable>>() {}));

        final Location loc = new Location(Bukkit.getWorld("world"), 0, 0, 0, 0, 0);
        if(endermenSpawns.isEmpty()) endermenSpawns.add(new EndermenSpawnRunnable(loc, (short) 2, 5));
        if(soundSources.isEmpty()) soundSources.add(new SoundSourceRunnable(loc, "sound", 10, 1F, 1F));

        //Create SoundRunnables
        endermenSpawns.forEach(r -> registerTask(r.runTaskTimer(PCS_Raid.getInstance(), 0, 20)));
        soundSources.forEach(r -> registerTask(r.runTaskTimer(PCS_Raid.getInstance(), 0, 20)));
        registerTask(new EndermanCaptureRunnable(endermen.keySet()).runTaskTimer(PCS_Raid.getInstance(), 0, 20));

        Objective objective = scoreboard.registerNewObjective("showhealth", "health", ChatColor.RED + "\u2764");
        objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
    }

    public void start() {
        state = GameState.RUNNING;
        Bukkit.broadcastMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "Raid startet...");
        registerTask(new BukkitRunnable() {
            int delay = PCS_Raid.getInstance().getConfig().getInt("countdown");

            @Override
            public void run() {
                if(delay-- > 0) {
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1.1F);
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GOLD + "Raid startet in " + ChatColor.WHITE + delay + ChatColor.GOLD + " Sekunden"));
                    });
                    return;
                }
                registerTask(new GameTimeRunnable(duration).runTaskTimer(PCS_Raid.getInstance(), 0, 20));
                getAttackers().spawnAll();
                getDefenders().spawnAll();
                cancel();
            }
        }.runTaskTimer(PCS_Raid.getInstance(), 0, 20));
    }

    public boolean isPaused() {
        return state == GameState.PAUSED;
    }

    public void setPaused(boolean paused) {
        if(getState() != GameState.RUNNING && getState() != GameState.PAUSED) throw new IllegalStateException("Game is not in progress!");

        if(paused) {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Raid pausiert...");
            state = GameState.PAUSED;
        } else {
            Bukkit.broadcastMessage(ChatColor.GREEN + "Raid fortgesetzt...");
            state = GameState.RUNNING;
        }
    }

    public void stop(boolean manual) {
        state = GameState.OVER;

        //Save scores
        if(PCS_Raid.getState() == PCS_Raid.ServerState.SECOND_ROUND) {
            List<GamePlayer> players = new ArrayList<>();
            players.addAll(getAttackers().getPlayers());
            players.addAll(getDefenders().getPlayers());
            final String timestamp = new SimpleDateFormat("HH-mm-ss").format(new Date());
            Util.writePlayerStats(players, new File(PCS_Raid.getInstance().getDataFolder(), timestamp + ".csv"));
        }

        shutdown();
        Bukkit.getPluginManager().callEvent(new RaidStopEvent(manual));
    }

    private void shutdown() {
        unregisterListeners();
        cancelTasks();

        bossBar.removeAll();
        Bukkit.removeBossBar(new NamespacedKey(PCS_Raid.getInstance(), "TimeBar"));
        endermen.keySet().forEach(Entity::remove);

        Bukkit.getOnlinePlayers().forEach(p -> {
            p.getInventory().clear();
            Util.clearPotionEffects(p);
            p.teleport(PCS_Raid.getLobby());
        });

        System.out.println("Saving endermen spawns");
        Util.runAndCatch(() -> Util.saveCollection(endermenSpawns, endermanSpawnFile));
        System.out.println("Saving sound sources");
        Util.runAndCatch(() -> Util.saveCollection(soundSources, soundSourceFile));
        Util.runAndCatch(attackers::save);
        Util.runAndCatch(defenders::save);
        System.out.println("Saving config");
        PCS_Raid.getInstance().saveConfig();
    }

    public void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, PCS_Raid.getInstance());
        listeners.add(listener);
    }

    private void unregisterListeners() {
        listeners.forEach(HandlerList::unregisterAll);
    }

    public void registerTask(BukkitTask task) {
        tasks.add(task);
    }

    private void cancelTasks() {
        tasks.forEach(BukkitTask::cancel);
    }

    public GameState getState() {
        return state;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public void addSoundSource(SoundSourceRunnable runnable) {
        soundSources.add(runnable);
        registerTask(runnable.runTaskTimer(PCS_Raid.getInstance(), 0, 20));
    }

    public void addEndermenSpawn(EndermenSpawnRunnable runnable) {
        endermenSpawns.add(runnable);
        registerTask(runnable.runTaskTimer(PCS_Raid.getInstance(), 0, 20));
    }

    public Player getLeashHolder(Enderman enderman) {
        return endermen.getOrDefault(enderman, null);
    }

    public Team getTeam(Player player) {
        if(attackers.getGamePlayer(player) != null) return attackers;
        else if(defenders.getGamePlayer(player) != null) return defenders;
        else return null;
    }

    public TeamAttacker getAttackers() {
        return attackers;
    }

    public TeamDefender getDefenders() {
        return defenders;
    }

    public int getAliensCaptured() {
        return aliensCaptured;
    }

    @EventHandler
    public void onCapture(EndermanCaptureEvent event) {
        aliensCaptured++;
        Enderman enderman = event.getEntity();
        endermen.remove(enderman);

        //Make it float
        final int duration = PCS_Raid.getInstance().getConfig().getInt("capture.duration");
        enderman.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, duration, 0));
        enderman.addScoreboardTag("captured");
        enderman.setLeashHolder(null);
        enderman.setAI(false);
        new BukkitRunnable() {
            @Override
            public void run() {
                enderman.remove();
            }
        }.runTaskLater(PCS_Raid.getInstance(), duration);
        //TODO Accelerate to center

        //Scores & Sounds
        Player player = getLeashHolder(enderman);
        if(player != null) {
            getAttackers().getGamePlayer(player).addScore(getAttackers().getConfig().getInt("scores.capture-active"));
        }

        final int score = getAttackers().getConfig().getInt("scores.capture-passive");
        PCS_Raid.getRaid().getAttackers().getPlayers().forEach(gp -> {
            Player p = gp.getPlayer();
            if(p != null) {
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.75F, 1F);
                gp.addScore(score);
            }
        });

        PCS_Raid.getRaid().getDefenders().getPlayers().forEach(gp -> {
            Player p = gp.getPlayer();
            if(p != null) p.playSound(p.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, 0.75F, 0.9F);
        });
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if(getState() == GameState.PAUSED) event.setCancelled(true);
    }

    /*@EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(getState() == GameState.PAUSED) event.setCancelled(true);
        else if(event.getAction().name().startsWith("R") && event.getItem() != null && event.getItem().getType() == Material.NETHER_STAR) {
            event.getPlayer().openInventory(getAttackers().getKitMenu());
        }
    }*/

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity entity = (LivingEntity) event.getEntity();

        Entity damager = null;
        if (event instanceof EntityDamageByEntityEvent) {
            damager = ((EntityDamageByEntityEvent) event).getDamager();
        }

        //Disable enderman & item frame damage
        if(event.getEntityType() == EntityType.ENDERMAN || event.getEntityType() == EntityType.ITEM_FRAME) {
            event.setCancelled(true);
            return;
        }

        //Prevent defender damage against mobs
        if(event.getEntityType() == EntityType.EVOKER
        || event.getEntityType() == EntityType.VINDICATOR) {
            if(damager != null && damager.getType() == EntityType.PLAYER) {
                if(getTeam((Player) damager) == getDefenders()) {
                    event.setCancelled(true);
                }
            }
        }

        //Disable TNT, fire and fireball damage for attackers
        if(event.getEntityType() == EntityType.PLAYER) {
            Player player = (Player) event.getEntity();

            if(event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
            || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
            || event.getCause() == EntityDamageEvent.DamageCause.FIRE) {
                if(getTeam(player) == getAttackers()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        //Handle death
        if(event.getFinalDamage() >= entity.getHealth()) {

            Player killer = null;
            if(damager != null && damager.getType() == EntityType.PLAYER) killer = (Player) damager;
            if(killer != null) {
                final Team killerTeam = getTeam(killer);
                killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.25F, 1F);
                killerTeam.getGamePlayer(killer).addScore(killerTeam.getConfig().getInt("score.kill"));
            }

            switch (event.getEntityType()) {
                case PLAYER: {
                    event.setCancelled(true);
                    Player player = (Player) entity;
                    Team team = getTeam(player);

                    if (team != null) {
                        team.getGamePlayer(player).addScore(team.getConfig().getInt("score.kill"));
                        team.handleDeath(player);
                    }
                    break;
                }
                case EVOKER:
                case VINDICATOR: {
                    //TODO Award
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if(event.getEntityType() != EntityType.PLAYER) return;
        //Only attackers get positive, only defenders negative
        if(event.getAction() == EntityPotionEffectEvent.Action.ADDED) {
            Team team = getTeam((Player) event.getEntity());
            if(Util.isNegative(event.getNewEffect().getType())) {
                if(team == getAttackers()) event.setCancelled(true);
            } else if(team == getDefenders()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Team team = getTeam(event.getPlayer());
        if(team != null) {
            team.spawn(event.getPlayer());
        } else if(!event.getPlayer().isOp()) event.getPlayer().kickPlayer("Die Runde hat bereits gestartet!");
        bossBar.addPlayer(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        //Redundant, is done on spawn through onJoin
        event.getPlayer().getInventory().clear();
        Util.clearPotionEffects(event.getPlayer());
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        //Prevent closing of Kit menu when not chosen
        if(event.getInventory().getType() == InventoryType.CHEST
                && getTeam((Player) event.getPlayer()) == attackers
                && getAttackers().getGamePlayer((Player) event.getPlayer()).getKit() == null) {
            registerTask(new BukkitRunnable() {
                @Override
                public void run() {
                    event.getPlayer().openInventory(event.getInventory());
                }
            }.runTaskLater(PCS_Raid.getInstance(), 1));
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if(event.getRightClicked().getType() == EntityType.ENDERMAN && getTeam(event.getPlayer()) == getAttackers()) {
            Enderman enderman = (Enderman) event.getRightClicked();
            if(!enderman.isLeashed()) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                    if(!enderman.isLeashed() && !endermen.containsValue(event.getPlayer()) && !enderman.getScoreboardTags().contains("captured")) {
                        enderman.setLeashHolder(event.getPlayer());
                        endermen.put(enderman, event.getPlayer());
                    }
                    }
                }.runTaskLater(PCS_Raid.getInstance(), 0);
            }
        }
    }

    @EventHandler
    public void onUnleash(EntityUnleashEvent event) {
        if(event.getEntityType() == EntityType.ENDERMAN && endermen.containsKey(event.getEntity())) {
            endermen.put((Enderman) event.getEntity(), null);
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if(event.getEntity().getItemStack().getType() == Material.LEAD) event.setCancelled(true);
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if(event.getEntityType() == EntityType.ENDERMAN) event.setCancelled(true);
    }


    private static final double TELEPORT_MAGIC = 0.19D;
    @EventHandler
    public void onTeleport(EntityTeleportEvent event) {
        final double diff = Math.abs(event.getTo().getY() - event.getFrom().getY());
        if(event.getEntityType() == EntityType.ENDERMAN && (diff == TELEPORT_MAGIC || diff == 1-TELEPORT_MAGIC)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if(event.getEntityType() == EntityType.ENDERMAN) {
            if(event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) event.setCancelled(true);
            else endermen.put((Enderman) event.getEntity(), null);
        }
    }

    @EventHandler
    public void onEffect(EntityPotionEffectEvent event) {
        if(event.getEntityType() == EntityType.ENDERMAN && event.getNewEffect() != null && event.getNewEffect().getType() == PotionEffectType.LEVITATION) {
            final LivingEntity entity = (LivingEntity) event.getEntity();
            entity.setLeashHolder(null);
            entity.setAI(false);
        }
    }

    @EventHandler
    public void onInventory(InventoryClickEvent event) {
        if(event.getSlot() >= 100 && event.getSlot() <= 103 || event.getSlot() == -106) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHandSwap(PlayerSwapHandItemsEvent event) {
        if(event.getOffHandItem().getType() == Material.LEAD) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if(event.getBlockPlaced().getType() == Material.TNT) {
            event.setCancelled(true);
            TNTPrimed tnt = (TNTPrimed) Bukkit.getWorld("arena").spawnEntity(event.getBlock().getLocation().add(0.5,0,0.5), EntityType.PRIMED_TNT);
            tnt.setVelocity(new Vector());
        }
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        event.blockList().clear();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(event.getItem() != null && event.getItem().getType() == Material.FIRE_CHARGE) {
            event.setCancelled(true);
            LargeFireball fireball = event.getPlayer().launchProjectile(LargeFireball.class);
            fireball.setInvulnerable(false);
            fireball.setIsIncendiary(false);
            fireball.setYield(0);

            event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount()-1);
        }
    }

    @EventHandler
    public void onProjectile(ProjectileHitEvent event) {
        final Entity hitEntity = event.getHitEntity();
        if(event.getEntity().getType() == EntityType.SNOWBALL && hitEntity != null) {
            if(hitEntity instanceof LivingEntity) {
                final ProjectileSource shooter = event.getEntity().getShooter();
                if(shooter instanceof Entity) {
                    //TODO Check if calls Event
                    //Bad if it doesnt
                    ((LivingEntity)hitEntity).damage(PCS_Raid.getInstance().getConfig().getDouble("snowball-damage"), (Entity) shooter);
                }
            }
        }
    }

    //TODO Despawn endermen on idle?

}
