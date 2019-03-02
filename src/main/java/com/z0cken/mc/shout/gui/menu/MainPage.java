package com.z0cken.mc.shout.gui.menu;

import com.z0cken.mc.shout.PCS_Shout;
import com.z0cken.mc.shout.Shout;
import com.z0cken.mc.shout.ShoutGroup;
import com.z0cken.mc.shout.config.ShoutManager;
import com.z0cken.mc.shout.gui.ShoutFavorite;
import com.z0cken.mc.shout.gui.ShoutFavoriteManager;
import com.z0cken.mc.shout.gui.ShoutFavorites;
import com.z0cken.mc.shout.gui.button.Button;
import com.z0cken.mc.shout.gui.button.CategoryButton;
import com.z0cken.mc.shout.gui.button.FavoriteButton;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Set;

@SuppressWarnings("Duplicates")
public class MainPage extends Page{
    public MainPage(int slots) {
        super(slots, "Main");
    }

    @Override
    public void build(Menu menu, Player p){
        menu.addPage(this);
        load(menu, p);
    }

    @Override
    public void load(Menu menu, Player p) {
        ShoutFavorites favorites = PCS_Shout.getInstance().getFavoriteManager().getFavorites(p.getUniqueId().toString());
        if(favorites != null){
            for(int i = 0; i < 9; i++){
                ShoutFavorite favorite = favorites.getFavorite(i);
                if(favorite != null){
                    Shout shout = ShoutManager.SHOUTS.get(favorite.getGroupID()).getShouts().get(favorite.getShoutID());
                    this.setItem(i, new FavoriteButton(shout.getMaterial(), p, shout.getGroupID(), shout.getId()));
                }
            }
        }
        for(int i = 9; i < 18; i++){
            ItemStack glass = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
            ItemMeta glassMeta = glass.getItemMeta();
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
            this.setItem(i, glass);
        }
        Integer[] keys = Arrays.copyOf(ShoutManager.SHOUTS.keySet().toArray(), ShoutManager.SHOUTS.keySet().size(), Integer[].class);
        for(int i = 18; i < 27; i++){
            int keyIndex = i % 9;
            if(keyIndex < keys.length){
                ShoutGroup group = ShoutManager.SHOUTS.get(keys[keyIndex]);
                CategoryButton groupStack = new CategoryButton(group.getMaterial(), keys[keyIndex], p);
                this.setItem(i, groupStack);
                new ShoutPage(slots, group.getId()).build(menu, p);
            }
        }
    }

    @Override
    public boolean close(InventoryCloseEvent e) {
        return true;
    }

    @Override
    public void click(Menu menu, InventoryClickEvent e, Player p){
        ItemStack clickedStack = getItem(e.getSlot());
        e.setCancelled(true);
        if(clickedStack != null && clickedStack instanceof Button){
            Button button = (Button)clickedStack;
            if(clickedStack instanceof FavoriteButton){
                FavoriteButton fButton = (FavoriteButton)button;
                ClickType clickType = e.getClick();
                if(clickType == ClickType.LEFT){
                    fButton.click(menu, e);
                }
                if(clickType == ClickType.SHIFT_RIGHT){
                    int groupID = fButton.getGroupID();
                    int shoutID = fButton.getShoutID();
                    ShoutFavorites favorites = PCS_Shout.getInstance().getFavoriteManager().getFavorites(p.getUniqueId().toString());
                    if(favorites.hasFavorite(groupID, shoutID)){
                        favorites.removeFavorite(groupID, shoutID);
                    }
                    this.setItem(e.getSlot(), null);
                    menu.showPage(menu.getCurrentPage());
                }
            }else{
                button.click(menu, e);
            }
        }
    }
}