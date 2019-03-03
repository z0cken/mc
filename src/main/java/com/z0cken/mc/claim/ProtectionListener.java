package com.z0cken.mc.claim;

import com.z0cken.mc.core.util.MessageBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.*;

import java.util.Set;

class ProtectionListener implements Listener {

    private static final Set<Material> INTERACTABLE_BLOCKS = Set.of(
        Material.ANVIL, Material.CHEST, Material.TRAPPED_CHEST, Material.DISPENSER, Material.DROPPER, Material.NOTE_BLOCK, Material.CAULDRON, Material.BREWING_STAND,
        Material.FURNACE, Material.ENCHANTING_TABLE, Material.JUKEBOX, Material.HOPPER, Material.COMPARATOR, Material.REPEATER, Material.TRIPWIRE, Material.BEACON,

        //Buttons
        Material.ACACIA_BUTTON,
        Material.BIRCH_BUTTON,
        Material.DARK_OAK_BUTTON,
        Material.JUNGLE_BUTTON,
        Material.OAK_BUTTON,
        Material.SPRUCE_BUTTON,
        Material.STONE_BUTTON,

        //Doors
        Material.ACACIA_DOOR,
        Material.BIRCH_DOOR,
        Material.DARK_OAK_DOOR,
        Material.JUNGLE_DOOR,
        Material.OAK_DOOR,
        Material.IRON_DOOR,
        Material.SPRUCE_DOOR,

        //Fence Gates
        Material.ACACIA_FENCE_GATE,
        Material.BIRCH_FENCE_GATE,
        Material.DARK_OAK_FENCE_GATE,
        Material.JUNGLE_FENCE_GATE,
        Material.OAK_FENCE_GATE,
        Material.SPRUCE_FENCE_GATE,

        //Pressure Plates
        Material.ACACIA_PRESSURE_PLATE,
        Material.BIRCH_PRESSURE_PLATE,
        Material.DARK_OAK_PRESSURE_PLATE,
        Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
        Material.JUNGLE_PRESSURE_PLATE,
        Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
        Material.OAK_PRESSURE_PLATE,
        Material.SPRUCE_PRESSURE_PLATE,
        Material.STONE_PRESSURE_PLATE,

        //Trapdoors
        Material.ACACIA_TRAPDOOR,
        Material.BIRCH_TRAPDOOR,
        Material.DARK_OAK_TRAPDOOR,
        Material.JUNGLE_TRAPDOOR,
        Material.OAK_TRAPDOOR,
        Material.IRON_TRAPDOOR,
        Material.SPRUCE_TRAPDOOR,

        //Shulker Boxes
        Material.SHULKER_BOX,
        Material.BLACK_SHULKER_BOX,
        Material.BLUE_SHULKER_BOX,
        Material.BROWN_SHULKER_BOX,
        Material.CYAN_SHULKER_BOX,
        Material.GRAY_SHULKER_BOX,
        Material.GREEN_SHULKER_BOX,
        Material.LIGHT_BLUE_SHULKER_BOX,
        Material.LIGHT_GRAY_SHULKER_BOX,
        Material.LIME_SHULKER_BOX,
        Material.MAGENTA_SHULKER_BOX,
        Material.ORANGE_SHULKER_BOX,
        Material.PINK_SHULKER_BOX,
        Material.PURPLE_SHULKER_BOX,
        Material.RED_SHULKER_BOX,
        Material.WHITE_SHULKER_BOX,
        Material.YELLOW_SHULKER_BOX,

        //Beds
        Material.BLACK_BED,
        Material.BLUE_BED,
        Material.BROWN_BED,
        Material.CYAN_BED,
        Material.GRAY_BED,
        Material.GREEN_BED,
        Material.LIGHT_BLUE_BED,
        Material.LIGHT_GRAY_BED,
        Material.LIME_BED,
        Material.MAGENTA_BED,
        Material.ORANGE_BED,
        Material.PINK_BED,
        Material.PURPLE_BED,
        Material.RED_BED,
        Material.WHITE_BED,
        Material.YELLOW_BED
    );

    private static final Set<Material> INTERACTABLE_ITEMS = Set.of(
        Material.ARMOR_STAND,
        Material.END_CRYSTAL,
        Material.PAINTING,
        Material.LEAD,
        Material.ITEM_FRAME,
        Material.BUCKET,
        Material.LAVA_BUCKET,
        Material.WATER_BUCKET,
        Material.FLINT_AND_STEEL,

        //Boats
        Material.ACACIA_BOAT,
        Material.BIRCH_BOAT,
        Material.DARK_OAK_BOAT,
        Material.JUNGLE_BOAT,
        Material.OAK_BOAT,
        Material.SPRUCE_BOAT,

        //Minecarts
        Material.MINECART,
        Material.CHEST_MINECART,
        Material.FURNACE_MINECART,
        Material.HOPPER_MINECART,
        Material.TNT_MINECART
    );

