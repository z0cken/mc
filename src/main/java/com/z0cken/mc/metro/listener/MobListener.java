package com.z0cken.mc.metro.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.z0cken.mc.metro.Metro;
import com.z0cken.mc.metro.PCS_Metro;
import com.z0cken.mc.metro.Station;
import com.z0cken.mc.metro.spawn.SpawnProfile;
import com.z0cken.mc.progression.PCS_Progression;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.regex.Pattern;

public class MobListener implements Listener {

    private static boolean instantiated;

    public MobListener() {
        if(instantiated) throw new IllegalStateException(this.getClass().getName() + " cannot be instantiated twice!");
        instantiated = true;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        if(event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM && Metro.getInstance().contains(event.getLocation())) {
            entity.getScoreboardTags().add("metro");

            String flag = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(event.getLocation())).queryValue(null, PCS_Metro.STRING_FLAG);
            if(flag != null) {
                SpawnProfile profile = Metro.getInstance().getProfile(flag);
                if(profile != null) {
                    if(profile.handleSpawn(entity.getType(), event.getLocation())) event.setCancelled(true);
                } else PCS_Metro.getInstance().getLogger().warning(String.format("Invalid spawn profile '%s' at %s", flag, event.getLocation().toString()));
            } else PCS_Metro.getInstance().getLogger().warning(String.format("Spawn profile missing for %s", event.getLocation().toString()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTarget(EntityTargetEvent event) {
        if(event.getTarget() == null || !(event.getEntity() instanceof Monster)) return;
        Monster monster = (Monster) event.getEntity();

        if(monster.getScoreboardTags().contains("metro")) {
            if(Metro.getInstance().getStations().stream().filter(Station::isActive).anyMatch(s -> s.contains(event.getTarget().getLocation()))) event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onKill(EntityDamageByEntityEvent event) {
        final Entity entity = event.getEntity();

        if(!(entity instanceof Monster) || !(event.getDamager() instanceof Player)) return;
        if(!entity.getScoreboardTags().contains("metro")) return;

        if(event.getFinalDamage() > ((LivingEntity) entity).getHealth()) {
            final Player player = (Player) event.getDamager();
            PCS_Progression.progress(player, "metro_kills", 1);
            for(String s : entity.getScoreboardTags()) {
                if(s.startsWith("metro-xp")) {
                    final int xp = Integer.parseInt(s.split(Pattern.quote(":"))[1]);
                    PCS_Progression.progress(player, "metro-xp", xp);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, PCS_Metro.getInstance().getMessageBuilder().define("AMOUNT", Integer.toString(xp)).build(PCS_Metro.getInstance().getConfig().getString("messages.xp-actionbar")));
                    break;
                }
            }
        }

    }

    /*private Difficulty getDifficulty(Location location) {
        Difficulty difficulty = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().queryValue(BukkitAdapter.adapt(location), null, PCS_Metro.DIFFICULTY_FLAG);
        if(difficulty == null) return Difficulty.NORMAL;
        return difficulty;
    }*/
}