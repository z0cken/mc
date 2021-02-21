package com.z0cken.mc.claim;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.z0cken.mc.core.util.MessageBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.EndPortalFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

class ClaimListener implements Listener {

    private static final HashMap<Block, BukkitTask> eyeTimers = new HashMap<>();
    private static final HashMap<Player, Claim.Owner> trespassers = new HashMap<>();

    static {
        new BukkitRunnable() {
            final MessageBuilder builder = new MessageBuilder();
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    BaseComponent[] message = null;
                    Claim.Owner current = PCS_Claim.getOwner(player.getLocation().getChunk());

                    if(current != null) {

                        if(!trespassers.containsKey(player) || trespassers.containsKey(player) && !trespassers.get(player).equals(current)) {
                            if(current.isPlayer(player)) {
                                player.spigot().sendMessage(builder.build(PCS_Claim.getInstance().getConfig().getString("messages.enter-self")));
                            } else message = builder.define("NAME", current.getName()).build(PCS_Claim.getInstance().getConfig().getString("messages.enter"));
                        }

                        trespassers.put(player, current);
                    } else if(trespassers.containsKey(player)) {
                            if(trespassers.get(player).isPlayer(player)) {
                                message = builder.build(PCS_Claim.getInstance().getConfig().getString("messages.leave-self"));
                            } else message = builder.define("NAME", trespassers.get(player).getName()).build(PCS_Claim.getInstance().getConfig().getString("messages.leave"));
                            trespassers.remove(player);
                        }

                    if(message != null) player.spigot().sendMessage(message);
                }
            }
        }.runTaskTimerAsynchronously(PCS_Claim.getInstance(), 100, 40);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void onBlockPlace(BlockPlaceEvent event) {
        final Block blockPlaced = event.getBlockPlaced();

        if(blockPlaced.getType() == Material.END_PORTAL_FRAME) {
            final Chunk chunk = blockPlaced.getChunk();
            final Player player = event.getPlayer();

            Claim.Owner owner = PCS_Claim.getOwner(chunk);
            if(owner == null) {
                if(!isClaimable(chunk)) {
                    event.setCancelled(true);
                    player.spigot().sendMessage(MessageBuilder.DEFAULT.build(PCS_Claim.getInstance().getConfig().getString("messages.unclaimable")));
                    return;
                }

                EndPortalFrame frame = (EndPortalFrame) blockPlaced.getBlockData();
                frame.setEye(true);
                blockPlaced.setBlockData(frame);

                Claim claim = PCS_Claim.claim(player, blockPlaced.getRelative(BlockFace.DOWN));
                showEffect(blockPlaced.getLocation());
                PCS_Claim.getInstance().getLogger().info(claim.getName() + " ADD -> " + player.getUniqueId() + " (" + player.getName() + ")");
                player.spigot().sendMessage(new MessageBuilder().define("CHUNK", claim.getName()).build(PCS_Claim.getInstance().getConfig().getString("messages.success")));
            }
        }
    }

    private static boolean isClaimable(Chunk chunk) {
        if(!PCS_Claim.getWorlds().contains(chunk.getWorld())) return false;

        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(chunk.getWorld()));
        if(regionManager == null) return false;
        BlockVector3 v1 = BlockVector3.at(chunk.getX() << 4, 0, chunk.getZ() << 4);
        BlockVector3 v2 = BlockVector3.at((chunk.getX() << 4) + 15, chunk.getWorld().getMaxHeight(), (chunk.getZ() << 4) + 15);
        ApplicableRegionSet set = regionManager.getApplicableRegions(new ProtectedCuboidRegion("dummy", v1, v2));
        return set.testState(null, PCS_Claim.CLAIM_FLAG);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void onInteract(PlayerInteractEvent event) {
        if(event.getHand() == EquipmentSlot.OFF_HAND) return;

        Block block = event.getClickedBlock();
        if(block != null && block.getType() == Material.END_PORTAL_FRAME) {
            final Chunk chunk = block.getChunk();
            final Player player = event.getPlayer();
            final Claim claim = PCS_Claim.getClaim(chunk);
            if(claim == null) return;

            final boolean isOwner = claim.getOwner().isPlayer(player);

            if(isOwner || PCS_Claim.isOverriding(player)) {
                BaseComponent[] message = null;
                MessageBuilder builder = new MessageBuilder().define("NAME", claim.getOwner().getName());

                EndPortalFrame frame = (EndPortalFrame) block.getBlockData();
                boolean hasEye = frame.hasEye();

                if(eyeTimers.containsKey(block)) {
                    eyeTimers.get(block).cancel();
                    eyeTimers.remove(block);
                }

                if(event.getAction().name().startsWith("L")) {
                    if(player.getGameMode() == GameMode.CREATIVE) event.setCancelled(true);
                    if(hasEye) {
                        //Has to right click
                        message = builder.build(PCS_Claim.getInstance().getConfig().getString("messages.right-click"));
                    } else {
                        block.breakNaturally();
                        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.END_PORTAL_FRAME, 1));
                        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 0.8F);

                        Claim c = PCS_Claim.claim(null, block);
                        PCS_Claim.getInstance().getLogger().info(c.getName() + " REM -> " + claim.getOwner().getUniqueId() + " (" + claim.getOwner().getName() + ")" + (isOwner ? "" : " - OVERRIDE by " + player.getName()));

                        trespassers.remove(player);
                        String path = isOwner ? "messages.unclaim" : "messages.unclaim-override";
                        message = builder.define("CHUNK", c.getName()).define("NAME", claim.getOwner().getName()).build(PCS_Claim.getInstance().getConfig().getString(path));
                    }
                } else {
                    frame.setEye(!hasEye);
                    block.setBlockData(frame);

                    if(hasEye) {
                        eyeTimers.put(block, new BukkitRunnable() {
                            @Override
                            public void run() {
                                if(block.getType() != Material.END_PORTAL_FRAME) return;

                                EndPortalFrame f = ((EndPortalFrame) block.getBlockData());
                                f.setEye(true);
                                block.setBlockData(f);
                            }
                        }.runTaskLater(PCS_Claim.getInstance(), PCS_Claim.getInstance().getConfig().getInt("eye-timer")*20));
                    }
                }
                player.spigot().sendMessage(message);
            }
        }
    }

    private static void showEffect(Location loc) {
        World world = loc.getWorld();
        loc.add(0, 1, 0);
        world.playEffect(loc, Effect.ENDER_SIGNAL, 10);
        world.playEffect(loc, Effect.ENDER_SIGNAL, 20);
        world.playEffect(loc, Effect.ENDER_SIGNAL, 30);
        world.playEffect(loc, Effect.ENDER_SIGNAL, 40);
        world.playEffect(loc, Effect.ENDER_SIGNAL, 50);

        world.playEffect(loc, Effect.SMOKE, 4);
        world.playEffect(loc, Effect.SMOKE, 4);
        world.playEffect(loc, Effect.SMOKE, 4);
        world.playSound(loc, Sound.BLOCK_END_PORTAL_SPAWN, 0.5F, 1);
    }
}
