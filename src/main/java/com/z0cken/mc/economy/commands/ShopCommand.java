package com.z0cken.mc.economy.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
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
    public void onShopCreateAdmin(CommandSender sender, String traderName){
        if(sender instanceof Player){
            Player p = (Player) sender;
            Villager v = (Villager)p.getWorld().spawnEntity(p.getLocation(), EntityType.VILLAGER);
            v.setInvulnerable(true);
            v.setCustomName(traderName);
            v.setCustomNameVisible(true);

            Trader trader = new Trader(v, p, traderName, true);

            TradeItem item1 = new TradeItem(Material.DIRT, 20, 10, true, true, 0);
            TradeItem item2 = new TradeItem(Material.GLASS, 20, 5, true, true, 0);
            TradeItem item3 = new TradeItem(Material.SAND, 20, 3, true, true, 0);
            trader.addTradeItem(item1);
            trader.addTradeItem(item2);
            trader.addTradeItem(item3);

            pcs_economy.traderManager.addTrader(trader);
        }
    }
}
