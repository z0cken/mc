package com.z0cken.mc.shout.gui.button;

import com.z0cken.mc.shout.ShoutGroup;
import com.z0cken.mc.shout.config.ShoutManager;
import com.z0cken.mc.shout.gui.menu.Page;
import org.bukkit.Material;

public class CategoryButton extends Button{
    private int groupID;

    public CategoryButton(Material material, int groupID) {
        super(material);
        this.groupID = groupID;
    }


    @Override
    protected ClickEvent createClickEvent() {
        ClickEvent event = ((menu, e) -> {
            ShoutGroup group = ShoutManager.SHOUTS.get(groupID);
            Page page = menu.getPages().stream().filter(page1 -> page1.getName().equals(group.getName())).findFirst().orElse(null);
            if(page != null){
                menu.showPage(page.getIndex());
            }
        });
        return event;
    }
}
