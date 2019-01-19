package com.z0cken.mc.claim;

import com.z0cken.mc.util.MessageBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

class ProtectionListener implements Listener {


    @EventHandler
    public static void onBlockPlace(BlockPlaceEvent event) {
        final Chunk chunk = event.getBlockPlaced().getChunk();
        final Player player = event.getPlayer();

        OfflinePlayer owner = PCS_Claim.getOwner(chunk);
        if(owner == null) return;

        boolean claim = event.getBlockPlaced().getType() == Material.END_PORTAL_FRAME;

        BaseComponent[] message = null;
        MessageBuilder builder = new MessageBuilder();

        if(PCS_Claim.canBuild(player, chunk)) {
            if(claim) {
                event.setCancelled(true);
                if(player.equals(owner)) message = builder.build(PCS_Claim.getInstance().getConfig().getString("messages.denied-self"));
                else message = builder.build(PCS_Claim.getInstance().getConfig().getString("messages.denied-friend"));
            }
        } else {
            event.setCancelled(true);
            message = builder.build(PCS_Claim.getInstance().getConfig().getString("messages.protected"));
        }
        if(message != null) player.spigot().sendMessage(message);
    }

}
