package com.z0cken.mc.shout.gui.menu;

import com.z0cken.mc.shout.gui.button.Button;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public abstract class Page {
    private ItemStack[] stacks;
    private int index;
    private String name;
    protected int slots;

    public Page(int slots, String name){
        this.stacks = new ItemStack[slots];
        this.slots = slots;
        this.name = name;
    }

    public void setItem(int i, ItemStack stack){
        if(checkBounds(i)){
            stacks[i] = stack;
        }
    }

    public ItemStack getItem(int i){
        if(checkBounds(i)){
            return stacks[i];
        }
        return null;
    }

    public boolean checkBounds(int i){
        if(i >= 0 && i < stacks.length){
            return true;
        }
        return false;
    }

    public ItemStack[] getContents(){
        return this.stacks;
    }

    public void click(Menu menu, InventoryClickEvent e, Player p){
        ItemStack clickedStack = getItem(e.getSlot());
        e.setCancelled(true);
        if(clickedStack != null && clickedStack instanceof Button){
            Button button = (Button)clickedStack;
            button.click(menu, e);
        }
    }

    public int firstEmpty(){
        for(int i = 0; i < stacks.length; i++){
            if(stacks[i] == null || stacks[i].getType() == Material.AIR){
                return i;
            }
        }
        return -1;
    }

    public void addItem(ItemStack stack){
        int firstEmpty = firstEmpty();
        if(firstEmpty != -1){
            this.setItem(firstEmpty, stack);
        }
    }

    public String getName(){
        return this.name;
    }

    public int getIndex(){
        return this.index;
    }

    public void setIndex(int index){
        this.index = index;
    }

    public abstract boolean close(InventoryCloseEvent e);
    public abstract void build(Menu menu, Player p);
    public abstract void load(Menu menu, Player p);
}
