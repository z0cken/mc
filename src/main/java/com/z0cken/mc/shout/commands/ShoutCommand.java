package com.z0cken.mc.shout.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.z0cken.mc.shout.PCS_Shout;
import com.z0cken.mc.shout.config.ConfigManager;
import com.z0cken.mc.shout.config.ShoutManager;
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
    public void onShout(CommandSender sender){
        if(sender instanceof Player){
            Player p = (Player)sender;

            Menu menu = new Menu(3, "Shoutouts", p);
            new MainPage(menu.getSize()).build(menu, p);
            menu.showPage(0);
            p.openInventory(menu);
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
