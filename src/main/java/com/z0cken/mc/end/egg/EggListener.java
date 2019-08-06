package com.z0cken.mc.end.egg;

import com.z0cken.mc.end.HiddenStringUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class EggListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();

        if(block.getType() == Material.DRAGON_EGG) {
            MagicEggType type;

            try {
                type = MagicEggType.valueOf(HiddenStringUtils.extractHiddenString(event.getItemInHand().getItemMeta().getLore().get(0)));
            } catch (IllegalArgumentException | NullPointerException e) {
                return;
            }

            type.spawn(event.getPlayer(), block);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if((event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock().getType() == Material.DRAGON_EGG) event.setUseInteractedBlock(Event.Result.DENY);
    }
}
