package com.z0cken.mc.end;

import net.md_5.bungee.api.chat.BaseComponent;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class DragonRespawnRunnable extends BukkitRunnable implements Listener {

    private static final String TITLE = PCS_End.getInstance().getConfig().getString("bar-title");
    private static final NamespacedKey KEY = new NamespacedKey(PCS_End.getInstance(), "respawn");

    private final World world;
    private final BossBar bossBar;
    private final Runnable onFinish;

    private final long respawnTime;
    private long remainingTime;

    public DragonRespawnRunnable(World world, long respawnTime, Runnable onFinish) {
        Bukkit.getPluginManager().registerEvents(this, PCS_End.getInstance());

        this.world = world;
        this.respawnTime = respawnTime;
        this.remainingTime = respawnTime;
        this.onFinish = onFinish;

        bossBar = Bukkit.createBossBar(KEY, makeTitle(), BarColor.PURPLE, BarStyle.SOLID);
        bossBar.setProgress(1);
        world.getPlayers().forEach(bossBar::addPlayer);
    }

    @Override
    public void run() {
        remainingTime--;
        bossBar.setTitle(makeTitle());
        bossBar.setProgress(((double) remainingTime) / respawnTime);

        if(remainingTime == PCS_End.getInstance().getConfig().getInt("announce-respawn")) {
            BaseComponent[] msg = PCS_End.getInstance().getMessageBuilder().build(PCS_End.getInstance().getConfig().getString("messages.announce-respawn"));
            Bukkit.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(msg));
        }

        if(remainingTime == 0) {
            onFinish.run();
            cancel();
        }
    }

    @Override
    public synchronized void cancel() {
        HandlerList.unregisterAll(this);
        bossBar.removeAll();
        Bukkit.removeBossBar(KEY);
        Bukkit.getScheduler().cancelTask(this.getTaskId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(event.getPlayer().getWorld().equals(world)) bossBar.addPlayer(event.getPlayer());
    }

    //TODO Persists after quit?

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if(event.getPlayer().getWorld().equals(world)) bossBar.addPlayer(event.getPlayer());
        else if(event.getFrom().equals(world)) bossBar.removePlayer(event.getPlayer());
    }

    private String makeTitle() {
        return String.format(TITLE, DurationFormatUtils.formatDuration(remainingTime * 1000, remainingTime > 60 * 60 ? "HH:mm" : "mm:ss"));
    }

    public String getFormattedDuration(String format) {
        return DurationFormatUtils.formatDuration(remainingTime * 1000, format);
    }
}
