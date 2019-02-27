package com.z0cken.mc.shout.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.z0cken.mc.shout.gui.menu.MainPage;
import com.z0cken.mc.shout.gui.menu.Menu;
import com.z0cken.mc.shout.gui.menu.Page;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("shoutout")
public class ShoutCommand extends BaseCommand {

    @Default
    @CommandPermission("pcs.shout")
    public void onShout(CommandSender sender){
        if(sender instanceof Player){
            Player p = (Player)sender;

            //p.getWorld().playSound(p.getLocation(), "ronny.arbeit", SoundCategory.NEUTRAL, 100, 50);
            Menu menu = new Menu(3, "Shoutouts");
            new MainPage(menu.getSize()).build(menu, p);
            menu.showPage(0);
            p.openInventory(menu);
        }
    }
}
