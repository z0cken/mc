package com.z0cken.mc.essentials.modules;

import com.z0cken.mc.essentials.PCS_Essentials;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Date;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;

public class ModuleRestart extends Module {

    private static final LocalTime time = LocalTime.of(3, 0);

    private static String sound;
    private static int warnTime, soundDuration; //In minutes
    private static Timer timer;

    ModuleRestart(String configPath) {
        super(configPath);
    }

    @Override
    protected void load() {
        sound = getConfig().getString("sound");
        soundDuration = getConfig().getInt("sound-duration");
        warnTime = getConfig().getInt("warn-time");
        startTimer();
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        if(timer != null) timer.cancel();
    }

    private void startTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final String warning = getConfig().getString("messages.warning");
                if(warning != null) Bukkit.broadcastMessage(String.format(warning, warnTime));

                tasks.add(new BukkitRunnable() {
                    @Override
                    public void run() {
                        restart();
                    }
                }.runTaskLater(PCS_Essentials.getInstance(), warnTime * 60 * 20));
            }
        }, getNextDate(), Duration.ofDays(1).toMillis());
    }

    private void restart() {
        for(Player player : Bukkit.getOnlinePlayers()) player.playSound(player.getLocation(), sound, 1, 1);

        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getServer().shutdown();
            }
        }.runTaskLater(PCS_Essentials.getInstance(), soundDuration * 20));
    }

    private static Date getNextDate() {
        LocalDate localDate = LocalDate.from(time.atDate(LocalDate.now()).minusMinutes(warnTime));
        if(LocalTime.now().isAfter(time)) localDate = localDate.plusDays(1);
        return Date.valueOf(localDate);
    }
}
