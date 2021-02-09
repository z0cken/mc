package com.z0cken.mc.claim;

import com.z0cken.mc.core.util.MessageBuilder;
import org.bukkit.Chunk;
import org.bukkit.Location;
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
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

import java.util.*;

import static org.bukkit.Material.*;

class ProtectionListener implements Listener {

    private static final Set<Material> INTERACTABLE_BLOCKS = EnumSet.of(
        //Various
        ANVIL,
        DAMAGED_ANVIL,
        CHIPPED_ANVIL,
        CHEST,
        TRAPPED_CHEST,
        BARREL,
        DISPENSER,
        DROPPER,
        NOTE_BLOCK,
        CAULDRON,
        BREWING_STAND,
        FURNACE,
        ENCHANTING_TABLE,
        JUKEBOX,
        HOPPER,
        COMPARATOR,
        REPEATER,
        TRIPWIRE,
        BEACON,
        FIRE,
        LEVER,
        CAKE,
        SMOKER,
        BLAST_FURNACE,
        LECTERN,

        //Buttons
        ACACIA_BUTTON,
        BIRCH_BUTTON,
        DARK_OAK_BUTTON,
        JUNGLE_BUTTON,
        OAK_BUTTON,
        SPRUCE_BUTTON,
        STONE_BUTTON,
        CRIMSON_BUTTON,
        POLISHED_BLACKSTONE_BUTTON,
        WARPED_BUTTON,

        //Doors
        ACACIA_DOOR,
        BIRCH_DOOR,
        DARK_OAK_DOOR,
        JUNGLE_DOOR,
        OAK_DOOR,
        IRON_DOOR,
        SPRUCE_DOOR,
        CRIMSON_DOOR,
        WARPED_DOOR,

        //Fence Gates
        ACACIA_FENCE_GATE,
        BIRCH_FENCE_GATE,
        DARK_OAK_FENCE_GATE,
        JUNGLE_FENCE_GATE,
        OAK_FENCE_GATE,
        SPRUCE_FENCE_GATE,

        //Pressure Plates
        ACACIA_PRESSURE_PLATE,
        BIRCH_PRESSURE_PLATE,
        DARK_OAK_PRESSURE_PLATE,
        HEAVY_WEIGHTED_PRESSURE_PLATE,
        JUNGLE_PRESSURE_PLATE,
        LIGHT_WEIGHTED_PRESSURE_PLATE,
        OAK_PRESSURE_PLATE,
        SPRUCE_PRESSURE_PLATE,
        STONE_PRESSURE_PLATE,
        CRIMSON_PRESSURE_PLATE,
        POLISHED_BLACKSTONE_PRESSURE_PLATE,
        WARPED_PRESSURE_PLATE,

        //Trapdoors
        ACACIA_TRAPDOOR,
        BIRCH_TRAPDOOR,
        DARK_OAK_TRAPDOOR,
        JUNGLE_TRAPDOOR,
        OAK_TRAPDOOR,
        IRON_TRAPDOOR,
        SPRUCE_TRAPDOOR,
        CRIMSON_TRAPDOOR,
        WARPED_TRAPDOOR,

        //Shulker Boxes
        SHULKER_BOX,
        BLACK_SHULKER_BOX,
        BLUE_SHULKER_BOX,
        BROWN_SHULKER_BOX,
        CYAN_SHULKER_BOX,
        GRAY_SHULKER_BOX,
        GREEN_SHULKER_BOX,
        LIGHT_BLUE_SHULKER_BOX,
        LIGHT_GRAY_SHULKER_BOX,
        LIME_SHULKER_BOX,
        MAGENTA_SHULKER_BOX,
        ORANGE_SHULKER_BOX,
        PINK_SHULKER_BOX,
        PURPLE_SHULKER_BOX,
        RED_SHULKER_BOX,
        WHITE_SHULKER_BOX,
        YELLOW_SHULKER_BOX,

        //Beds
        BLACK_BED,
        BLUE_BED,
        BROWN_BED,
        CYAN_BED,
        GRAY_BED,
        GREEN_BED,
        LIGHT_BLUE_BED,
        LIGHT_GRAY_BED,
        LIME_BED,
        MAGENTA_BED,
        ORANGE_BED,
        PINK_BED,
        PURPLE_BED,
        RED_BED,
        WHITE_BED,
        YELLOW_BED
    );

    private static final Set<Material> INTERACTABLE_ITEMS = Set.of(
        ARMOR_STAND,
        END_CRYSTAL,
        PAINTING,
        LEAD,
        ITEM_FRAME,
        BUCKET,
        LAVA_BUCKET,
        WATER_BUCKET,
        FLINT_AND_STEEL,
        BONE_MEAL,
        FIRE_CHARGE,

        WOODEN_HOE,
        STONE_HOE,
        GOLDEN_HOE,
        IRON_HOE,
        DIAMOND_HOE,

        WOODEN_AXE,
        STONE_AXE,
        GOLDEN_AXE,
        IRON_AXE,
        DIAMOND_AXE,

        //Boats
        ACACIA_BOAT,
        BIRCH_BOAT,
        DARK_OAK_BOAT,
        JUNGLE_BOAT,
        OAK_BOAT,
        SPRUCE_BOAT,

        //Minecarts
        MINECART,
        CHEST_MINECART,
        FURNACE_MINECART,
        HOPPER_MINECART,
        TNT_MINECART
    );

    private static final PCS_Claim PLUGIN = PCS_Claim.getInstance();

