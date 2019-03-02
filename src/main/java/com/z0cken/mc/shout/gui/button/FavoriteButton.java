package com.z0cken.mc.shout.gui.button;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class FavoriteButton extends ShoutButton{
    public FavoriteButton(Material material, Player p, int groupID, int shoutID){
        super(material, p, groupID, shoutID);
    }

    @Override
    protected ClickEvent createClickEvent() {
        return super.createClickEvent();
    }
}
