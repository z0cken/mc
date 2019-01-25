package com.z0cken.mc.economy.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Subcommand;
import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.shops.TradeItem;
import com.z0cken.mc.economy.shops.Trader;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

@CommandAlias("shop")
public class ShopCommand extends BaseCommand {
    @Dependency
    PCS_Economy pcs_economy;

    public ShopCommand(){ }

    @Subcommand("create")
    public void onShopCreate(CommandSender sender, String traderName){
        if(sender instanceof Player){

        }
    }

    @Subcommand("create admin")
    @CommandPermission("pcs.economy.admin")
    public void onShopCreateAdmin(CommandSender sender, String traderName){
        if(sender instanceof Player){
            Player p = (Player) sender;
            Villager v = (Villager)p.getWorld().spawnEntity(p.getLocation(), EntityType.VILLAGER);
            v.setInvulnerable(true);
            v.setCustomName(traderName);
            v.setCustomNameVisible(true);
            Trader trader = new Trader(v, p, traderName, true);
            pcs_economy.traderManager.addTrader(trader);
        }
    }
}
