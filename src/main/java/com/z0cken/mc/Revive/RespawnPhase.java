package com.z0cken.mc.Revive;

import com.z0cken.mc.Revive.utils.PlayerUtils;
import com.z0cken.mc.core.util.MessageBuilder;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

public class RespawnPhase {

    private final Player player;

    private final BossBar personalBossBar;
    private final BossBar externalBossBar;
    private final ItemStack[] inventoryContents;
    private final ItemStack[] armorContents;
    private final Location respawnLocation;
    private final long deathTimeStamp;
    private final int expToDrop;

    private ArmorStand armorStand;
    private NPC npc;

    private BukkitRunnable respawnRunnable;

    public RespawnPhase(Player player, int expToDrop) {
        this.player = player;
        this.inventoryContents = this.player.getInventory().getContents();
        this.armorContents = this.player.getInventory().getArmorContents();
        this.respawnLocation = this.player.getLocation();
        this.expToDrop = expToDrop;
        this.deathTimeStamp = System.currentTimeMillis();

        this.personalBossBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);
        this.externalBossBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);

        this.personalBossBar.addPlayer(player);
    }

    public void startRespawnPhase() {
        PlayerUtils.clearPlayerInvAll(this.player);

        //Make sure player is on ground
        Location teleportLocation = this.player.getLocation();
        while (!teleportLocation.getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
            teleportLocation = teleportLocation.getBlock().getRelative(BlockFace.DOWN).getLocation();
        }

        if(!teleportLocation.equals(this.player.getLocation())) {
            this.player.teleport(teleportLocation);
        }

        this.npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, player.getName());
        this.npc.spawn(player.getLocation());

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    sendBedPacket(player);
                }
            }
        }.runTaskLater(Revive.getPlugin(), 20L);

        //Try again with more delay because sometimes it bugs when skin loads in
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    try {
                        Revive.getPlugin().getNmsBridge().sendHumanNPCBed(player, npc);
                        Revive.getPlugin().getNmsBridge().sendBedRelMove(player, npc);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }.runTaskLater(Revive.getPlugin(), 40L);


        this.player.spigot().sendMessage(new MessageBuilder().define("hier", new ClickEvent(ClickEvent.Action.RUN_COMMAND, "respawn")).build(Revive.getPlugin().getConfig().getString("respawn-command")));

        this.player.setGameMode(GameMode.SPECTATOR);

        //Spawn entity
        Location location = this.player.getLocation().subtract(0, 1, 0);
        location.setPitch(-90);
        this.armorStand = (ArmorStand) this.player.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        this.armorStand.setGravity(false);
        this.armorStand.setVisible(false);
        this.armorStand.setCustomName(this.player.getName());
        this.armorStand.setCustomNameVisible(false);

        this.respawnRunnable = new BukkitRunnable() {
            public void run() {
                updateBossBar();

                if (player.getGameMode() == GameMode.SPECTATOR) {
                    player.setSpectatorTarget(armorStand);
                }

                if (getTimeTillRespawnForced() <= 0) {
                    //Task is cancelled in the endRespawnPhase method
                    respawnPlayer();
                }
            }
        };
        this.respawnRunnable.runTaskTimer(Revive.getPlugin(), 0L, 20L);
    }

    public void revivePlayer() {
        this.player.getInventory().setContents(this.inventoryContents);
        this.player.getInventory().setArmorContents(this.armorContents);

        this.player.getWorld().playSound(this.player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

        endRespawnPhase();
    }

    public void respawnPlayer() {
        for (ItemStack itemStack : this.inventoryContents) {
            if (itemStack != null) {
                this.player.getWorld().dropItemNaturally(this.player.getLocation(), itemStack);
            }
        }

        for (ItemStack itemStack : this.armorContents) {
            if (itemStack != null) {
                this.player.getWorld().dropItemNaturally(this.player.getLocation(), itemStack);
            }
        }

        //Drop experience
        if (this.expToDrop > 0) {
            this.player.getWorld().spawn(this.player.getLocation(), ExperienceOrb.class).setExperience(this.expToDrop);
            this.player.setTotalExperience(this.player.getTotalExperience() - this.expToDrop);
        }

        endRespawnPhase();
        this.player.teleport(this.player.getWorld().getSpawnLocation());
    }

    private void endRespawnPhase() {
        RespawnHandler.getHandler().removeRespawn(this.player);
        this.player.setGameMode(GameMode.SURVIVAL);

        this.armorStand.remove();

        this.respawnRunnable.cancel();

        this.personalBossBar.removePlayer(player);

        for (Player player : this.externalBossBar.getPlayers()) {
            this.externalBossBar.removePlayer(player);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            Revive.getPlugin().getNmsBridge().sendBedBlockChange(player, this.npc, true);
        }

        this.npc.destroy();
    }

    private void updateBossBar() {
        int timeTillForce = getTimeTillRespawnForced();

        //Different title for the dead player than external players
        this.personalBossBar.setTitle("Du stirbst automatisch in " + timeTillForce + " Sekunden...");
        this.externalBossBar.setTitle(this.player.getName() + " stirbt in " + timeTillForce + " Sekunden...");

        BarColor barColor = BarColor.GREEN;

        if (timeTillForce < RespawnHandler.SECONDS_TILL_RESPAWN_FORCE * 0.25) {
            barColor = BarColor.RED;
        } else if (timeTillForce < RespawnHandler.SECONDS_TILL_RESPAWN_FORCE * 0.5) {
            barColor = BarColor.YELLOW;
        }

        if (this.externalBossBar.getColor() != barColor) {
            this.externalBossBar.setColor(barColor);

            //We only update both bars at the same time which means if external has the wrong colour the personal has the wrong colour too
            this.personalBossBar.setColor(barColor);
        }
    }

    public void tryShowBossBarTo(Player reviver) {
        //The player at least needs one totem of undying
        if (!reviver.getInventory().contains(Material.TOTEM_OF_UNDYING, 1)) {
            //If the player sees the boss bar we need to hide it
            if (this.externalBossBar.getPlayers().contains(reviver)) {
                hideBossBarFrom(reviver);
            }

            return;
        }

        this.externalBossBar.addPlayer(reviver);
    }

    public void hideBossBarFrom(Player reviver) {
        this.externalBossBar.removePlayer(reviver);
    }

    public void sendBedPacket(Player player) {
        try {
            Revive.getPlugin().getNmsBridge().sendBedBlockChange(player, this.npc, false);
            Revive.getPlugin().getNmsBridge().sendHumanNPCBed(player, this.npc);
            Revive.getPlugin().getNmsBridge().sendBedRelMove(player, this.npc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public int getTimeTill(int time) {
        return (int) Math.ceil(time - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - this.deathTimeStamp));
    }

    public int getTimeTillRespawnForced() {
        return getTimeTill(RespawnHandler.SECONDS_TILL_RESPAWN_FORCE);
    }

    public int getTimeTillRespawnAllowed() {
        return getTimeTill(RespawnHandler.SECONDS_TILL_RESPAWN_AVAILABLE);
    }

    public boolean isRespawnAllowed() {
        return System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(RespawnHandler.SECONDS_TILL_RESPAWN_AVAILABLE) >= this.deathTimeStamp;
    }

    public Player getPlayer() {
        return this.player;
    }

    public ItemStack[] getInventoryContents() {
        return this.inventoryContents;
    }

    public ItemStack[] getArmorContents() {
        return this.armorContents;
    }

    public long getDeathTimeStamp() {
        return this.deathTimeStamp;
    }

    public BossBar getExternalBossBar() {
        return this.externalBossBar;
    }

    public NPC getNPC() {
        return npc;
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }
}
