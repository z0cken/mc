package com.z0cken.mc.shout.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.z0cken.mc.shout.PCS_Shout;
import com.z0cken.mc.shout.Shout;
import com.z0cken.mc.shout.config.ConfigManager;
import com.z0cken.mc.shout.config.ShoutManager;
import com.z0cken.mc.shout.gui.ShoutFavorite;
import com.z0cken.mc.shout.gui.menu.MainPage;
import com.z0cken.mc.shout.gui.menu.Menu;
import com.z0cken.mc.shout.gui.menu.Page;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("shoutout")
public class ShoutCommand extends BaseCommand {

    @Dependency
    PCS_Shout pcs_shout;

    @Default
    @CommandPermission("pcs.shout")
    public void onShout(CommandSender sender, @Optional @Values("@slots") String index){
        if(sender instanceof Player){
            Player p = (Player)sender;
            if(index != null){
                int iIndex = Integer.valueOf(index);
                ShoutFavorite favorite = PCS_Shout.getInstance().getFavoriteManager().getFavorites(p.getUniqueId().toString()).getFavorite(iIndex - 1);
                if(favorite != null){
                    ShoutManager.SHOUTS.get(favorite.getGroupID()).getShouts().get(favorite.getShoutID()).play(p);
                }
            }else{
                Menu menu = new Menu(3, "Shoutouts", p);
                new MainPage(menu.getSize()).build(menu, p);
                menu.showPage(0);
                p.openInventory(menu);
            }
        }
    }

    @Subcommand("reload")
    @CommandPermission("pcs.shout.admin")
    public void onShoutReload(CommandSender sender){
        ShoutManager.load();
        if(sender instanceof Player){
            Player p = (Player)sender;
            p.spigot().sendMessage(pcs_shout.getMessageBuilder().build(ConfigManager.successReload));
        }else{
            pcs_shout.getLogger().info(ConfigManager.successReload.replace("{PREFIX}", ""));
        }
    }
}