    private static boolean instantiated;
    private static final Map<Player, Integer> mutedFor = Collections.synchronizedMap(new HashMap<>());
    private static final Map<TNTPrimed, Chunk> explosives = new HashMap<>();  //Entries from unloaded primed TNTs will leak

    ProtectionListener() {
        if(instantiated) throw new IllegalStateException(getClass().getName() + " already instantiated!");
        instantiated = true;

        new BukkitRunnable() {
            @Override
            public void run() {
                mutedFor.replaceAll((player, integer) -> integer--);
                mutedFor.values().removeIf(i -> i.equals(0));
            }
        }.runTaskTimerAsynchronously(PLUGIN, 100, 20);
    }

    private void sendProtected(Player recipient, Claim.Owner owner) {
        recipient.spigot().sendMessage(MessageBuilder.DEFAULT.define("NAME", owner.getName()).build(PLUGIN.getConfig().getString("messages.protected")));
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
        mutedFor.put(player, PLUGIN.getConfig().getInt("mute-duration"));
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
                if(player.equals(claim.getOwner())) player.spigot().sendMessage(MessageBuilder.DEFAULT.build(PLUGIN.getConfig().getString("messages.denied-self")));
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
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        final Entity remover = event.getRemover();

        Player player = null;
        if(remover instanceof Player) {
            player = (Player) remover;
        } else if(remover instanceof Projectile) {
            ProjectileSource source = ((Projectile) remover).getShooter();
            if(source instanceof Player) player = (Player) source;
        }

        if(player == null) return;

        final BoundingBox bb = event.getEntity().getBoundingBox();


        //Prohibit if both chunks are protected from remover
        final Location min = new Location(event.getEntity().getWorld(), bb.getMinX(), bb.getMinY(), bb.getMinZ());
        final Claim minClaim = PCS_Claim.getClaim(min.getChunk());
        if(minClaim == null) return;

        final Location max = new Location(event.getEntity().getWorld(), bb.getMaxX(), bb.getMaxY(), bb.getMaxZ());
        final Claim maxClaim = PCS_Claim.getClaim(max.getChunk());
        if(maxClaim == null) return;

        if(!minClaim.canBuild(player) && !maxClaim.canBuild(player)) {
            event.setCancelled(true);
            sendProtected(player, minClaim.getOwner());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if(event.getCause() != HangingBreakEvent.RemoveCause.EXPLOSION) return;
        final BoundingBox bb = event.getEntity().getBoundingBox();

        //Prohibit if either chunk is protected
        final Location min = new Location(event.getEntity().getWorld(), bb.getMinX(), bb.getMinY(), bb.getMinZ());
        final Claim minClaim = PCS_Claim.getClaim(min.getChunk());

        final Location max = new Location(event.getEntity().getWorld(), bb.getMaxX(), bb.getMaxY(), bb.getMaxZ());
        final Claim maxClaim = PCS_Claim.getClaim(max.getChunk());

        if(minClaim != null || maxClaim != null) event.setCancelled(true);
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

        if(!legal && event.getAction() == Action.PHYSICAL) muteFor(player);
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
            if(claim.getBaseLocation().equals(blockState.getLocation())) return true;

            if(event.getPlayer() != null) return claim.canBuild(event.getPlayer());
            else if(origin != null) return !claim.canBuild(origin.getOwner().getOfflinePlayer());
            else return true;
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
        final PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if(cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL
                || cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT
                || cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {

            final Claim claim = PCS_Claim.getClaim(event.getTo().getChunk());

            if(claim != null && !claim.canBuild(event.getPlayer())) {
                event.setCancelled(true);
                if(cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
                    event.getPlayer().spigot().sendMessage(MessageBuilder.DEFAULT.define("NAME", claim.getOwner().getName()).build(PLUGIN.getConfig().getString("messages.portal-claimed")));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplosiveSpawn(EntitySpawnEvent event) {
        if(event.getEntityType() == EntityType.PRIMED_TNT) {
            explosives.put((TNTPrimed) event.getEntity(), event.getLocation().getChunk());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplosion(EntityExplodeEvent event) {
        OfflinePlayer player = null;
        if(event.getEntity() instanceof TNTPrimed) {
            final TNTPrimed tnt = (TNTPrimed) event.getEntity();
            final Chunk tntOrigin = explosives.remove(tnt);

            if(event.isCancelled()) return; //Return only after we removed the entry from explosives

            final Entity source = tnt.getSource();
            if(source instanceof OfflinePlayer) {
                player = (OfflinePlayer) source;
            } else {
                //Set player as owner of the TNT's originating chunk
                if(tntOrigin != null) {
                    final Claim originClaim = PCS_Claim.getClaim(tntOrigin);
                    if(originClaim != null) player = originClaim.getOwner().getOfflinePlayer();
                }
            }
        }

        final Iterator<Block> iterator = event.blockList().iterator();

        final Set<Claim> affectedClaims = new HashSet<>();
        while (iterator.hasNext()) {
            final Claim claim = PCS_Claim.getClaim(iterator.next().getChunk());
            if(claim != null && (player == null || !claim.canBuild(player))) {
                iterator.remove();
                affectedClaims.add(claim);
            }
        }

        if(player != null) {
            final Player p = player.getPlayer();
            if(p == null) return;

            for (Claim affectedClaim : affectedClaims) {
                sendProtected(p, affectedClaim.getOwner());
            }
        }
    }
}