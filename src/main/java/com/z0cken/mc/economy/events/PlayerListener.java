package com.z0cken.mc.economy.events;

import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.shops.InventoryMeta;
import com.z0cken.mc.economy.shops.TradeInventoryType;
import com.z0cken.mc.economy.shops.Trader;
import com.z0cken.mc.economy.shops.gui.TraderConfigGUI;
import com.z0cken.mc.economy.shops.gui.TraderTradeSelectionGUI;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.sql.Connection;

public class PlayerListener implements Listener {

    public PlayerListener(){ }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player p = event.getPlayer();

        PCS_Economy.pcs_economy.getLogger().info("Player joined");
        PCS_Economy.pcs_economy.getLogger().info("JoinedBefore? " + p.hasPlayedBefore());
        PCS_Economy.pcs_economy.accountManager.addAccountFromPlayer(p);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        PCS_Economy.pcs_economy.accountManager.removeAccountFromMap(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityInteract(PlayerInteractEntityEvent e){
        if(e.getPlayer() != null){
            if(e.getRightClicked() != null && e.getRightClicked() instanceof Villager){
                Villager v = (Villager)e.getRightClicked();
                Trader trader = PCS_Economy.pcs_economy.traderManager.getTrader(v.getUniqueId());
                if(trader != null) {
                    e.setCancelled(true);
                    if(e.getPlayer().isSneaking()){
                        Inventory inv = new TraderConfigGUI(trader).getInventory();
                        PCS_Economy.pcs_economy.inventoryManager.getInventories().put(inv, new InventoryMeta(trader, TradeInventoryType.CONFIG));
                        e.getPlayer().openInventory(inv);
                    }else{
                        Inventory inv = new TraderTradeSelectionGUI(trader).getInventory();
                        PCS_Economy.pcs_economy.inventoryManager.getInventories().put(inv, new InventoryMeta(trader, TradeInventoryType.SELECTION));
                        e.getPlayer().openInventory(inv);
                    }
                }
                return;
            }
            return;
        }
        return;
    }

    /*
    * Das soll, wenn ein Spieler über einen Shop etwas kaufen möchte, verhindern, dass er ein Item aufnimmt. Man muss immer mit Trolls rechnen.
    * Kann aber sein, dass das nicht funktioniert.
    * */
    //TODO
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityPickup(EntityPickupItemEvent e){
        if(e.getEntityType() == EntityType.PLAYER){
            Player p = (Player)e.getEntity();
            if(PCS_Economy.pcs_economy.inventoryManager.getInventories().containsKey(p.getOpenInventory().getTopInventory())){
                e.setCancelled(true);
            }
        }
    }
}
