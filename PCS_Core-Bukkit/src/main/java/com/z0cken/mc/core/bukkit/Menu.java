package com.z0cken.mc.core.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftInventoryCustom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class Menu extends CraftInventoryCustom implements Listener {

    private HashMap<Integer, Button> buttons = new HashMap<>();
    private Menu parent;

    public Menu(JavaPlugin plugin, int rows) {
        super(null, rows);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public Menu(JavaPlugin plugin, int rows, Menu parent) {
        super(null, rows);
        this.parent = parent;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void setButton(int slot, Button button) {
        buttons.put(slot, button);
        setItem(slot, button.itemStack);
    }

   @EventHandler
   public void onInteract(InventoryInteractEvent event) {
        //TODO Test event consistency
        if(event.getInventory() != this) return;
        event.setCancelled(true);
   }


   @EventHandler
   public void onClick(InventoryClickEvent event) {
        if(event.getClickedInventory() != this) return;
        event.setCancelled(true);

        Button button = buttons.getOrDefault(event.getSlot(), null);
        if(button == null) return;

        if(button.clickEvent != null) button.clickEvent.run(this, (Player) event.getWhoClicked());
   }

   @EventHandler
   public void onClose(InventoryCloseEvent event) {
        if(event.getInventory() != this) return;

        if(parent != null) event.getPlayer().openInventory(parent);
   }

    public static class Button {

        private final ItemStack itemStack;
        private Button.ClickEvent clickEvent;

        public Button(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        public Button(ItemStack itemStack, Button.ClickEvent clickEvent) {
            this.itemStack = itemStack;
            this.clickEvent = clickEvent;
        }

        public ClickEvent getClickEvent() {
            return clickEvent;
        }

        public void setClickEvent(ClickEvent clickEvent) {
            this.clickEvent = clickEvent;
        }

        public static abstract class ClickEvent {
            abstract void run(Menu menu, Player player);
        }


    }

}
