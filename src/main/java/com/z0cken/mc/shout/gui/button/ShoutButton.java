package com.z0cken.mc.shout.gui.button;

import com.z0cken.mc.shout.Shout;
import com.z0cken.mc.shout.config.ShoutManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

public class ShoutButton extends Button{
    private int groupID;
    private int shoutID;

    public ShoutButton(Material material, int groupID, int shoutID){
        super(material);
        this.groupID = groupID;
        this.shoutID = shoutID;
        ItemMeta meta = this.getItemMeta();
        meta.setDisplayName(ShoutManager.SHOUTS.get(groupID).getShouts().get(shoutID).getName());
        this.setItemMeta(meta);
    }

    @Override
    protected ClickEvent createClickEvent(){
        ClickEvent event = ((menu, e) -> {
            if(e.getWhoClicked() instanceof Player){
                Player p = (Player)e.getWhoClicked();
                Shout shout = ShoutManager.SHOUTS.get(groupID).getShouts().get(shoutID);
                p.getWorld().playSound(p.getLocation(), shout.getPath(), shout.getVolume(), shout.getPitch());
            }
        });
        return event;
    }

    public int getGroupID(){
        return this.groupID;
    }

    public int getShoutID(){
        return this.shoutID;
    }
}
