package com.z0cken.mc.shout.gui.menu;

import com.z0cken.mc.shout.PCS_Shout;
import com.z0cken.mc.shout.Shout;
import com.z0cken.mc.shout.ShoutGroup;
import com.z0cken.mc.shout.config.ShoutManager;
import com.z0cken.mc.shout.gui.ShoutFavorite;
import com.z0cken.mc.shout.gui.ShoutFavorites;
import com.z0cken.mc.shout.gui.button.CategoryButton;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Set;

public class MainPage extends Page{
    public MainPage(int slots) {
        super(slots, "Main");
    }

    @Override
    public void build(Menu menu, Player p){
        menu.addPage(this);
        ShoutFavorites favorites = PCS_Shout.getInstance().getFavoriteManager().getFavorites(p.getUniqueId().toString());
        if(favorites != null){
            for(int i = 0; i < 9; i++){
                ShoutFavorite favorite = favorites.getFavorite(i);
                if(favorite != null){
                    Shout shout = ShoutManager.SHOUTS.get(favorite.getGroupID()).getShouts().get(favorite.getShoutID());
                    this.setItem(i, new ItemStack(shout.getMaterial()));
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
                CategoryButton groupStack = new CategoryButton(group.getMaterial(), keys[keyIndex]);
                ItemMeta groupMeta = groupStack.getItemMeta();
                groupMeta.setDisplayName(group.getName());
                groupStack.setItemMeta(groupMeta);
                this.setItem(i, groupStack);
                new ShoutPage(slots, group.getId()).build(menu, p);
            }
        }
    }

    @Override
    public boolean close(InventoryCloseEvent e) {
        return true;
    }
}
