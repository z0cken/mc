package com.z0cken.mc.shout.gui.menu;

import com.z0cken.mc.shout.PCS_Shout;
import com.z0cken.mc.shout.Shout;
import com.z0cken.mc.shout.ShoutGroup;
import com.z0cken.mc.shout.config.ShoutManager;
import com.z0cken.mc.shout.gui.ShoutFavorite;
import com.z0cken.mc.shout.gui.ShoutFavorites;
import com.z0cken.mc.shout.gui.button.Button;
import com.z0cken.mc.shout.gui.button.ShoutButton;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class ShoutPage extends Page{
    private int groupID;

    public ShoutPage(int slots, int groupID) {
        super(slots, ShoutManager.SHOUTS.get(groupID).getName());
        this.groupID = groupID;
    }

    @Override
    public void build(Menu menu, Player p) {
        menu.addPage(this);
        load(menu, p);
    }

    @Override
    public void load(Menu menu, Player p) {
        ShoutGroup group = ShoutManager.SHOUTS.get(groupID);
        for(Shout shout : group.getShouts().values()){
            ShoutButton button = new ShoutButton(shout.getMaterial(), p, groupID, shout.getId());
            this.addItem(button);
        }
    }

    @Override
    public boolean close(InventoryCloseEvent e) {
        return false;
    }

    @Override
    public void click(Menu menu, InventoryClickEvent e, Player p){
        ItemStack clickedStack = getItem(e.getSlot());
        e.setCancelled(true);
        if(clickedStack != null && clickedStack instanceof Button){
            ShoutButton button = (ShoutButton)clickedStack;
            ClickType clickType = e.getClick();
            if(clickType == ClickType.LEFT){
                button.click(menu, e);
            }
            if(clickType == ClickType.SHIFT_LEFT){
                Shout shout = ShoutManager.SHOUTS.get(button.getGroupID()).getShouts().get(button.getShoutID());
                if(shout.hasPermission()){
                    if(p.hasPermission(shout.getPermission())){
                        shoutShiftLeft(menu, button, p);
                    }
                }else{
                    shoutShiftLeft(menu, button, p);
                }
            }
            if(clickType == ClickType.SHIFT_RIGHT){
                Shout shout = ShoutManager.SHOUTS.get(button.getGroupID()).getShouts().get(button.getShoutID());
                if(shout.hasPermission()){
                    if(p.hasPermission(shout.getPermission())){
                        shoutShiftRight(menu, button, p);
                    }
                }else{
                    shoutShiftRight(menu, button, p);
                }

            }
        }
    }

    private void shoutShiftLeft(Menu menu, ShoutButton button, Player p){
        ShoutFavorites favorites = PCS_Shout.getInstance().getFavoriteManager().getFavorites(p.getUniqueId().toString());
        if(!favorites.hasFavorite(button.getGroupID(), button.getShoutID())){
            int firstFree = favorites.firstFree();
            if(firstFree != -1){
                ShoutFavorite favorite = new ShoutFavorite(button.getGroupID(), button.getShoutID());
                favorites.setFavorite(firstFree, favorite);
            }
            menu.getPages().get(0).load(menu, p);
        }
    }

    private void shoutShiftRight(Menu menu, ShoutButton button, Player p){
        ShoutFavorites favorites = PCS_Shout.getInstance().getFavoriteManager().getFavorites(p.getUniqueId().toString());
        if(!favorites.hasFavorite(button.getGroupID(), button.getShoutID())){
            ShoutFavorite favorite = new ShoutFavorite(button.getGroupID(), button.getShoutID());
            int firstFree = favorites.firstFree();
            if(firstFree == -1){
                favorites.setFavorite(firstFree, favorite);
            }else{
                favorites.setFavorite(8, favorite);
            }
            menu.getPages().get(0).load(menu, p);
        }
    }
}
