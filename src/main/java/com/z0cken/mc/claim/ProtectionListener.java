package com.z0cken.mc.claim;

import com.z0cken.mc.core.util.MessageBuilder;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

class ProtectionListener implements Listener {

    private static final Set<Material> INTERACTABLE_BLOCKS = Set.of(
        Material.ANVIL, Material.DAMAGED_ANVIL, Material.CHIPPED_ANVIL, Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL, Material.DISPENSER, Material.DROPPER, Material.NOTE_BLOCK, Material.CAULDRON, Material.BREWING_STAND,
        Material.FURNACE, Material.ENCHANTING_TABLE, Material.JUKEBOX, Material.HOPPER, Material.COMPARATOR, Material.REPEATER, Material.TRIPWIRE, Material.BEACON, Material.FIRE, Material.LEVER, Material.CAKE,

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
        Material.BONE_MEAL,
        Material.FIRE_CHARGE,
        
        Material.WOODEN_HOE,
        Material.STONE_HOE,
        Material.GOLDEN_HOE,
        Material.IRON_HOE,
        Material.DIAMOND_HOE,

        Material.WOODEN_AXE,
        Material.STONE_AXE,
        Material.GOLDEN_AXE,
        Material.IRON_AXE,
        Material.DIAMOND_AXE,

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
    private static final Map<Player, Integer> mutedFor = Collections.synchronizedMap(new HashMap<>());
    private static final Map<TNTPrimed, Player> explosives = new HashMap<>();

    ProtectionListener() {
        if(instantiated) throw new IllegalStateException(getClass().getName() + " already instantiated!");
        instantiated = true;

        new BukkitRunnable() {
            @Override
            public void run() {
                mutedFor.replaceAll((player, integer) -> integer--);
                mutedFor.values().removeAll(Collections.singleton(0));
            }
        }.runTaskTimerAsynchronously(PCS_Claim.getInstance(), 100, 20);
    }

    private void sendProtected(Player recipient, Claim.Owner owner) {
        recipient.spigot().sendMessage(MessageBuilder.DEFAULT.define("NAME", owner.getName()).build(PCS_Claim.getInstance().getConfig().getString("messages.protected")));
        mutedFor.remove(recipient);
    }

    private boolean handleManipulation(Cancellable event, Chunk chunk, Player player, boolean silent) {
        Claim claim = PCS_Claim.getClaim(chunk);
        if(claim == null || claim.canBuild(player)) return true;
        event.setCancelled(true);
        if(!silent) sendProtected(player, claim.getOwner());
        return false;
    }

    private void muteFor(Player player) {
        mutedFor.put(player, PCS_Claim.getInstance().getConfig().getInt("mute-duration"));
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
        handleManipulation(event, event.getBlock().getChunk(), event.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if((block == null || !INTERACTABLE_BLOCKS.contains(block.getType()))
        && (event.getItem() == null || !INTERACTABLE_ITEMS.contains(event.getItem().getType()))) return;

        final Player player = event.getPlayer();
        if(block != null && block.getState() instanceof Container && player.hasPermission("pcs.claim.override")) return;

        boolean legal = handleManipulation(event, event.getClickedBlock().getChunk(), player, mutedFor.containsKey(player))
                        && handleManipulation(event, event.getClickedBlock().getRelative(event.getBlockFace()).getChunk(), player, mutedFor.containsKey(player));

        //Whitelist TNT
        if(legal) {
            if(event.getAction() == Action.RIGHT_CLICK_BLOCK && block.getType() == Material.TNT && (event.getItem().getType() == Material.FLINT_AND_STEEL || event.getItem().getType() == Material.FIRE_CHARGE)) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        final Optional<Entity> entity = block.getWorld().getNearbyEntities(block.getLocation(), 2, 2, 2).stream().filter(e -> e instanceof TNTPrimed).findAny();
                        entity.ifPresent(e -> explosives.put((TNTPrimed) e, player));
                    }
                }.runTaskLater(PCS_Claim.getInstance(), 1);
            }
        }
        else if(event.getAction() == Action.PHYSICAL) muteFor(player);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if(event.getRightClicked() instanceof Tameable && event.getPlayer().equals(((Tameable)event.getRightClicked()).getOwner())) return;
        handleManipulation(event, event.getRightClicked().getLocation().getChunk(), event.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        handleManipulation(event, event.getRightClicked().getLocation().getChunk(), event.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLeashEntity(PlayerLeashEntityEvent event) {
        if(event.getEntity() instanceof Tameable && ((Tameable)event.getEntity()).getOwner().equals(event.getPlayer())) return;
        handleManipulation(event, event.getEntity().getLocation().getChunk(), event.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if(event.getCaught() == null) return;
        handleManipulation(event, event.getCaught().getLocation().getChunk(), event.getPlayer(), false);
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
        handleManipulation(event, event.getItem().getLocation().getChunk(), (Player) entity, mutedFor.containsKey(entity));
        muteFor((Player) entity);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if(event.getAttacker() == null) return;
        final EntityDamageByEntityEvent e = new EntityDamageByEntityEvent(event.getAttacker(), event.getVehicle(), EntityDamageEvent.DamageCause.CUSTOM, 100);
        onEntityDamageByEntity(e);
        if(e.isCancelled()) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof Monster
                || event.getEntityType() == EntityType.SLIME
                || event.getEntityType() == EntityType.PHANTOM) return;

        Entity damager = event.getDamager();

        if(damager.getType() != EntityType.PLAYER) {
            if(damager instanceof Projectile) {
                ProjectileSource source = ((Projectile)damager).getShooter();
                if(source instanceof Monster) return;
                else if(source instanceof Player) damager = (Entity) source;
            } else return;
        }

        final Claim claim = PCS_Claim.getClaim(event.getEntity().getLocation().getChunk());
        if(claim == null) return;

        if(claim.canBuild((OfflinePlayer) damager)) return;

        //Exclude players in alien claims
        if(event.getEntity().getType() != EntityType.PLAYER || claim.canBuild((OfflinePlayer) event.getEntity())) {
            event.setCancelled(true);
            //sendProtected((Player) damager, claim.getOwner());
        }
    }

     /*
     private String locToString(Location location) {
        return location.getWorld().getEnvironment().name() + " | " + location.getX()  + " | " + location.getY() + " | " + location.getZ();
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        System.out.println("Portal: " + event.getCause().name() + " / " +  locToString(event.getFrom()) + " | " + locToString(event.getTo()));

        if(event.getCause() != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) return;

        Claim claim = PCS_Claim.getClaim(event.getTo().getChunk());
        if(claim != null && !claim.canBuild(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().spigot().sendMessage(MessageBuilder.DEFAULT.define("NAME", claim.getOwner().getName()).build(PCS_Claim.getInstance().getConfig().getString("messages.portal-claimed")));
        }
        System.out.println(event.isCancelled());
    }

    public void onPortalCreate(PortalCreateEvent event) {
        final Entity entity = event.getEntity();

        System.out.println("Portal create: " + event.getReason() + " | " + entity);
        if(event.getReason() != PortalCreateEvent.CreateReason.NETHER_PAIR) return;

        event.getBlocks().forEach(blockState -> System.out.println(blockState.getWorld().getName()));
        event.setCancelled(true);
        if(true) return;

        Set<Claim> claims = event.getBlocks().stream().map(BlockState::getChunk).map(PCS_Claim::getClaim).collect(Collectors.toSet());

        if(!claims.isEmpty() && entity instanceof Player) {
            Player player = (Player) entity;
            if(claims.stream().anyMatch(c -> !c.canBuild(player))) {
                //TODO Msg
            }
        }

    }*/

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFlow(BlockFromToEvent event) {
        if(event.getBlock().getChunk() == event.getToBlock().getChunk()) return;
        final Claim to = PCS_Claim.getClaim(event.getToBlock().getChunk());
        if(to == null) return;
        final Claim from = PCS_Claim.getClaim(event.getBlock().getChunk());
        if(from != null && to.canBuild(from.getOwner().getOfflinePlayer())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if(event.getDirection() == BlockFace.DOWN || event.getDirection() == BlockFace.UP) return;

        Claim origin = PCS_Claim.getClaim(event.getBlock().getChunk());
        final Block targetBlock = event.getBlock().getRelative(event.getDirection(), event.getBlocks().size()+1);
        Claim target = PCS_Claim.getClaim(targetBlock.getChunk());

        if(target != null && (origin == null || !target.canBuild(origin.getOwner().getOfflinePlayer()))) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if(event.getDirection() == BlockFace.DOWN || event.getDirection() == BlockFace.UP) return;

        Claim origin = PCS_Claim.getClaim(event.getBlock().getChunk());
        final Block targetBlock = event.getBlock().getRelative(event.getDirection().getOppositeFace(), event.getBlocks().size()+1);
        Claim target = PCS_Claim.getClaim(targetBlock.getChunk());

        if(target != null && (origin == null || !target.canBuild(origin.getOwner().getOfflinePlayer()))) event.setCancelled(true);
    }

    @EventHandler
    public void onStructureGrow(StructureGrowEvent event) {
        final Claim origin = PCS_Claim.getClaim(event.getLocation().getChunk());
        event.getBlocks().removeIf(blockState -> {
            final Claim claim = PCS_Claim.getClaim(blockState.getChunk());
            if(claim == null) return false;
            if(origin == null) return true;
            return origin.getOwner().equals(claim.getOwner());
        } );
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTempt(EntityTargetLivingEntityEvent event) {
        if(event.getReason() != EntityTargetEvent.TargetReason.TEMPT) return;

        Claim claim = PCS_Claim.getClaim(event.getEntity().getLocation().getChunk());
        if(claim == null) return;

        if(!claim.canBuild((Player) event.getTarget())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if(event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL || event.getCause() == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
            final Claim claim = PCS_Claim.getClaim(event.getTo().getChunk());
            if(claim != null && !claim.canBuild(event.getPlayer())) event.setCancelled(true);
        }
    }

    private static Map<EntityExplodeEvent, List<Block>> explosionBlocks = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplosionLow(EntityExplodeEvent event) {
        if(event.getEntityType() != EntityType.PRIMED_TNT) return;
        Player owner = explosives.getOrDefault(event.getEntity(), null);
        if(owner != null) {
            Iterator<Block> iterator = event.blockList().iterator();
            while (iterator.hasNext()) {
                Claim claim = PCS_Claim.getClaim(iterator.next().getChunk());
                if(claim == null || !claim.canBuild(owner)) iterator.remove();
            }
            explosionBlocks.put(event, new ArrayList<>(event.blockList()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplosionHigh(EntityExplodeEvent event) {
        List<Block> blockList = explosionBlocks.getOrDefault(event, null);
        if(blockList != null) {
            explosionBlocks.remove(event);
            event.blockList().clear();
            event.blockList().addAll(blockList);
        }
    }
}