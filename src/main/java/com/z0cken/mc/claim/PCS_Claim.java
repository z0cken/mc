package com.z0cken.mc.claim;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.z0cken.mc.core.util.MessageBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/** @author Flare */
@SuppressWarnings("unused")
public final class PCS_Claim extends JavaPlugin implements Listener {

    /*
     *
     * Memory usage could be further reduced at the expense of performance by only caching Chunks following the initial getOwner request
     *
     */

    private static PCS_Claim instance;

    public static PCS_Claim getInstance() {
        return instance;
    }

    public static final StateFlag CLAIM_FLAG = new StateFlag("claimable", true);
    private static final ConcurrentHashMap<ChunkCoordinate, Claim> claimedChunks = new ConcurrentHashMap<>();
    private static final Set<ChunkCoordinate> lockedChunks = Collections.synchronizedSet(new HashSet<>());
    private static final Set<Player> overriding = new HashSet<>();

    public PCS_Claim() {
        if(instance != null) throw new IllegalStateException(this.getClass().getName() + " cannot be instantiated twice!");
    }

    @Override
    public void onLoad() {
        instance = this;
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        WorldGuard.getInstance().getFlagRegistry().register(CLAIM_FLAG);
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        /* TODO Make optional?
        try {
            DatabaseHelper.populate(claimedChunks, Bukkit.getWorld(getConfig().getString("main-world")));
        } catch (SQLException e) {
            Bukkit.getServer().shutdown();
        }
        */

        //TODO Whitelist?
        //Lock all chunks until their status has been retrieved asynchronously
        final HashSet<ChunkCoordinate> coordinates = Arrays.stream(Bukkit.getWorld(getConfig().getString("main-world")).getLoadedChunks())
                                                                .map(ChunkCoordinate::new).collect(Collectors.toCollection(HashSet::new));
        lockedChunks.addAll(coordinates);

        /*new BukkitRunnable() {
            @Override
            public void run() {
                long time = System.nanoTime();

                for (Chunk chunk : chunks) {
                    Claim claim = DatabaseHelper.getClaim(chunk);
                    if(claim != null) {
                        final ChunkCoordinate coordinate = new ChunkCoordinate(chunk);
                        claimedChunks.put(coordinate, claim);
                        lockedChunks.remove(coordinate);
                    }
                }

                System.out.println("1: " + (System.nanoTime() - time) / 1000000D + " ms");
            }
        }.runTaskAsynchronously(this);*/

        new BukkitRunnable() {
            @Override
            public void run() {
                //long time = System.nanoTime();
                claimedChunks.putAll(DatabaseHelper.getClaims(Bukkit.getWorld("world"), coordinates));
                lockedChunks.removeAll(coordinates);
                //System.out.println("2: " + (System.nanoTime() - time) / 1000000D + " ms");
            }
        }.runTaskAsynchronously(this);

        Bukkit.getPluginManager().registerEvents(new ClaimListener(), this);
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(), this);
    }

    @Override
    public void onDisable() {
        DatabaseHelper.push();

        instance = null;
    }

    private static final Set<EntityType> trackedEntities = Set.of(
            EntityType.CHICKEN,
            EntityType.COW,
            EntityType.PIG,
            EntityType.SHEEP
    );

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;

            if(command.getName().equalsIgnoreCase("claim")) {

                if (args.length == 0) {
                    Claim claim = getClaim(player.getLocation().getChunk());
                    if(claim != null && claim.canBuild(player)) {
                        String blockString = "[" + claim.getBaseBlock().getX() + "|" + claim.getBaseBlock().getY() + "|" + claim.getBaseBlock().getZ() + "]";
                        MessageBuilder messageBuilder = MessageBuilder.DEFAULT.define("CLAIM", claim.getName()).define("BLOCK", blockString);
                        List<Entity> list = Arrays.asList(claim.getChunk().getEntities());
                        for(EntityType type : trackedEntities) {
                            messageBuilder = messageBuilder.define(type.name(), Long.toString(list.stream().filter(entity -> entity.getType() == type).count()));
                        }
                        MessageBuilder mb = messageBuilder;
                        getConfig().getStringList("messages.info").forEach(s -> player.spigot().sendMessage(mb.build(s)));
                    } else getConfig().getStringList("messages.help").forEach(s -> player.spigot().sendMessage(MessageBuilder.DEFAULT.build(s)));

                    return true;
                } else {
                    if(args[0].equalsIgnoreCase("override") && sender.hasPermission("pcs.claim.override")) {
                        if(overriding.contains(player)) {
                            overriding.remove(player);
                            player.spigot().sendMessage(MessageBuilder.DEFAULT.build(getConfig().getString("messages.override-off")));

                        } else {
                            overriding.add(player);
                            player.spigot().sendMessage(MessageBuilder.DEFAULT.build(getConfig().getString("messages.override-on")));
                        }

                        return true;

                    } else if (args[0].equalsIgnoreCase("unclaim") && sender.hasPermission("pcs.claim.unclaim")) {
                        Claim claim = getClaim(player.getLocation().getChunk());

                        if (claim != null) {
                            //Success
                            claim(null, claim.getBaseBlock());
                            Block b = claim.getBaseBlock().getRelative(BlockFace.UP);
                            if (b.getType() == Material.END_PORTAL_FRAME) b.setType(Material.AIR);

                            player.spigot().sendMessage(new MessageBuilder()
                                    .define("CHUNK", claim.getName())
                                    .define("NAME", claim.getOwner().getName())
                                    .build(getConfig().getString("messages.unclaim-override")));
                        } else {
                            player.sendMessage(getConfig().getString("messages.not-owned"));
                        }

                        return true;
                    }
                }
            }

        }

        return false;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        final Chunk chunk = event.getChunk();
        if(!chunk.getWorld().equals(Bukkit.getWorld(getConfig().getString("main-world")))) return;
        final ChunkCoordinate chunkCoordinate = new ChunkCoordinate(chunk);

        if (!claimedChunks.containsKey(chunkCoordinate)) {
            lockedChunks.add(chunkCoordinate);
            new BukkitRunnable() {
                @Override
                public void run() {
                    Claim claim = DatabaseHelper.getClaim(chunk);
                    if(claim != null) {
                        claimedChunks.put(chunkCoordinate, claim);
                        claim.updateBaseMaterial();
                    }
                    lockedChunks.remove(chunkCoordinate);
                }
            }.runTaskAsynchronously(this);
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if(!event.getWorld().equals(Bukkit.getWorld(getConfig().getString("main-world")))) return;
        final ChunkCoordinate chunkCoordinate = new ChunkCoordinate(event.getChunk());
        claimedChunks.remove(chunkCoordinate);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        overriding.remove(event.getPlayer());
    }

    public static Claim claim(@Nullable OfflinePlayer player, @Nonnull Block baseBlock) {
        Claim claim = new Claim(player == null ? null : player.getUniqueId(), baseBlock);
        DatabaseHelper.commit(claim);

        final ChunkCoordinate chunkCoordinate = new ChunkCoordinate(baseBlock.getChunk());
        if(player == null) claimedChunks.remove(chunkCoordinate);
        else claimedChunks.put(chunkCoordinate, claim);

        return claim;
    }

    public static Claim.Owner getOwner(@Nonnull Chunk chunk) {
        Claim claim = getClaim(chunk);
        return claim == null ? null : claim.getOwner();
    }

    public static Claim getClaim(@Nonnull Chunk chunk) {
        Claim claim;
        final ChunkCoordinate coordinate = new ChunkCoordinate(chunk);
        if(lockedChunks.contains(coordinate)) claim = DatabaseHelper.getClaim(chunk);
        else claim = claimedChunks.getOrDefault(coordinate, null);

        return claim;
    }

    public static boolean isOverriding(@Nonnull Player player) {
        return overriding.contains(player);
    }

    public static class Unsafe {

        //Disregards unpushed changes - accuracy of result not guaranteed
        public static Set<Claim> getClaims(@Nonnull World world, @Nonnull OfflinePlayer player) {
            return DatabaseHelper.getClaims(world, player);
        }

    }
}
