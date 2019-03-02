package com.z0cken.mc.shout.gui.button;

import com.z0cken.mc.shout.PCS_Shout;
import com.z0cken.mc.shout.Shout;
import com.z0cken.mc.shout.config.ConfigManager;
import com.z0cken.mc.shout.config.ShoutManager;
import com.z0cken.mc.shout.gui.LoreBuilder;
import com.z0cken.mc.shout.gui.menu.Menu;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;

public class ShoutButton extends Button{
    private int groupID;
    private int shoutID;

    public ShoutButton(Material material, Player p, int groupID, int shoutID){
        super(material);
        this.groupID = groupID;
        this.shoutID = shoutID;

        Shout shout = ShoutManager.SHOUTS.get(groupID).getShouts().get(shoutID);
        if(shout.hasPermission() && !p.hasPermission(shout.getPermission())){
            this.setType(Material.GRAY_STAINED_GLASS);
        }

        ItemMeta meta = this.getItemMeta();
        meta.setDisplayName(ShoutManager.SHOUTS.get(groupID).getShouts().get(shoutID).getName());
        if(shout.hasPermission() && !p.hasPermission(shout.getPermission()) && shout.getPrice() > 0){
            meta.setLore(LoreBuilder.build(ConfigManager.loreShoutBuyPrice, shout.getPrice(), ConfigManager.loreShoutBuyDescription));
        }
        this.setItemMeta(meta);
    }

    @Override
    protected ClickEvent createClickEvent(){
        ClickEvent event = ((menu, e, p) -> {
            Shout shout = ShoutManager.SHOUTS.get(groupID).getShouts().get(shoutID);
            if(shout.hasPermission()){
                if(p.hasPermission(shout.getPermission())){
                    p.getWorld().playSound(p.getLocation(), shout.getPath(), shout.getVolume(), shout.getPitch());
                }else{
                    if(shout.getPrice() > 0 && PCS_Shout.getInstance().getEconomy().has(p, shout.getPrice())){
                        if(PCS_Shout.getInstance().getEconomy().withdrawPlayer(p, shout.getPrice()).type == EconomyResponse.ResponseType.SUCCESS){
                            PCS_Shout.getInstance().getPermissions().playerAdd(p, shout.getPermission());
                            ItemMeta meta = this.getItemMeta();
                            meta.setLore(null);
                            this.setItemMeta(meta);
                            this.setType(shout.getMaterial());
                            menu.showPage(menu.getCurrentPage());
                        }
                    }
                }
            }else{
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
