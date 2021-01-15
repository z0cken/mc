package com.z0cken.mc.economy.events;

import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.economy.shops.InventoryMeta;
import com.z0cken.mc.economy.shops.TradeInventoryType;
import com.z0cken.mc.economy.shops.Trader;
import com.z0cken.mc.economy.shops.gui.TraderConfigGUI;
import com.z0cken.mc.economy.shops.gui.TraderTradeSelectionGUI;
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

public class PlayerListener implements Listener {

    public PlayerListener(){ }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player p = event.getPlayer();
        PCS_Economy.pcs_economy.createPlayerCommandCompletionHandler();
        if(!p.hasPlayedBefore()){
            PCS_Economy.pcs_economy.accountManager.createAccount(p);
            PCS_Economy.pcs_economy.accountManager.getAccount(p).add(ConfigManager.config.getDouble("economy.currency.initialBalance"));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        PCS_Economy.pcs_economy.createPlayerCommandCompletionHandler();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityInteract(PlayerInteractEntityEvent e){
        if(e.getPlayer() != null){
            if(e.getRightClicked() != null && e.getRightClicked() instanceof Villager){
                Villager v = (Villager)e.getRightClicked();
                Trader trader = PCS_Economy.pcs_economy.traderManager.getTrader(v.getUniqueId());
                if(trader != null) {
                    e.setCancelled(true);
                    if(e.getPlayer().isSneaking() && e.getPlayer().hasPermission("pcs.economy.admin")){
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
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityPickup(EntityPickupItemEvent e){
        if(e.getEntity() instanceof Player){
            Player p = (Player)e.getEntity();
            if(PCS_Economy.pcs_economy.inventoryManager.getInventories().containsKey(p.getOpenInventory().getTopInventory())){
                e.setCancelled(true);
            }
        }
    }
}
