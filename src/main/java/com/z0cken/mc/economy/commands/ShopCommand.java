package com.z0cken.mc.economy.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.economy.shops.Trader;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.UUID;

@CommandAlias("shop")
public class ShopCommand extends BaseCommand {
    @Dependency
    PCS_Economy pcs_economy;

    public ShopCommand(){ }

    @Subcommand("create")
    @CommandPermission("pcs.economy.admin")
    public void onShopCreate(CommandSender sender, String traderName){
        if(sender instanceof Player){
            sender.sendMessage("Kommt später");
        }
    }

    @Subcommand("create admin")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@traderLook")
    public void onShopCreateAdmin(CommandSender sender, String traderName, @Optional@Values("@traderLook") String traderLook){
        if(sender instanceof Player){
            Player p = (Player) sender;
            Villager v = (Villager)p.getWorld().spawnEntity(p.getLocation(), EntityType.VILLAGER);
            v.setInvulnerable(true);
            v.setCustomName(traderName);
            v.setCustomNameVisible(true);
            if(traderLook != null){
                v.setProfession(Villager.Profession.valueOf(traderLook));
            }
            Trader trader = new Trader(pcs_economy.traderManager.generateTraderID(), v, p, traderName, true);
            pcs_economy.traderManager.addTrader(trader);
            pcs_economy.createTraderCommandCompletionHandler();
            p.spigot().sendMessage(pcs_economy.getMessageBuilder().build(ConfigManager.shopTraderSuccessCreateTrader));
        }
    }

    @Subcommand("findtraders")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    public void onDebugTraderFindOwners(CommandSender sender, Player p){
        if(sender instanceof Player){
            ArrayList<UUID> uuids = pcs_economy.traderManager.getTradersFromPlayer(p);
            if(uuids != null && uuids.size() > 0){
                uuids.forEach(uuid -> {
                    sender.sendMessage("Noch nicht gebraucht");
                    sender.sendMessage(uuid.toString());
                });
            }else{
                if(uuids == null){
                    sender.sendMessage("Null");
                }else{
                    sender.sendMessage("Nix drin");
                }
            }
        }
    }

    @Subcommand("delete")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@traderID")
    public void onTraderDelete(CommandSender sender, String sTraderID){
        if(sender instanceof Player){
            Player p = (Player)sender;
            if(sTraderID != null && !sTraderID.isEmpty()){
                try{
                    String[] args = sTraderID.split("#");
                    if(args.length == 2){
                        int traderID = Integer.valueOf(args[1]);
                        pcs_economy.traderManager.removeTrader(pcs_economy.traderManager.getTrader(traderID));
                        p.spigot().sendMessage(pcs_economy.getMessageBuilder().build(ConfigManager.shopTraderSuccessRemoveTrader));
                    }
                }catch (NumberFormatException e){
                    p.spigot().sendMessage(pcs_economy.getMessageBuilder().build(ConfigManager.shopTraderErrorRemoveTrader));
                }
            }
        }
    }

    @Subcommand("change name")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@traderID")
    public void onTraderChangeName(CommandSender sender, String sTraderID, String traderName){
        if(sender instanceof Player){
            Player p = (Player)sender;
            if(sTraderID != null && !sTraderID.isEmpty() && traderName != null && !traderName.isEmpty()){
                try{
                    String[] args = sTraderID.split("#");
                    if(args.length == 2){
                        int traderID = Integer.valueOf(args[1]);
                        Trader trader = pcs_economy.traderManager.getTrader(traderID);
                        if(trader != null){
                            if(pcs_economy.getServer().getEntity(trader.getTraderUUID()) != null){
                                pcs_economy.getServer().getEntity(trader.getTraderUUID()).setCustomName(traderName);
                                trader.setTraderName(traderName);
                            }
                        }

                    }
                }catch (NumberFormatException e){
                    p.spigot().sendMessage(pcs_economy.getMessageBuilder().build(ConfigManager.shopTraderErrorChangeName));
                }
            }
        }
    }

    @Subcommand("tpto")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@traderID")
    public void onTraderTeleportTo(CommandSender sender, String sTraderID){
        if(sender instanceof Player){
            Player p = (Player)sender;
            Trader trader = getTrader(sTraderID);
            if(trader != null){
                Entity entity = pcs_economy.getServer().getEntity(trader.getTraderUUID());
                if(entity != null && entity instanceof Villager){
                    Villager v = (Villager)entity;
                    p.teleport(v);
                }
            }else{
                p.spigot().sendMessage(pcs_economy.getMessageBuilder().build(ConfigManager.shopTraderErrorTpTo));
            }
        }
    }

    @Subcommand("tphere")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@traderID")
    public void onTraderTeleportHere(CommandSender sender, String sTraderID){
        if(sender instanceof Player){
            Player p = (Player)sender;
            Trader trader = getTrader(sTraderID);
            if(trader != null){
                Entity entity = pcs_economy.getServer().getEntity(trader.getTraderUUID());
                if(entity != null && entity instanceof Villager){
                    entity.teleport(p);
                }
            }else{
                p.spigot().sendMessage(pcs_economy.getMessageBuilder().build(ConfigManager.shopTraderErrorTpHere));
            }
        }
    }

    @Subcommand("career")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@traderID @traderLook")
    public void onTraderCareer(CommandSender sender, String sTraderID, String traderLook){
        if(sender instanceof Player){
            Trader trader = getTrader(sTraderID);
            if(trader != null){
                Entity entity = pcs_economy.getServer().getEntity(trader.getTraderUUID());
                if(entity != null && entity instanceof Villager){
                    Villager v = (Villager)entity;
                    v.setProfession(Villager.Profession.valueOf(traderLook));
                }
            }
        }
    }

    private Trader getTrader(String sTraderID){
        if(sTraderID != null && !sTraderID.isEmpty()){
            try{
                String[] args = sTraderID.split("#");
                if(args.length == 2){
                    int traderID = Integer.valueOf(args[1]);
                    Trader trader = pcs_economy.traderManager.getTrader(traderID);
                    return trader;
                }
            }catch (NumberFormatException e){
                return null;
            }
        }
        return null;
    }
}
