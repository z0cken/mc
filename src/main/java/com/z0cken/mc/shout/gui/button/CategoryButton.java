package com.z0cken.mc.shout.gui.button;

import com.z0cken.mc.shout.PCS_Shout;
import com.z0cken.mc.shout.ShoutGroup;
import com.z0cken.mc.shout.config.ConfigManager;
import com.z0cken.mc.shout.config.ShoutManager;
import com.z0cken.mc.shout.gui.LoreBuilder;
import com.z0cken.mc.shout.gui.menu.Page;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

public class CategoryButton extends Button{
    private int groupID;

    public CategoryButton(Material material, int groupID, Player p) {
        super(material);
        this.groupID = groupID;

        ShoutGroup group = ShoutManager.SHOUTS.get(groupID);

        if(group.hasPermission() && !p.hasPermission(group.getPermission())){
            this.setType(Material.GRAY_STAINED_GLASS);
        }

        ItemMeta meta = this.getItemMeta();
        meta.setDisplayName(ShoutManager.SHOUTS.get(groupID).getName());
        if(group.hasPermission() && !p.hasPermission(group.getPermission()) && group.getPrice() > 0){
            meta.setLore(LoreBuilder.build(ConfigManager.loreGroupBuyPrice, group.getPrice(), ConfigManager.loreGroupBuyDescription));
        }
        this.setItemMeta(meta);
    }


    @Override
    protected ClickEvent createClickEvent() {
        ClickEvent event = ((menu, e) -> {
            if(e.getWhoClicked() instanceof Player){
                Player p = (Player)e.getWhoClicked();
                ShoutGroup group = ShoutManager.SHOUTS.get(groupID);
                Page page = menu.getPages().stream().filter(page1 -> page1.getName().equals(group.getName())).findFirst().orElse(null);
                if(page != null){
                    if(group.hasPermission()){
                        if(p.hasPermission(group.getPermission())){
                            menu.showPage(menu.getCurrentPage());
                        }else{
                            if(group.getPrice() > 0 && PCS_Shout.getInstance().getEconomy().has(p, group.getPrice())){
                                if(PCS_Shout.getInstance().getEconomy().withdrawPlayer(p, group.getPrice()).type == EconomyResponse.ResponseType.SUCCESS){
                                    this.setType(group.getMaterial());
                                    ItemMeta meta = this.getItemMeta();
                                    meta.setLore(null);
                                    this.setItemMeta(meta);
                                    PCS_Shout.getInstance().getPermissions().playerAdd(p, group.getPermission());
                                    menu.showPage(menu.getCurrentPage());
                                }
                            }
                        }
                    }else{
                        menu.showPage(page.getIndex());
                    }
                }
            }
        });
        return event;
    }
}
