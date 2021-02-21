package com.z0cken.mc.claim;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.z0cken.mc.core.util.MessageBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
    private static final ConcurrentHashMap<ChunkPosition, Claim> claimedChunks = new ConcurrentHashMap<>();
    private static final Set<ChunkPosition> lockedChunks = Collections.synchronizedSet(new HashSet<>());
    private static final Set<Player> overriding = new HashSet<>();
    private static final Map<EntityType, String> trackedEntities = new HashMap<>();
    private static final List<World> worlds                      = new ArrayList<>();

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

        for (String worldName : getConfig().getStringList("worlds")) {
            worlds.add(Bukkit.getWorld(worldName));
        }

        //TODO Whitelist?
        //Lock all chunks until their status has been retrieved asynchronously
        final Set<ChunkPosition> chunkPositions = worlds
                .stream()
                .flatMap(world -> Arrays.stream(world.getLoadedChunks()))
                .map(ChunkPosition::new)
                .collect(Collectors.toSet());

        lockedChunks.addAll(chunkPositions);

        new BukkitRunnable() {
            @Override
            public void run() {
                //long time = System.nanoTime();
                claimedChunks.putAll(DatabaseHelper.getClaims(chunkPositions));
                lockedChunks.removeAll(chunkPositions);
                //System.out.println("2: " + (System.nanoTime() - time) / 1000000D + " ms");
            }
        }.runTaskAsynchronously(this);

        getConfig().getConfigurationSection("tracked-animals").getValues(false).forEach((s, o) -> trackedEntities.put(EntityType.valueOf(s), (String) o));

        Bukkit.getPluginManager().registerEvents(new ClaimListener(), this);
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(), this);
    }

    @Override
    public void onDisable() {
        DatabaseHelper.push();

        instance = null;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;

            if(command.getName().equalsIgnoreCase("claim")) {

                if (args.length == 0) {
                    Claim claim = getClaim(player.getLocation().getChunk());
                    if(claim != null && claim.canBuild(player)) {
                        Block baseBlock = claim.getBaseBlock();
                        String blockString = "[" + baseBlock.getX() + "|" + baseBlock.getY() + "|" + baseBlock.getZ() + "]";
                        MessageBuilder messageBuilder = MessageBuilder.DEFAULT.define("CLAIM", claim.getName()).define("BLOCK", blockString);
                        getConfig().getStringList("messages.info").forEach(s -> player.spigot().sendMessage(messageBuilder.build(s)));

                        Map<EntityType, Integer> map = new HashMap<>();
                        Arrays.stream(claim.getChunk().getEntities()).forEach(e -> {
                            if(!trackedEntities.containsKey(e.getType())) return;
                            if(map.putIfAbsent(e.getType(), 1) != null) {
                                map.computeIfPresent(e.getType(), (k, v) ->  v + 1);
                            }
                        });

                        LinkedHashMap<EntityType, Integer> reverseSortedMap = new LinkedHashMap<>();
                        map.entrySet()
                                .stream()
                                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                                .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));

                        final String string = getConfig().getString("messages.animals");
                        for(Map.Entry<EntityType, Integer> entry : reverseSortedMap.entrySet()) {
                            player.spigot().sendMessage(MessageBuilder.DEFAULT.define("TYPE", trackedEntities.get(entry.getKey())).define("VALUE", entry.getValue().toString()).build(string));
                        }
                        if(reverseSortedMap.size() > 0) {
                            Integer sum = map.values().stream().mapToInt(Integer::intValue).sum();

                            int limit = getConfig().getInt("animal-limit");
                            ChatColor color = sum < limit ? ChatColor.GREEN : ChatColor.RED;
                            player.spigot().sendMessage(MessageBuilder.DEFAULT.define("VALUE", color + sum.toString()).build(getConfig().getString("messages.animals-sum")));
                        }

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
                    } else if (args[0].equalsIgnoreCase("scan") && sender.hasPermission("pcs.claim.scan")) {
                        Set<Claim> claims = DatabaseHelper.getAllClaims(player.getWorld());
                        claims.stream().filter(claim -> claim.getBaseBlock().getRelative(BlockFace.UP).getType() != Material.END_PORTAL_FRAME).forEach(claim -> player.sendMessage(claim.getBaseLocation().toString()));
                    } else if(args[0].equals("block") && sender.hasPermission("pcs.claim.block")) {
                        final Block block = player.getTargetBlock(null, 10);

                        if(block.getType() == Material.END_PORTAL_FRAME) {
                            Claim claim = PCS_Claim.getClaim(player.getLocation().getChunk());
                            if(claim != null) {
                                claim.setBaseLocation(block.getRelative(BlockFace.DOWN).getLocation());
                                player.sendMessage("Claimblock geändert! ");
                            } player.sendMessage("Gehe in das geclaimte Gebiet!");
                        } else player.sendMessage("Ziele auf einen Claimblock!");
                    }
                }
            }

        }

        return false;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        final Chunk chunk = event.getChunk();
        if(!worlds.contains(chunk.getWorld())) return;
        final ChunkPosition chunkPosition = new ChunkPosition(chunk);

        if (!claimedChunks.containsKey(chunkPosition)) {
            lockedChunks.add(chunkPosition);
            new BukkitRunnable() {
                @Override
                public void run() {
                    Claim claim = DatabaseHelper.getClaim(chunk);
                    if(claim != null) {
                        claimedChunks.put(chunkPosition, claim);
                        claim.updateBaseMaterial();
                    }
                    lockedChunks.remove(chunkPosition);
                }
            }.runTaskAsynchronously(this);
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if(!worlds.contains(event.getWorld())) return;
        final ChunkPosition chunkPosition = new ChunkPosition(event.getChunk());
        claimedChunks.remove(chunkPosition);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        overriding.remove(event.getPlayer());
    }

    public static Claim claim(@Nullable OfflinePlayer player, @Nonnull Block baseBlock) {
        Claim claim = new Claim(player == null ? null : player.getUniqueId(), baseBlock);
        DatabaseHelper.commit(claim);

        final ChunkPosition chunkPosition = new ChunkPosition(baseBlock.getChunk());
        if(player == null) claimedChunks.remove(chunkPosition);
        else claimedChunks.put(chunkPosition, claim);

        return claim;
    }

    public static Claim.Owner getOwner(@Nonnull Chunk chunk) {
        Claim claim = getClaim(chunk);
        return claim == null ? null : claim.getOwner();
    }

    public static Claim getClaim(@Nonnull Chunk chunk) {
        if(!worlds.contains(chunk.getWorld())) return null;

        Claim claim;
        final ChunkPosition chunkPosition = new ChunkPosition(chunk);
        if(lockedChunks.contains(chunkPosition)) claim = DatabaseHelper.getClaim(chunk);
        else claim = claimedChunks.getOrDefault(chunkPosition, null);

        return claim;
    }

    public static boolean isOverriding(@Nonnull Player player) {
        return overriding.contains(player);
    }

    public static List<World> getWorlds() {
        return Collections.unmodifiableList(worlds);
    }

    public static class Unsafe {

        //Disregards unpushed changes - accuracy of result not guaranteed
        public static Set<Claim> getClaims(@Nonnull World world, @Nonnull OfflinePlayer player) {
            return DatabaseHelper.getClaims(world, player);
        }

    }
}
