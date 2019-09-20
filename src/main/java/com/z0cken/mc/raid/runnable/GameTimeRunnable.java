package com.z0cken.mc.raid.runnable;

import com.z0cken.mc.raid.GameState;
import com.z0cken.mc.raid.PCS_Raid;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;

public class GameTimeRunnable extends BukkitRunnable {
    private Duration duration, remaining;

    private BossBar bossBar = PCS_Raid.getRaid().getBossBar();

    public GameTimeRunnable(Duration duration) {
        this.duration = duration;
        remaining = this.duration;
    }

    @Override
    public void run() {
        if(PCS_Raid.getRaid().getState() == GameState.PAUSED) return;

        if(remaining.toSeconds() == 60) {
            bossBar.setColor(BarColor.RED);
        }

        if(remaining.toSeconds() < 10) {
            Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1.1F));
        }

        if(remaining.toSeconds() == 0) {
            PCS_Raid.getRaid().stop(false);
            //TODO Round end music
            this.cancel();
            return;
        }

        bossBar.setTitle(DurationFormatUtils.formatDuration(remaining.toMillis(), "mm:ss") + ChatColor.GRAY + " | Aliens gerettet: " + PCS_Raid.getRaid().getAliensCaptured());
        bossBar.setProgress((double) remaining.toSeconds() / Math.max((double) duration.toSeconds(), 1));
        remaining = remaining.minusSeconds(1);
    }
}
