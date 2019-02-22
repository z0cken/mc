package com.z0cken.mc.claim;

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
    private static final HashMap<Player, OfflinePlayer> trespassers = new HashMap<>();

    static {
        new BukkitRunnable() {
            MessageBuilder builder = new MessageBuilder();
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    BaseComponent[] message = null;
                    OfflinePlayer current = PCS_Claim.getOwner(player.getLocation().getChunk());

                    if(current != null) {

                        if(!trespassers.containsKey(player) || trespassers.containsKey(player) && !trespassers.get(player).equals(current)) {
                            if(player.equals(current)) {
                                player.spigot().sendMessage(builder.build(PCS_Claim.getInstance().getConfig().getString("messages.enter-self")));
                            } else message = builder.define("NAME", current.getName()).build(PCS_Claim.getInstance().getConfig().getString("messages.enter"));
                        }

                        trespassers.put(player, current);
                    } else if(trespassers.containsKey(player)) {
                            if(player.equals(trespassers.get(player))) {
                                message = builder.build(PCS_Claim.getInstance().getConfig().getString("messages.leave-self"));
                            } else message = builder.define("NAME", trespassers.get(player).getName()).build(PCS_Claim.getInstance().getConfig().getString("messages.leave"));
                            trespassers.remove(player);
                        }

                    if(message != null) player.spigot().sendMessage(message);
                }
            }
        }.runTaskTimerAsynchronously(PCS_Claim.getInstance(), 100, 40);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onBlockPlace(BlockPlaceEvent event) {
        if(event.isCancelled()) return;

        final Block blockPlaced = event.getBlockPlaced();

        if(blockPlaced.getType() == Material.END_PORTAL_FRAME) {
            final Chunk chunk = blockPlaced.getChunk();
            final Player player = event.getPlayer();

            OfflinePlayer owner = PCS_Claim.getOwner(chunk);
            if(owner == null) {
                EndPortalFrame frame = (EndPortalFrame) blockPlaced.getBlockData();
                frame.setEye(true);
                blockPlaced.setBlockData(frame);

                PCS_Claim.getInstance().getLogger().info("[" + chunk.getX() + "|" + chunk.getZ() + "]" + " ADD -> " + player.getUniqueId() + " (" + player.getName() + ")");
                PCS_Claim.claim(player, blockPlaced.getRelative(BlockFace.DOWN));
                player.spigot().sendMessage(new MessageBuilder().define("CHUNK", "[" + chunk.getX() + "|" + chunk.getZ() + "]").build(PCS_Claim.getInstance().getConfig().getString("messages.success")));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onInteract(PlayerInteractEvent event) {
        if(event.isCancelled() || event.getHand() == EquipmentSlot.OFF_HAND) return;
        event.getPlayer().sendMessage(event.getAction().toString() + event.getClickedBlock().getType().name());

        Block block = event.getClickedBlock();
        if(block != null && block.getType() == Material.END_PORTAL_FRAME) {
            final Chunk chunk = block.getChunk();
            final Player player = event.getPlayer();
            final OfflinePlayer owner = PCS_Claim.getOwner(chunk);
            if(owner == null) return;

            final boolean isOwner = player.equals(owner);

            if(isOwner || player.hasPermission("pcs.claim.override")) {
                BaseComponent[] message = null;
                MessageBuilder builder = new MessageBuilder().define("NAME", owner.getName());

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

                        PCS_Claim.claim(null, block);
                        PCS_Claim.getInstance().getLogger().info("[" + chunk.getX()+"|" + chunk.getZ() + "]" + " REM -> " + owner.getUniqueId() + " (" + owner.getName() + ")" + (isOwner ? "" : " - OVERRIDE by " + player.getName()));

                        trespassers.remove(player);
                        String path = isOwner ? "messages.unclaim" : "messages.unclaim-override";
                        message = builder.define("CHUNK", "[" + chunk.getX() + "|" + chunk.getZ() + "]").build(PCS_Claim.getInstance().getConfig().getString(path));
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
}
