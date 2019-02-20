package com.z0cken.mc.claim;

import com.z0cken.mc.core.FriendsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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

    private static final ConcurrentHashMap<Chunk, Optional<Claim>> claims = new ConcurrentHashMap<>();

    public PCS_Claim() {
        if(instance != null) throw new IllegalStateException(this.getClass().getName() + " cannot be instantiated twice!");
    }

    @Override
    public void onLoad() {
        instance = this;
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        DatabaseHelper.connect();

        /* TODO Make optional?
        try {
            DatabaseHelper.populate(claims, Bukkit.getWorld(getConfig().getString("main-world")));
        } catch (SQLException e) {
            Bukkit.getServer().shutdown();
        }
        */

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Chunk chunk : Bukkit.getWorld("world").getLoadedChunks()) {
                    Claim claim = DatabaseHelper.getClaim(chunk);
                    claims.put(chunk, claim == null ? Optional.empty() : Optional.of(claim));
                }
            }
        }.runTaskAsynchronously(this);

        Bukkit.getPluginManager().registerEvents(new ClaimListener(), this);
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(), this);
    }

    @Override
    public void onDisable() {
        DatabaseHelper.push();
        DatabaseHelper.disconnect();

        instance = null;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        final Chunk chunk = event.getChunk();

        int x = chunk.getX(), z = chunk.getZ();
        if (x > 200 || z > 200) Bukkit.broadcastMessage("+ " + x + " | " + z);

        if (!claims.containsKey(chunk)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Claim claim = DatabaseHelper.getClaim(chunk);
                    claims.put(chunk, claim == null ? Optional.empty() : Optional.of(claim));
                }
            }.runTaskAsynchronously(this);
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        int x = event.getChunk().getX(), z = event.getChunk().getZ();
        if(x > 200 || z > 200) Bukkit.broadcastMessage("- "+x+" | "+z);

        claims.remove(event.getChunk());
    }

    public static void claim(OfflinePlayer player, @Nonnull Block baseBlock) {
        Claim claim = new Claim(player, baseBlock);
        DatabaseHelper.commit(claim);

        if(player == null) {
            claims.put(baseBlock.getChunk(), Optional.empty());
        } else {
            claims.put(baseBlock.getChunk(), Optional.of(claim));
        }
    }

    public static boolean canBuild(OfflinePlayer player, Chunk chunk) {
        OfflinePlayer owner = getOwner(chunk);

        if(player.isOnline() && player.getPlayer().hasPermission("pcs.claim.override")) return true;

        boolean friends = false;

        if(owner != null) {
            try {
                friends = FriendsAPI.areFriends(player.getUniqueId(), owner.getUniqueId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return owner == null || owner.equals(player) || friends;
    }

    public static OfflinePlayer getOwner(Chunk chunk) {
        Claim claim;

        if(claims.containsKey(chunk)) {
            claim = claims.get(chunk).orElse(null);
        } else {
            claim = DatabaseHelper.getClaim(chunk);
        }

        return claim == null ? null : claim.getPlayer();
    }

    //Broken
    public static List<Claim> getClaims(OfflinePlayer player) {
        ArrayList<Claim> list = new ArrayList<>();
        claims.forEach(((chunk, claim) -> { if(claim.isPresent() && player.equals(claim.get().getPlayer())) list.add(claim.get()); }));
        return list;
    }
}
