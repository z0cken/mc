package com.z0cken.mc.capture;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Score;

public class GameTimeRunnable extends BukkitRunnable {

    short duration, current;
    private ChatColor color = ChatColor.WHITE;

    public GameTimeRunnable(int duration) {
        this.duration = (short) duration;
        current = this.duration;
    }

    @Override
    public void run() {
        if(Arena.isPaused()) return;

        ScoreboardManager.setText(4, ChatColor.GRAY.toString() + ChatColor.BOLD + "- " + (((current % 3600) / 60) + ":" + ((current % 60) < 10 ? "0" : "") + current % 60) + ChatColor.GRAY.toString() + ChatColor.BOLD + " - ");

        if(current == 60) color = ChatColor.RED;

        if(current <= 60 && current != 0 && current % 5 == 0)  {
            Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1.1F));
        }

        if(current == 0) {
            if(Arena.endGame()) this.cancel();
            return;
        }

        current--;
    }

}
