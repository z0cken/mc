package com.z0cken.mc.core.bukkit;

import com.z0cken.mc.core.util.MessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftInventoryCustom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Menu extends CraftInventoryCustom implements Listener {

    private Menu parent;

    public Menu(JavaPlugin plugin, int rows) {
        this(plugin, rows, null);
    }

    public Menu(JavaPlugin plugin, int rows, Menu parent) {
        super(null, rows * 9);
        this.parent = parent;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

   @EventHandler
   public void onInteract(InventoryInteractEvent event) {
        //TODO Test event reliability
        if(event.getInventory() != this) return;
        event.setCancelled(true);
   }


   @EventHandler
   public void onClick(InventoryClickEvent event) {
        Bukkit.broadcastMessage("ClickEvent 1");
        if(event.getClickedInventory() != this || !(event.getCurrentItem() instanceof Button)) {
            System.out.println(event.getClickedInventory().hashCode());
            System.out.println(((CraftInventory)event.getClickedInventory()).getInventory().hashCode());
            System.out.println(this.getInventory().hashCode());
            return;
        }
        event.setCancelled(true);
        Bukkit.broadcastMessage("ClickEvent 2");

        Button button = (Button) event.getCurrentItem();
        if(button.getClickEvent() == null) return;


        //TODO Implement Economy
        if(button instanceof PricedButton && ((PricedButton)button).price > 0) {
            int playerMoney = Integer.MAX_VALUE;
            if(playerMoney < ((PricedButton)button).price) {
                //Play sound
                return;
            } else {
                //Deduct price
            }
        }

        button.clickEvent.run(this, button, (Player) event.getWhoClicked(), event.getClick());
        setItem(event.getSlot(), button);
        Bukkit.broadcastMessage("ClickEvent 3");

   }

   @EventHandler
   public void onClose(InventoryCloseEvent event) {
        if(event.getInventory() != this) return;

        if(parent != null) event.getPlayer().openInventory(parent);
   }

    public static class Button extends ItemStack {

        public interface ClickEvent {
            void run(Menu menu, Button button, Player player, ClickType clickType);
        }

        protected Button.ClickEvent clickEvent;

        public Button(ClickEvent clickEvent) { this.clickEvent = clickEvent; }
        public Button(ClickEvent clickEvent, Material material) { super(material); this.clickEvent = clickEvent; }
        public Button(ClickEvent clickEvent, Material material, int amount) { super(material, amount); this.clickEvent = clickEvent; }

        public ClickEvent getClickEvent() {
            return clickEvent;
        }

        public void setClickEvent(ClickEvent clickEvent) { this.clickEvent = clickEvent; }

        @Override
        public Button clone() {
            throw new UnsupportedOperationException();
            /*Button button = (Button) super.clone();
            button.clickEvent = clickEvent;
            button.price = price;

            return button;*/
        }

        @Override
        public int hashCode() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }

        @Override
        public Map<String, Object> serialize() {
            throw new UnsupportedOperationException();
        }

        public static Button deserialize(Map<String, Object> args) {
            throw new UnsupportedOperationException();
        }
    }

    public class PricedButton extends Button {

        protected int price;

        public PricedButton(ClickEvent clickEvent, int price) { super(clickEvent); this.price = price; }
        public PricedButton(ClickEvent clickEvent, int price, Material material) { super(clickEvent, material); this.price = price; }
        public PricedButton(ClickEvent clickEvent, int price, Material material, int amount) { super(clickEvent, material, amount); this.price = price; }

        public void setPricetagVisible(boolean visible) {
            ItemMeta itemMeta = getItemMeta();
            setItemMeta(itemMeta);
        }

        @Override
        public ItemMeta getItemMeta() {
            ItemMeta itemMeta = super.getItemMeta();

            List<String> lore = itemMeta.getLore();
            lore.remove(lore.size()-1);
            itemMeta.setLore(lore);

            return itemMeta;
        }

        @Override
        public boolean setItemMeta(ItemMeta itemMeta) {
            MessageBuilder builder = new MessageBuilder().define("PRICE", Integer.toString(price));
            List<String> lore = itemMeta.getLore();

            for (ListIterator<String> it = lore.listIterator(); it.hasNext(); ) {
                it.set(builder.setValues(it.next()));
            }

            lore.add(builder.setValues(PCS_Core.getInstance().getConfig().getString("button-pricetag")));

            itemMeta.setLore(lore);

            return super.setItemMeta(itemMeta);
        }
    }

}
