package com.z0cken.mc.end.phase;

import com.z0cken.mc.core.persona.PersonaAPI;
import com.z0cken.mc.core.util.MessageBuilder;
import com.z0cken.mc.end.End;
import com.z0cken.mc.end.PCS_End;
import com.z0cken.mc.progression.PCS_Progression;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CombatPhase extends EndPhase implements Listener {

    private Map<Player, Integer> kills = new HashMap<>();

    CombatPhase(End end) {
        super(PhaseType.COMBAT, end);
    }

    @Override
    public void start() {
        getEnd().getWorld().setPVP(true);
        getEnd().getMainPlayers().forEach(p -> p.teleport(getEnd().getWorld().getSpawnLocation()));

        Bukkit.getPluginManager().registerEvents(this, PCS_End.getInstance());
    }

    @Override
    public void stop() {
        super.stop();
        getEnd().getWorld().setPVP(false);
        HandlerList.unregisterAll(this);

        kills.forEach((p, k) -> PCS_Progression.progress(p, "end_kills", k));

        BaseComponent[] phaseMsg = PCS_End.getInstance().getMessageBuilder().define("VALUE", "Schlachtphase").build(PCS_End.getInstance().getConfig().getString("messages.phase-end"));
        Bukkit.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(phaseMsg));

        final Map.Entry<Player, Integer> topKiller = getTopKiller();
        if(topKiller != null) PersonaAPI.getPersona(topKiller.getKey().getUniqueId()).getComponent(topKiller.getKey().getName(), 3, TimeUnit.SECONDS).thenAccept(component -> {
            BaseComponent[] msg = MessageBuilder.DEFAULT.define("PLAYER", component).define("VALUE", topKiller.getValue().toString()).build(PCS_End.getInstance().getConfig().getString("messages.top-killer"));
            Bukkit.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(msg));
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if(killer != null) kills.put(killer, kills.getOrDefault(killer, 0) + 1);
    }

    private Map.Entry<Player, Integer> getTopKiller() {
        if(kills.isEmpty()) return null;
        return kills.entrySet().stream().max(Map.Entry.comparingByValue()).get();
    }
}
