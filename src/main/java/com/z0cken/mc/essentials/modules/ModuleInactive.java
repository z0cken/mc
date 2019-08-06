package com.z0cken.mc.essentials.modules;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.utils.DateUtil;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.wiefferink.areashop.AreaShop;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class ModuleInactive extends Module implements CommandExecutor {

    ModuleInactive(String configPath) {
        super(configPath);
        registerCommand("inactive");
    }

    @Override
    protected void load() {

    }

    @Override
    public boolean onCommand(CommandSender commandsender, Command command, String s, String[] astring) {
        Player p = (Player) commandsender;
        Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");

        Map<ProtectedRegion, Long> lastSeen = new HashMap<>();
        AreaShop.getInstance().getRegionManager(p.getWorld()).getRegions().forEach((n, r) -> {
            Optional<Long> seen = r.getMembers().getUniqueIds().stream().map(uuid -> ess.getUser(uuid).getLastLogin()).max(Comparator.comparingLong(Long::longValue));
            seen.ifPresent(l -> lastSeen.put(r, l));
        });

        LinkedHashMap<ProtectedRegion, Long> map = new LinkedHashMap<>();
        lastSeen.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                .forEachOrdered(x -> map.put(x.getKey(), x.getValue()));

        int i = 0, max = Integer.parseInt(astring[0]);
        for(Map.Entry<ProtectedRegion, Long> entry : map.entrySet()) {
            p.sendMessage(entry.getKey().getId() + " -> " + DateUtil.formatDateDiff(entry.getValue()) + ChatColor.GRAY + " (" + toFriendlyString(entry.getKey().getMembers().getUniqueIds()) + ")");
            if(++i == max) break;
        }
        return false;
    }

    private String toFriendlyString(Set<UUID> uuids) {
        if(uuids == null || uuids.isEmpty()) return "";
        String s = "";
        Iterator<UUID> it = uuids.iterator();

        while (it.hasNext()) {
            OfflinePlayer p = Bukkit.getOfflinePlayer(it.next());
            if(p != null) {
                s += p.getName();
                if(it.hasNext()) s += " ";
            }
        }

        return s;
    }
}