    private static boolean instantiated;

    ProtectionListener() {
        if(instantiated) throw new IllegalStateException(getClass().getName() + " already instantiated!");
        instantiated = true;
    }

    private void sendProtected(Player recipient, OfflinePlayer owner) {
        recipient.spigot().sendMessage(MessageBuilder.DEFAULT.define("NAME", owner.getName()).build(PCS_Claim.getInstance().getConfig().getString("messages.protected")));
    }

    private void handleManipulation(Cancellable event, Chunk chunk, Player player) {
        Claim claim = PCS_Claim.getClaim(chunk);
        if(claim == null || claim.canBuild(player)) return;
        event.setCancelled(true);
        sendProtected(player, claim.getOwner());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        final Claim claim = PCS_Claim.getClaim(event.getBlockPlaced().getChunk());
        if(claim == null) return;

        final Player player = event.getPlayer();

        boolean newClaim = event.getBlockPlaced().getType() == Material.END_PORTAL_FRAME;

        if(claim.canBuild(player)) {
            if(newClaim) {
                event.setCancelled(true);
                if(player.equals(claim.getOwner())) player.spigot().sendMessage(MessageBuilder.DEFAULT.build(PCS_Claim.getInstance().getConfig().getString("messages.denied-self")));
                else sendProtected(player, claim.getOwner());
            }
        } else {
            event.setCancelled(true);
            sendProtected(player, claim.getOwner());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        handleManipulation(event, event.getBlock().getChunk(), event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if((block == null || !INTERACTABLE_BLOCKS.contains(block.getType()))
        && (event.getItem() == null || !INTERACTABLE_ITEMS.contains(event.getItem().getType()))) return;

        handleManipulation(event, event.getClickedBlock().getChunk(), event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        handleManipulation(event, event.getRightClicked().getLocation().getChunk(), event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLeashEntity(PlayerLeashEntityEvent event) {
        handleManipulation(event, event.getEntity().getLocation().getChunk(), event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        handleManipulation(event, event.getCaught().getLocation().getChunk(), event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEggThrow(PlayerEggThrowEvent event) {
        final Claim claim = PCS_Claim.getClaim(event.getEgg().getLocation().getChunk());
        if(claim == null) return;

        final Player player = event.getPlayer();
        if(claim.canBuild(player)) return;

        event.setHatching(false);
        sendProtected(player, claim.getOwner());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        final Entity entity = event.getEntity();
        if(entity.getType() != EntityType.PLAYER) return;
        handleManipulation(event, event.getItem().getLocation().getChunk(), (Player) entity);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof Monster || event.getEntity().getType() == EntityType.SLIME) return;

        final Entity damager = event.getDamager();
        if(damager.getType() != EntityType.PLAYER) return;

        final Claim claim = PCS_Claim.getClaim(event.getEntity().getLocation().getChunk());
        if(claim == null) return;

        if(claim.canBuild((OfflinePlayer) damager)) return;

        //Exclude players in alien claims
        if(event.getEntity().getType() != EntityType.PLAYER || claim.canBuild((OfflinePlayer) event.getEntity())) {
            event.setCancelled(true);
            //sendProtected((Player) damager, claim.getOwner());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if(event.getFrom().getWorld().getEnvironment() != World.Environment.NETHER) return;
        event.getPortalTravelAgent().setCanCreatePortal(false);

        Location portal = event.getPortalTravelAgent().findPortal(event.getTo());
        Claim claim;

        if(portal != null) {
            claim = PCS_Claim.getClaim(portal.getChunk());
            if(claim != null && !claim.canBuild(event.getPlayer())) {
                event.setCancelled(true);
                event.getPlayer().spigot().sendMessage(MessageBuilder.DEFAULT.define("NAME", claim.getOwner().getName()).build(PCS_Claim.getInstance().getConfig().getString("messages.portal-claimed")));
            }
        }
        else event.getPlayer().spigot().sendMessage(MessageBuilder.DEFAULT.build(PCS_Claim.getInstance().getConfig().getString("messages.portal-new")));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFlow(BlockFromToEvent event) {
        if(event.getBlock().getChunk() == event.getToBlock().getChunk()) return;
        final Claim to = PCS_Claim.getClaim(event.getToBlock().getChunk());
        if(to == null) return;
        final Claim from = PCS_Claim.getClaim(event.getBlock().getChunk());
        if(from != null && to.canBuild(from.getOwner())) return;
        event.setCancelled(true);
    }
}
