package com.z0cken.mc.shout.gui.menu;

import com.z0cken.mc.shout.Shout;
import com.z0cken.mc.shout.ShoutGroup;
import com.z0cken.mc.shout.config.ShoutManager;
import com.z0cken.mc.shout.gui.button.ShoutButton;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class ShoutPage extends Page{
    private int groupID;

    public ShoutPage(int slots, int groupID) {
        super(slots, ShoutManager.SHOUTS.get(groupID).getName());
        this.groupID = groupID;
    }

    @Override
    public void build(Menu menu, Player p) {
        menu.addPage(this);
        ShoutGroup group = ShoutManager.SHOUTS.get(groupID);
        for(Shout shout : group.getShouts().values()){
            ShoutButton button = new ShoutButton(shout.getMaterial(), groupID, shout.getId());
            this.addItem(button);
        }
    }

    @Override
    public boolean close(InventoryCloseEvent e) {
        return false;
    }
}
