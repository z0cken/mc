package com.z0cken.mc.economy.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Subcommand;
import com.z0cken.mc.economy.PCS_Economy;
import org.bukkit.command.CommandSender;

@CommandAlias("shop")
public class ShopCommand extends BaseCommand {
    @Dependency
    PCS_Economy pcs_economy;

    public ShopCommand(){ }

    @Subcommand("create")
    public void onShopCreate(CommandSender sender){

    }
}
