package com.z0cken.mc.core.bukkit;

import com.z0cken.mc.core.util.MessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftInventoryCustom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Menu extends CraftInventoryCustom implements Listener {

    private Menu parent;
    private final List<ItemStack[]> pages = new ArrayList<>();
    private int currentPage = 0;

    public Menu(@Nonnull JavaPlugin plugin, int rows, @Nonnull String title) {
        this(plugin, rows, title, null);
    }

    public Menu(@Nonnull JavaPlugin plugin, int rows, @Nonnull String title, Menu parent) {
        super(null, rows * 9, title);
        pages.add(new ItemStack[getSize()]);
        this.parent = parent;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public ItemStack getItem(int index) {
        return getItem(getCurrentPage(), index);
    }

    public ItemStack getItem(int page, int index) {
        return pages.get(page)[index];
    }

    @Override
    public void setItem(int index, @Nullable ItemStack item) {
        setItem(currentPage, index, item);
    }

    public void setItem(int page, int index, @Nullable ItemStack item) {
        while (pages.size() <= page) pages.add(new ItemStack[getSize()]);
        pages.get(page)[index] = item;
        if (page == currentPage) super.setItem(index, item);
    }

    @Override
    public void setContents(@Nullable ItemStack[] items) {
        setContents(currentPage, items);
    }

    public void setContents(int page, @Nullable ItemStack[] items) {
        if(items == null) clearPage(page);

        else {
            if (this.getSize() < items.length) {
                throw new IllegalArgumentException("Invalid inventory size; expected " + this.getSize() + " or less");
            } else {
                for (int i = 0; i < this.getSize(); ++i) {
                    ItemStack itemStack = items[i];
                    this.setItem(page, i, itemStack);
                }
            }
        }
    }

    public void showPage(int page) {
        if(page < 0 || page >= pages.size()) throw new IndexOutOfBoundsException("Page " + page + " is out of bounds");
        currentPage = page;
        setContents(pages.get(page));
    }

    public void clearPage(int page) {
        ItemStack[] empty = new ItemStack[getSize()];
        pages.set(page, empty);
        if(page == currentPage) clear();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public Menu getParent() {
        return parent;
    }

    public int getPageCount() {
        return pages.size();
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if(!event.getInventory().equals(this)) return;
        int hashCode = this.hashCode();
        for(Integer i : event.getRawSlots()) {
            Inventory inventory = event.getView().getInventory(i);
            if(inventory.hashCode() == hashCode && inventory.equals(this)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory inv = event.getView().getTopInventory();
        if (inv == null || !inv.equals(this)) return;

        if(event.getClick().isShiftClick()) event.setCancelled(true);
        if(event.getClick() == ClickType.DOUBLE_CLICK && this.contains(event.getCursor().getType())) event.setCancelled(true);
        if(inv != event.getClickedInventory()) return;
        event.setCancelled(true);

        ItemStack itemStack = pages.get(currentPage)[event.getSlot()];
        if(itemStack == null) return;
        if(!(itemStack instanceof Button)) return;
        Button button = (Button) itemStack;
        if(button.getClickEvent() == null) return;


        //TODO Implement Economy
        if (button instanceof PricedButton && ((PricedButton) button).price > 0) {
            int playerMoney = Integer.MAX_VALUE;
            if (playerMoney < ((PricedButton) button).price) {
                //Play sound
                return;
            } else {
                //Deduct price
            }
        }

        button.clickEvent.run(this, button, (Player) event.getWhoClicked(), event);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory() != this) return;

        if (parent != null) event.getPlayer().openInventory(parent);
    }

    public static class Button extends ItemStack {

        public interface ClickEvent {
            void run(Menu menu, Button button, Player player, InventoryClickEvent event);
        }

        protected Button.ClickEvent clickEvent;

        public Button(@Nullable ClickEvent clickEvent) {
            super();
            this.clickEvent = clickEvent;
        }

        public Button(@Nullable ClickEvent clickEvent, @Nonnull Material material) {
            super(material);
            this.clickEvent = clickEvent;
        }

        public Button(@Nullable ClickEvent clickEvent, @Nonnull Material material, int amount) {
            super(material, amount);
            this.clickEvent = clickEvent;
        }

        public ClickEvent getClickEvent() {
            return clickEvent;
        }

        public void setClickEvent(@Nullable ClickEvent clickEvent) {
            this.clickEvent = clickEvent;
        }

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

    public static class PricedButton extends Button {

        protected int price;

        public PricedButton(@Nullable ClickEvent clickEvent, int price) {
            super(clickEvent);
            this.price = price;
        }

        public PricedButton(@Nullable ClickEvent clickEvent, int price, Material material) {
            super(clickEvent, material);
            this.price = price;
        }

        public PricedButton(@Nullable ClickEvent clickEvent, int price, Material material, int amount) {
            super(clickEvent, material, amount);
            this.price = price;
        }

        public void setPricetagVisible(boolean visible) {
            ItemMeta itemMeta = getItemMeta();
            setItemMeta(itemMeta);
        }

        @Override
        public ItemMeta getItemMeta() {
            ItemMeta itemMeta = super.getItemMeta();

            List<String> lore = itemMeta.getLore();
            if (lore != null && !lore.isEmpty()) lore.remove(lore.size() - 1);
            itemMeta.setLore(lore);

            return itemMeta;
        }

        @Override
        public boolean setItemMeta(@Nonnull ItemMeta itemMeta) {
            MessageBuilder builder = MessageBuilder.DEFAULT.define("PRICE", Integer.toString(price));
            List<String> lore = itemMeta.getLore();
            if(lore == null) lore = new ArrayList<>();

            for (ListIterator<String> it = lore.listIterator(); it.hasNext(); ) {
                it.set(builder.setValues(it.next()));
            }

            lore.add(builder.setValues(PCS_Core.getInstance().getConfig().getString("button-pricetag")));

            itemMeta.setLore(lore);

            return super.setItemMeta(itemMeta);
        }
    }

}
