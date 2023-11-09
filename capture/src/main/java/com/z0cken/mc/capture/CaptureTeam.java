package com.z0cken.mc.capture;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class CaptureTeam implements Listener {

    private final Team team;
    private final Flag flag;
    private final Color color;
    private final BarColor barColor;
    private final Set<Player> players = new HashSet<>();

    private ProtectedRegion captureRegion;
    private Location spawnLocation, spectatorLocation;
    private int score = 0;

    public CaptureTeam(@Nonnull String path, @Nonnull String name, @Nonnull ChatColor chatColor, @Nonnull Color color, @Nonnull BarColor barColor) {
        this.team = ScoreboardManager.getScoreboard().registerNewTeam(name);
        team.setColor(chatColor);
        team.setPrefix(chatColor.toString());
        team.setAllowFriendlyFire(false);

        this.color = color;
        this.barColor = barColor;

        ConfigurationSection configSection = PCS_Capture.getInstance().getConfig().getConfigurationSection(path);
        flag = new Flag(configSection.getSerializable("flag", Location.class));
        spectatorLocation = configSection.getSerializable("spectator", Location.class);
        captureRegion = PCS_Capture.getInstance().getRegion(configSection.getString("capture-region"));
        spawnLocation = configSection.getSerializable("spawn", Location.class);

        Bukkit.getPluginManager().registerEvents(this, PCS_Capture.getInstance());
    }

    public void addPlayer(Player player) {
        getBukkitTeam().addEntry(player.getName());
        players.add(player);
        if(Arena.isLive()) spawnPlayer(player);
    }

    public void spawnPlayer(Player player) {
        if(!players.contains(player)) throw new IllegalArgumentException();
        player.setGameMode(GameMode.SURVIVAL);

        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 10, true, true));
        player.teleport(spawnLocation);

        final Kit kit = Arena.getKit(player);
        if(kit == null) player.openInventory(Arena.getKitMenu());
        else kit.apply(player);
    }

    protected Team getBukkitTeam() {
        return team;
    }

    public Location getSpectatorLocation() {
        return spectatorLocation;
    }

    public String getDisplayName() {
        return team.getColor() + team.getDisplayName();
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public Color getColor() {
        return color;
    }

    public void score(CaptureTeam enemy) {
        score++;
        Arena.pushScores();

        getPlayers().forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1));
        Bukkit.broadcastMessage(ChatColor.BOLD + getDisplayName() + " hat gepunktet!");
    }

    public int getScore() {
        return score;
    }

    public Flag getFlag() {
        return flag;
    }

    public void reset() {
        score = 0;
        Arena.pushScores();
        players.clear();
        flag.reset();
    }

    class Flag {
        private final Material MATERIAL = Material.BEACON;
        private final Block block;
        private final ItemStack itemStack;

        private Player carrier;
        private Item item;

        private Flag(Location location) {
            block = location.getBlock();
            block.setType(MATERIAL);
            block.getRelative(BlockFace.UP).setType(Material.valueOf(getBukkitTeam().getColor().name() + "_STAINED_GLASS_PANE"));

            itemStack = new ItemStack(MATERIAL);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(getDisplayName());
            itemStack.setItemMeta(itemMeta);
        }

        public void pickup(Player player) {
            carrier = player;
            carrier.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1, false, false));
            getPlayers().forEach(p -> p.playSound(p.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1, 1));


            if(item != null) item.remove();
            item = null;

            block.setType(Material.AIR);
            block.getRelative(BlockFace.UP).setType(Material.AIR);
        }

        public void drop(Item item) {
            carrier.removePotionEffect(PotionEffectType.GLOWING);
            carrier = null;
            this.item = item;

            new FlagReturnRunnable(CaptureTeam.this, item, barColor).runTaskTimer(PCS_Capture.getInstance(), 0, 20);

            Util.pulse(item, new Particle.DustOptions(color, 1));
            Util.sound(item);
            Util.firework(item, color);
        }

        public void reset() {
            if(carrier != null) {
                carrier.getInventory().remove(itemStack);
                carrier.removePotionEffect(PotionEffectType.GLOWING);
                carrier = null;
            }

            if(item != null) item.remove();
            item = null;

            block.setType(MATERIAL);
            block.getRelative(BlockFace.UP).setType(Material.valueOf(getBukkitTeam().getColor().name() + "_STAINED_GLASS_PANE"));
        }

        public Block getBlock() {
            return block;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public Player getCarrier() {
            return carrier;
        }

        public Item getItem() {
            return item;
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if(!Arena.isLive() || !event.getPlayer().equals(flag.getCarrier())) return;

        Location location = event.getPlayer().getLocation();
        CaptureTeam team = Arena.getTeam(event.getPlayer());
        if(team.captureRegion.contains((int) location.getX(), (int) location.getY(), (int) location.getZ())) {
            event.getPlayer().getInventory().remove(flag.getItemStack());
            team.score(this);
            flag.reset();
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if(!Arena.isLive()) return;

        if(event.getEntityType() == EntityType.PLAYER) {
            Player player = (Player) event.getEntity();
            if(event.getFinalDamage() < player.getHealth() || !players.contains(player)) return;
            event.setDamage(0);

            Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).filter(Predicate.not(Arena.getKit(player)::contains)).forEach(is -> {
                player.getWorld().dropItemNaturally(player.getLocation(), is);
            });

            final int respawnTime = PCS_Capture.getInstance().getConfig().getInt("respawn-time");

            Util.resetPlayer(player);
            player.teleport(spectatorLocation);
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, respawnTime * 20, 1));

            new BukkitRunnable() {
                @Override
                public void run() {
                    spawnPlayer(player);
                }
            }.runTaskLater(PCS_Capture.getInstance(), respawnTime * 20);

        } else if(event.getEntityType() == EntityType.DROPPED_ITEM && event.getEntity().equals(flag.getItem())) event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        if(!Arena.isLive() || !players.contains(player) || player.isOp()) return;

        player.damage(Integer.MAX_VALUE);
        getBukkitTeam().removeEntry(player.getName());
        players.remove(player);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        if(Arena.isLive() && !players.contains(player) && event.getClickedBlock() != null && event.getClickedBlock().equals(flag.getBlock())) {
            flag.pickup(player);
            player.getInventory().addItem(flag.getItemStack());
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if(Arena.isLive() && event.getEntity().getItemStack().equals(flag.getItemStack())) {
            flag.drop(event.getEntity());
        }
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if(Arena.isLive() && event.getItem().equals(flag.getItem())) {
            if(event.getEntityType() == EntityType.PLAYER) {
                Player player = (Player) event.getEntity();
                if(players.contains(player)) event.setCancelled(true);
                else flag.pickup(player);

            } else event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        if(Arena.isLive() && event.getEntity().getItemStack().equals(flag.getItemStack())) event.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if(!Arena.isLive()) return;

        final Block block = event.getBlock();
        if(flag.getItem() != null && flag.getItem().getLocation().distanceSquared(block.getLocation().add(0.5, 0.5, 0.5)) < Math.pow(PCS_Capture.getInstance().getConfig().getInt("flag-protection-radius"), 2)) event.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if(!Arena.isLive()) return;

        final Block block = event.getBlock();
        if(flag.getItem() != null && flag.getItem().getLocation().distanceSquared(block.getLocation().add(0.5, 0.5, 0.5)) < Math.pow(PCS_Capture.getInstance().getConfig().getInt("flag-protection-radius"), 2)) event.setCancelled(true);
    }
}
