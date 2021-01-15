package com.z0cken.mc.shout.gui.menu;

import com.z0cken.mc.shout.PCS_Shout;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventoryCustom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Menu extends CraftInventoryCustom implements Listener {
    private final PCS_Shout pcs_shout = PCS_Shout.getInstance();
    private final List<Page> pages;
    private int currentPage = 0;
    private Player player;

    public Menu(int rows, String title, Player player){
        super(null, rows * 9, title);
        pages = new ArrayList<>();
        this.player = player;
        pcs_shout.getServer().getPluginManager().registerEvents(this, pcs_shout);
    }

    public PCS_Shout getPlugin(){
        return this.pcs_shout;
    }

    public void setType(int i, Material mat){
        pages.get(currentPage).getItem(i).setType(mat);
        super.setItem(i, pages.get(currentPage).getItem(i));
    }

    @Override
    public void setItem(int i, ItemStack stack){
        setItem(currentPage, i, stack);
    }

    public void setItem(int page, int i, ItemStack stack){
        pages.get(page).setItem(i, stack);
        if(page == currentPage) super.setItem(i, stack);
    }

    @Override
    public void setContents(ItemStack[] stacks){
        setContents(currentPage, stacks);
    }

    public void setContents(int page, ItemStack[] stacks){
        if(this.getSize() < stacks.length){
            return;
        }else{
            ItemStack stack;
            for(int i = 0; i < this.getSize(); i++){
                stack = stacks[i];
                if(stack == null) stack = new ItemStack(Material.AIR);
                this.setItem(page, i, stack);
            }
        }
    }

    public int getCurrentPage(){
        return currentPage;
    }

    public int getPagesSize(){
        return this.pages.size();
    }

    public void addPage(Page page){
        if(page != null){
            page.setIndex(pages.size());
            pages.add(page);
        }
    }

    public void showPage(int i){
        if(i >= 0 && i < pages.size()){
            currentPage = i;
            setContents(pages.get(i).getContents());
        }
    }

    public Player getPlayer(){
        return this.player;
    }

    public List<Page> getPages(){
        return this.pages;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(e.getClickedInventory() != null){
            if(e.getWhoClicked() instanceof Player){
                Player p = (Player)e.getWhoClicked();
                Inventory topInventory = p.getOpenInventory().getTopInventory();
                Inventory bottomInventory = p.getOpenInventory().getBottomInventory();
                if(topInventory != null && bottomInventory != null && topInventory.equals(this)){
                    e.setCancelled(true);
                    if(!e.getClickedInventory().equals(bottomInventory)){
                        pages.get(currentPage).click(this, e, p);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e){
        if(e.getInventory().equals(this)){
            if(!pages.get(currentPage).close(e)){
                if(e.getPlayer() instanceof Player) {
                    Player p = (Player) e.getPlayer();
                    if(pages.get(currentPage) instanceof ShoutPage){
                        this.showPage(0);
                        Menu menu = this;
                        pcs_shout.getServer().getScheduler().scheduleSyncDelayedTask(pcs_shout, new Runnable() {
                            @Override
                            public void run() {
                                p.openInventory(menu);
                            }
                        }, 1);
                    }
                }
            }
        }
    }
}
