package com.z0cken.mc.capture;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class FlagReturnRunnable extends BukkitRunnable {

    private final CaptureTeam team;
    private final Item item;
    private final BossBar bossBar;
    private final NamespacedKey key = new NamespacedKey(PCS_Capture.getInstance(), UUID.randomUUID().toString());
    private int returnTime = PCS_Capture.getInstance().getConfig().getInt("return-time");

    FlagReturnRunnable(CaptureTeam team, Item item, BarColor color) {
        this.team = team;
        this.item = item;
        this.bossBar = Bukkit.createBossBar(key, team.getDisplayName() + "e Flagge", color, BarStyle.SOLID);
        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);
    }

    @Override
    public void run() {
        if(!item.isValid()) {
            bossBar.removeAll();
            Bukkit.removeBossBar(key);
            this.cancel();
        }

        bossBar.setProgress(returnTime / (double) PCS_Capture.getInstance().getConfig().getInt("return-time"));

        if(returnTime-- == 0) {
            team.getPlayers().forEach(p -> p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 1));
            team.getFlag().reset();
            bossBar.removeAll();
            Bukkit.removeBossBar(key);
            this.cancel();
        }
    }
}
