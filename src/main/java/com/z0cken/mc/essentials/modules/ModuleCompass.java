package com.z0cken.mc.essentials.modules;

import com.z0cken.mc.core.bukkit.Menu;
import com.z0cken.mc.core.util.MessageBuilder;
import com.z0cken.mc.essentials.PCS_Essentials;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ModuleCompass extends Module implements Listener {

    private static final HashMap<Player, CompassTarget> targets = new HashMap<>();
    private final Menu MENU = makeMenu();

    public ModuleCompass(String configPath) {
        super(configPath);

        //TODO Make async
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(PCS_Essentials.getInstance(), 20, 20));
    }

    @Override
    protected void load() {

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(event.getMaterial() == Material.COMPASS && event.getPlayer().getWorld().getEnvironment() == World.Environment.NORMAL) {

            if(event.getAction().name().startsWith("L")) {
                //TODO Send distance
                MessageBuilder builder = new MessageBuilder().define("DISTANCE", Integer.toString((int) event.getPlayer().getCompassTarget().distance(event.getPlayer().getLocation())));
                event.getPlayer().spigot().sendMessage(builder.build(config.getString("messages.left-click")));

            } else {
                //Open menu

            }
        }
    }

    private Menu makeMenu() {
        Menu menu = new Menu(PCS_Essentials.getInstance(), config.getInt("menu.rows"));
        ConfigurationSection section = config.getConfigurationSection("targets");
        for(String s : section.getKeys(false)) {
            int  i = Integer.parseInt(s);

            new Menu.Button.ClickEvent() {
               @Override
               public void run(Menu menu1, Player player) {

               }
            };

            new BukkitRunnable() {
                @Override
                public void run() {

                }
            };

            menu.setButton(i, new Menu.Button(new ItemStack(Material.valueOf(config.getString(section + "." + s + ".material")))));

        }

        return menu;
    }

    private void tick() {
        Iterator<Map.Entry<Player, CompassTarget>> iterator = targets.entrySet().iterator();

        while(iterator.hasNext()) {
            Map.Entry<Player, CompassTarget> entry = iterator.next();

            if(!entry.getKey().isOnline()) {
                entry.getValue().disband();
                iterator.remove();
                continue;
            } else if(entry.getValue().getTarget() == null) {
                //TODO Notify of target loss
                iterator.remove();
                entry.getKey().setCompassTarget(entry.getKey().getWorld().getSpawnLocation());
                continue;
            }

            entry.getKey().setCompassTarget(entry.getValue().getTarget());
        }
    }


    class CompassTarget {

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
        }

        public Location getTarget() {
            return target;
        }

        public void disband() {
            task.cancel();
        }
    }


}
