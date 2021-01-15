package com.z0cken.mc.metro;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.z0cken.mc.metro.event.StationActivateEvent;
import com.z0cken.mc.metro.event.StationDeactivateEvent;
import com.z0cken.mc.progression.PCS_Progression;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Station implements Listener {

    private final Metro metro;
    private final String id, name;
    private final MetroBeacon beacon;
    private final ProtectedRegion region;
    private boolean active = false;

    //private final Difficulty difficulty;

    Station(Metro metro, String id, String name, Location beaconLocation) {
        this.metro = metro;
        this.id = id;
        this.name = name;

        int supply = DatabaseHelper.getSupply(id);
        this.beacon = new MetroBeacon(beaconLocation, supply);

        this.region = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(beaconLocation.getWorld())).getRegion(id);
        //this.difficulty = region.getFlag(PCS_Metro.DIFFICULTY_FLAG);

        if(supply > 0) {
            active = true;
            beaconLocation.getBlock().getRelative(BlockFace.DOWN).setType(Material.DIAMOND_BLOCK);
        } else beaconLocation.getBlock().getRelative(BlockFace.DOWN).setType(Material.BEDROCK);
    }

    public boolean contains(Location location) {
        if(!metro.getWorld().equals(location.getWorld())) return false;
        return region.contains((int) location.getX(), (int) location.getY(), (int) location.getZ());
    }

    public List<Player> getPlayers() {
        return Bukkit.getOnlinePlayers().stream().filter(p -> p.getWorld().equals(metro.getWorld())).filter(p -> contains(p.getLocation())).collect(Collectors.toList());
    }

    private void activate() {
        Metro.getInstance().getAppropriateEffect().deactivate();
        active = true;
        Metro.getInstance().getAppropriateEffect().activate();

        beacon.getBlock().getRelative(BlockFace.DOWN).setType(Material.DIAMOND_BLOCK);

        Bukkit.getPluginManager().callEvent(new StationActivateEvent(Metro.getInstance(), this, Collections.unmodifiableList(getPlayers())));
    }

    private void deactivate() {
        Metro.getInstance().getAppropriateEffect().deactivate();
        active = false;
        Metro.getInstance().getAppropriateEffect().activate();

        beacon.getBlock().getRelative(BlockFace.DOWN).setType(Material.BEDROCK);

        Bukkit.getPluginManager().callEvent(new StationDeactivateEvent(Metro.getInstance(), this));
    }

    public static Station fromConfig(Metro metro, ConfigurationSection section, String key) {
        section = section.getConfigurationSection(key);
        String name = section.getString("name");
        Location beacon = section.getSerializable("beacon", Location.class);
        final Station station = new Station(metro, key, name, beacon);
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

    /*public Difficulty getDifficulty() {
        return difficulty == null ? Difficulty.NORMAL : difficulty;
    }*/

    class MetroBeacon implements Listener {
        private final Location location;
        private final Inventory inventory = Bukkit.createInventory(null, 9, getName());

        MetroBeacon(Location location, int supply) {
            this.location = location;
            location.getBlock().setType(Material.BEACON);

            inventory.setItem(0, new ItemStack(Material.GLASS_PANE));
            inventory.setItem(8, new ItemStack(Material.GLASS_PANE));

            final int stackSize = metro.getStackSize();
            final ItemStack is = new ItemStack(Material.LAPIS_LAZULI, stackSize);
            final int stacks = supply / stackSize;
            if(stacks > 7) PCS_Metro.getInstance().getLogger().warning("Too much supply for station '" + id + "' (" + supply + ")");
            if(stacks == 0) inventory.setItem(1, new ItemStack(Material.LAPIS_LAZULI, supply));
            else for(int i = 0; i <= stacks && i < 7; i++) {
                if(i == stacks && supply % stackSize > 0) is.setAmount(supply % stackSize);
                inventory.setItem(i+1, is);
            }
        }

        Block getBlock() {
            return location.getBlock();
        }

        int getSupply() {
            return inventory.all(Material.LAPIS_LAZULI).values().stream().mapToInt(ItemStack::getAmount).sum();
        }

        void drain() {
            int amount = metro.getRate();
            if(getSupply() <= amount) deactivate();
            else for(int i = 7; i > 0; i--) {
                if(amount == 0) break;

                ItemStack is = inventory.getItem(i);
                if(is == null) continue;
                if(is.getAmount() > amount) {
                    is.setAmount(is.getAmount() - amount);
                    inventory.setItem(i, is);
                    break;
                } else {
                    is.setType(Material.AIR);
                    inventory.setItem(i, is);
                    amount -= is.getAmount();
                }
            }
            DatabaseHelper.setSupply(id, getSupply());
        }

        private int addLapis(int given) {
            int cost = 0;
            for(int i = 1; i < 8; i++) {
                final ItemStack is = inventory.getItem(i);
                final int present = is == null ? 0 : is.getAmount();
                final int added = Math.min(given - cost, metro.getStackSize() - present);

                final ItemStack stack = new ItemStack(Material.LAPIS_LAZULI, present + added);
                inventory.setItem(i, stack);

                cost += added;
                if(cost == given) break;
            }

            final int supply = getSupply();
            DatabaseHelper.setSupply(id, supply);
            if(!isActive() && supply >= Metro.getInstance().getRate()) {
                activate();
                drain();
            }
            return cost;
        }

        @EventHandler(ignoreCancelled = true)
        public void onInteract(PlayerInteractEvent event) {
            if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().equals(getBlock())) {
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

            //TODO Duplicate code
            if(inv == event.getClickedInventory()) {
                if(event.getSlot() == 0 || event.getSlot() == 8) return;

                if(event.getCursor().getType() == Material.LAPIS_LAZULI) {
                    ItemStack cursor = event.getCursor();

                    int amount = cursor.getAmount();
                    if(event.getClick() == ClickType.MIDDLE) amount /= 2;
                    else if(event.getClick() == ClickType.RIGHT) amount = 1;

                    final int cost = addLapis(amount);
                    cursor.setAmount(cursor.getAmount() - cost);
                    event.getWhoClicked().setItemOnCursor(cursor);
                    PCS_Progression.progress((Player) event.getWhoClicked(),"metro_lapis", cost);
                    final int xp = PCS_Metro.getInstance().getConfig().getInt("experience.per-lapis") * cost;
                    getPlayers().forEach(p -> {
                        p.spigot().sendMessage(PCS_Metro.getInstance().getMessageBuilder().define("AMOUNT", Integer.toString(xp)).define("REASON", "den Nachschub").build(PCS_Metro.getInstance().getConfig().getString("messages.experience")));
                        PCS_Progression.progress(p, "metro_xp", xp);
                    });
                }
            } else {
                //Own
                if(event.isShiftClick()) {
                    if(currentItem != null && currentItem.getType() == Material.LAPIS_LAZULI) {
                        final int cost = addLapis(currentItem.getAmount());
                        currentItem.setAmount(currentItem.getAmount() - cost);
                        event.getClickedInventory().setItem(event.getSlot(), currentItem);
                        PCS_Progression.progress((Player) event.getWhoClicked(),"metro_lapis", cost);

                        final int xp = PCS_Metro.getInstance().getConfig().getInt("experience.per-lapis") * cost;
                        getPlayers().forEach(p -> {
                            p.spigot().sendMessage(PCS_Metro.getInstance().getMessageBuilder().define("AMOUNT", Integer.toString(xp)).define("REASON", "den Nachschub").build(PCS_Metro.getInstance().getConfig().getString("messages.experience")));
                            PCS_Progression.progress(p, "metro_xp", xp);
                        });
                    }
                } else {
                    if(event.getClick() == ClickType.DOUBLE_CLICK) {
                        if(!inventory.contains(event.getCursor().getType())) event.setCancelled(false);
                    } else event.setCancelled(false);
                }
            }
        }
    }
}
