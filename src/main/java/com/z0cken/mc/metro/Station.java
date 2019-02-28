package com.z0cken.mc.metro;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.z0cken.mc.metro.event.StationActivateEvent;
import com.z0cken.mc.metro.event.StationDeactivateEvent;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Station implements Listener {

    private final String id, name;
    private final MetroBeacon beacon;
    private boolean active = false;
    private final ProtectedRegion region;
    private final Difficulty difficulty;

    Station(String id, String name, Location beaconLocation) {
        this.id = id;
        this.name = name;

        int supply = DatabaseHelper.getSupply(id);
        this.beacon = new MetroBeacon(beaconLocation, supply);

        this.region = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(beaconLocation.getWorld())).getRegion(id);
        this.difficulty = region.getFlag(PCS_Metro.DIFFICULTY_FLAG);

        if(supply > 0) {
            active = true;
            beaconLocation.getBlock().getRelative(BlockFace.DOWN).setType(Material.DIAMOND_BLOCK);
        } else beaconLocation.getBlock().getRelative(BlockFace.DOWN).setType(Material.BEDROCK);
    }

    public boolean contains(Location location) {
        return region.contains((int) location.getX(), (int) location.getY(), (int) location.getZ());
    }

    private void activate() {
        active = true;
        beacon.getBlock().getRelative(BlockFace.DOWN).setType(Material.DIAMOND_BLOCK);

        List<Player> players = Bukkit.getOnlinePlayers().stream().filter(p -> contains(p.getLocation())).collect(Collectors.toList());
        Bukkit.getPluginManager().callEvent(new StationActivateEvent(Metro.getInstance(), this, players));
    }

    private void deactivate() {
        active = false;
        beacon.getBlock().getRelative(BlockFace.DOWN).setType(Material.BEDROCK);

        Bukkit.getPluginManager().callEvent(new StationDeactivateEvent(Metro.getInstance(), this));
    }

    public static Station fromConfig(ConfigurationSection section, String key) {
        section = section.getConfigurationSection(key);
        String name = section.getString("name");
        Location beacon = section.getSerializable("beacon", Location.class);
        final Station station = new Station(key, name, beacon);
        System.out.println((PCS_Metro.getInstance() == null) + "");
        Bukkit.getPluginManager().registerEvents(station, PCS_Metro.getInstance());
        Bukkit.getPluginManager().registerEvents(station.getBeacon(), PCS_Metro.getInstance());

        return station;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    MetroBeacon getBeacon() {
        return beacon;
    }

    public boolean isActive() {
        return active;
    }

    public ProtectedRegion getRegion() {
        return region;
    }

    public Difficulty getDifficulty() {
        return difficulty == null ? Difficulty.NORMAL : difficulty;
    }

    class MetroBeacon implements Listener {
        private final Location location;
        private final Inventory inventory = Bukkit.createInventory(null, 9, getName());

        MetroBeacon(Location location, int supply) {
            this.location = location;

            inventory.setItem(0, new ItemStack(Material.GLASS_PANE));
            inventory.setItem(8, new ItemStack(Material.GLASS_PANE));

            ItemStack is = new ItemStack(Material.LAPIS_LAZULI, 12);
            int stacks = supply / 12;
            if(stacks > 7) throw new IllegalArgumentException("Too much supply for station '" + id + "' (" + supply + ")");
            for(int i = 1; i <= stacks; i++) {
                if(i == stacks) is.setAmount(supply % 12);
                inventory.setItem(i, is);
            }
        }

        Block getBlock() {
            return location.getBlock();
        }

        void decrement() {
            if(!isActive()) return;
            //Remove one item
            for(int i = 7; i > 0; i--) {
                ItemStack is = inventory.getItem(i);
                if(is != null && is.getType() == Material.LAPIS_LAZULI) {
                    int amount = is.getAmount()-1;
                    is.setAmount(amount);
                    inventory.setItem(i, is);
                    if(i == 1 && amount == 0) deactivate();
                    break;
                } else if(i == 1) {
                    PCS_Metro.getInstance().getLogger().warning(id + " is empty but still active, indicating an inventory exploit!");
                    deactivate();
                }
            }
        }

        int getSupply() {
            return inventory.all(Material.LAPIS_LAZULI).values().stream().mapToInt(ItemStack::getAmount).sum();
        }

        private int addLapis(int amount) {
            int cost = 0;
            for(int i = 1; i < 8; i++) {
                ItemStack is = inventory.getItem(i);
                int result = Math.min(amount - cost, 12 - (is == null ? 0 : is.getAmount()));
                cost += result;
                ItemStack stack = new ItemStack(Material.LAPIS_LAZULI, (is == null ? 0 : is.getAmount()) + result);
                inventory.setItem(i, stack);
                if(cost == amount) break;
            }

            DatabaseHelper.addSupply(id, cost);
            if(!isActive()) activate();
            return cost;
        }

        @EventHandler(ignoreCancelled = true)
        public void onInteract(PlayerInteractEvent event) {
            if(event.getClickedBlock().equals(getBlock())) {
                event.setCancelled(true);
                event.getPlayer().openInventory(inventory);
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void onDrag(InventoryDragEvent event) {
            if(!event.getInventory().equals(inventory)) return;
            int hashCode = inventory.hashCode();
            for(Integer i : event.getRawSlots()) {
                Inventory inv = event.getView().getInventory(i);
                if(inv.hashCode() == hashCode && inv.equals(inventory)) {
                    event.setCancelled(true);
                    break;
                }
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void onClick(InventoryClickEvent event) {
            Inventory inv = event.getView().getTopInventory();
            if (inv == null || !inv.equals(inventory) || event.getClickedInventory() == null) return;
            event.setCancelled(true);

            final ItemStack currentItem = event.getCurrentItem();

            if(inv == event.getClickedInventory()) {
                if(event.getSlot() == 0 || event.getSlot() == 8) return;

                if(event.getCursor().getType() == Material.LAPIS_LAZULI) {
                    ItemStack cursor = event.getCursor();

                    int amount = cursor.getAmount();
                    if(event.getClick() == ClickType.MIDDLE) amount /= 2;
                    else if(event.getClick() == ClickType.RIGHT) amount = 1;

                    cursor.setAmount(cursor.getAmount() - addLapis(amount));
                    event.getWhoClicked().setItemOnCursor(cursor);
                }
            } else {
                //Own
                if(event.isShiftClick()) {
                    if(currentItem.getType() == Material.LAPIS_LAZULI) {
                        currentItem.setAmount(currentItem.getAmount() - addLapis(currentItem.getAmount()));
                        event.getClickedInventory().setItem(event.getSlot(), currentItem);
                    }
                } else {
                    if(event.getClick() == ClickType.DOUBLE_CLICK) {
                        Bukkit.broadcastMessage(currentItem.getType().name());
                        if(!inventory.contains(event.getCursor().getType())) event.setCancelled(false);
                    } else event.setCancelled(false);
                }
            }
        }
    }
}
