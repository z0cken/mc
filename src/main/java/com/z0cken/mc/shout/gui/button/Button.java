package com.z0cken.mc.shout.gui.button;

import com.z0cken.mc.shout.gui.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class Button extends ItemStack {
    public interface ClickEvent {
        void run(Menu menu, InventoryClickEvent e);
    }

    protected ClickEvent clickEvent;

    public Button(Material material){
        super(material);
        createClickEvent();
        this.clickEvent = createClickEvent();
    }

    public void click(Menu menu, InventoryClickEvent e){
        if(e.getWhoClicked() instanceof Player){
            clickEvent.run(menu, e);
        }
    }

    protected abstract ClickEvent createClickEvent();
}
