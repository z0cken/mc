package com.z0cken.mc.essentials.modules;

import com.z0cken.mc.claim.Claim;
import com.z0cken.mc.claim.PCS_Claim;
import com.z0cken.mc.core.FriendsAPI;
import com.z0cken.mc.core.bukkit.Menu;
import com.z0cken.mc.core.util.MessageBuilder;
import com.z0cken.mc.essentials.PCS_Essentials;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class ModuleCompass extends Module implements Listener {

    private Set<Player> deadPlayers;
    private static final Map<Player, CompassTarget> targets = new HashMap<>();

    private boolean keepOnDeath;

    public ModuleCompass(String configPath) {
        super(configPath);

        //TODO Make async and configurable
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(PCS_Essentials.getInstance(), 20, 20));
    }

    @Override
    protected void load() {
        keepOnDeath = getConfig().getBoolean("keep-on-death");
        if(keepOnDeath) deadPlayers = new HashSet<>();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if(keepOnDeath && event.getEntity().getInventory().contains(Material.COMPASS)) deadPlayers.add(event.getEntity());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if(keepOnDeath && deadPlayers.remove(event.getPlayer())) event.getPlayer().getInventory().addItem(new ItemStack(Material.COMPASS));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        targets.remove(event.getPlayer());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        if(event.getMaterial() == Material.COMPASS && player.getWorld().getEnvironment() == World.Environment.NORMAL) {
            final CompassMeta compassMeta = (CompassMeta) event.getItem().getItemMeta();
            if(compassMeta.isLodestoneTracked()) return;

            if(event.getAction().name().startsWith("L")) {
                MessageBuilder builder = new MessageBuilder().define("DISTANCE", Integer.toString((int) player.getCompassTarget().distance(player.getLocation())));
                player.spigot().sendMessage(builder.build(getConfig().getString("messages.left-click")));

            } else {
                ConfigurationSection worldSection = getConfig().getConfigurationSection(player.getWorld().getName());
                if(worldSection == null) return;

                final CompassMenu menu = new CompassMenu(worldSection);
                if(Arrays.stream(menu.getContents()).anyMatch(Objects::nonNull)) player.openInventory(menu);
            }
        }
    }

    private void tick() {
        Iterator<Map.Entry<Player, CompassTarget>> iterator = targets.entrySet().iterator();

        while(iterator.hasNext()) {
            Map.Entry<Player, CompassTarget> entry = iterator.next();
            final Location loc = entry.getValue().getLocation();
            final Player player = entry.getKey();

            if(!player.isOnline()) {
                entry.getValue().disband();
                iterator.remove();
                continue;
            } else if(loc == null || !loc.getWorld().equals(player.getWorld())) {
                player.spigot().sendMessage(new MessageBuilder().build(getConfig().getString("messages.target-lost")));
                entry.getValue().disband();
                iterator.remove();
                player.setCompassTarget(player.getWorld().getSpawnLocation());
                continue;
            }

            player.setCompassTarget(loc);
        }
    }

    private static final Menu.Button PREVIOUS_PAGE = new Menu.Button((menu, button, player, event) -> {
        menu.showPage(menu.getCurrentPage() - 1);
    }, Material.ARROW);

    private static final Menu.Button NEXT_PAGE = new Menu.Button((menu, button, player, event) -> {
        menu.showPage(menu.getCurrentPage() + 1);
    }, Material.ARROW);

    private static final Menu.Button BACK = new Menu.Button((menu, button, player, event) -> {
        player.closeInventory();
    }, Material.BOOK);

    private final Menu.Button.ClickEvent FRIENDS_EVENT = (menu, button, player, event) -> {
        final Set<Player> friends;
        try {
            friends = FriendsAPI.getFriends(player.getUniqueId()).keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).filter(Player::isOnline).collect(Collectors.toSet());
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        if(friends.size() == 0) return;

        final int rows = calculateTotalRows(friends.size());
        final boolean multiPage = rows > 6;
        final int contentSlots = (multiPage ? 5 : rows) * 9;

        final Menu friendMenu = new Menu(PCS_Claim.getInstance(), multiPage ? rows + 1 : rows, getConfig().getString("titles.friends"), menu);

        int i = 0;
        for(Player friend : friends) {
            final int page = i / contentSlots;
            final int slot = (i % contentSlots);

            Menu.Button friendButton = new Menu.Button((m, b, p, e) -> {
                final MessageBuilder messageBuilder = MessageBuilder.DEFAULT.define("TARGET", friend.getName());

                if(p.getWorld().equals(friend.getWorld())){
                    setTarget(p, friend);
                    p.spigot().sendMessage(messageBuilder.build(getConfig().getString("messages.selected")));
                } else p.spigot().sendMessage(messageBuilder.build(getConfig().getString("messages.other-world")));

            }, Material.PLAYER_HEAD);

            SkullMeta itemMeta = (SkullMeta) friendButton.getItemMeta();
            itemMeta.setDisplayName(ChatColor.RESET + friend.getName());
            itemMeta.setOwningPlayer(friend);
            friendButton.setItemMeta(itemMeta);

            friendMenu.setItem(page, slot, friendButton);
            i++;
        }

        if(multiPage) addNavigation(friendMenu);

        player.openInventory(friendMenu);
    };

    private final Menu.Button.ClickEvent CLAIMS_EVENT = (menu, button, player, event) -> {
        if(!Bukkit.getPluginManager().isPluginEnabled("PCS_Claim")) return;

        final Set<Claim> claims = PCS_Claim.Unsafe.getClaims(player.getWorld(), player);
        if(claims.size() == 0) return;

        final int rows = calculateTotalRows(claims.size());
        final boolean multiPage = rows > 6;
        final int contentSlots = (multiPage ? 5 : rows) * 9;

        final Menu chunkMenu = new Menu(PCS_Claim.getInstance(), multiPage ? 6 : rows, getConfig().getString("titles.claims"), menu);

        int i = 0;
        for(Claim claim : claims) {
            final int page = i / contentSlots;
            final int slot = (i % contentSlots);

            Material base = claim.getBaseMaterial();

            Menu.Button chunkButton = new Menu.Button((m, b, p, e) -> {
                final MessageBuilder messageBuilder = MessageBuilder.DEFAULT.define("TARGET", claim.getName());

                if(p.getWorld().equals(claim.getWorld())) {
                    setTarget(p, new Location(claim.getWorld(), claim.getBaseLocation().getX(), claim.getBaseLocation().getY(), claim.getBaseLocation().getBlockZ()));
                    player.spigot().sendMessage(messageBuilder.build(getConfig().getString("messages.selected")));
                } else p.spigot().sendMessage(messageBuilder.build(getConfig().getString("messages.other-world")));
            }, base.isSolid() ? base : Material.END_PORTAL_FRAME);

            ItemMeta itemMeta = chunkButton.getItemMeta();
            itemMeta.setDisplayName(claim.getChunkCoordinate().getX()+"|"+claim.getChunkCoordinate().getZ());
            //TODO Add claim date to lore
            chunkButton.setItemMeta(itemMeta);

            chunkMenu.setItem(page, slot, chunkButton);
            i++;
        }

        if(multiPage) addNavigation(chunkMenu);

        player.openInventory(chunkMenu);
    };

    private final List<Menu.Button.ClickEvent> PRESETS = List.of(FRIENDS_EVENT, CLAIMS_EVENT);

    private static void setTarget(@Nonnull Player player, @Nonnull Entity entity) {
        targets.put(player, new CompassTarget(entity));
        player.setCompassTarget(entity.getLocation());
    }

    private static void setTarget(@Nonnull Player player, @Nonnull Location location) {
        targets.put(player, new CompassTarget(location));
        player.setCompassTarget(location);
    }

    private static void addNavigation(Menu menu) {
        int rows = 5;
        for(int j = 0; j < menu.getPageCount(); j++) {
            menu.setItem(j, rows * 9 + 4, BACK);
            if(j > 0) menu.setItem(j, rows * 9 + 3, PREVIOUS_PAGE);
            if(j < menu.getPageCount() - 1) menu.setItem(j, rows * 9 + 5, NEXT_PAGE);
        }
    }


    private static int calculateTotalRows(int items) {
        int rows = items / 9;
        if(items % 9 != 0) rows++;
        return rows;
    }

    class CompassMenu extends Menu {

        public CompassMenu(ConfigurationSection section) {
            super(PCS_Essentials.getInstance(), section.getInt("rows"), getConfig().getString("titles.claims"));

            populate(section);
        }

        private void populate(ConfigurationSection section) {
            section = section.getConfigurationSection("targets");

            for(String s : section.getKeys(false)) {
                Button button;
                Material material = Material.valueOf(section.getString(s + ".material"));

                if(section.contains(s + ".preset")) {
                    button = new Button(PRESETS.get(section.getInt(s + ".preset")), material);

                } else if(section.contains(s + ".location")) {
                    final CompassTarget target = new CompassTarget(section.getSerializable(s + ".location", Location.class));

                    Menu.Button.ClickEvent clickEvent = (menu, button1, player, event) -> {
                        player.setCompassTarget(target.getLocation());

                        CompassTarget oldTarget = targets.getOrDefault(player, null);
                        if(oldTarget != null) oldTarget.disband();
                        targets.put(player, target);

                        player.spigot().sendMessage((new MessageBuilder()).define("TARGET", button1.getItemMeta().getDisplayName()).build(getConfig().getString("messages.selected")));
                    };

                    button = new Button(clickEvent, material);

                } else {
                    return;
                }

                ItemMeta itemMeta = button.getItemMeta();
                itemMeta.setDisplayName(section.getString(s + ".title"));
                itemMeta.setLore(section.getStringList(s + ".lore"));
                button.setItemMeta(itemMeta);

                setItem(Integer.parseInt(s), button);
            }
        }
    }

    static class CompassTarget {

        private Location target;
        private BukkitTask task;

        CompassTarget(Location location) {
            target = location;
        }

        CompassTarget(Entity entity) {
            target = entity.getLocation();

            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if(!entity.isValid()) {
                        target = null;
                        return;
                    }
                    target = entity.getLocation();
                }
            }.runTaskTimer(PCS_Essentials.getInstance(), 40, 40);
            //tasks.add(task);
        }

        public Location getLocation() {
            return target;
        }

        public void disband() {
            if(task != null) task.cancel();
        }
    }
}
