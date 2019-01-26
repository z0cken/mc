package com.z0cken.mc.economy.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.contexts.OnlinePlayer;
import com.z0cken.mc.economy.Account;
import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.economy.shops.TradeItem;
import com.z0cken.mc.economy.shops.Trader;
import com.z0cken.mc.economy.utils.MessageBuilder;
import com.z0cken.mc.economy.utils.NBTUtils;
import net.milkbowl.vault.economy.EconomyResponse;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.NBTTagString;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.UUID;

@CommandAlias("m0ney")
public class MoneyCommand extends BaseCommand {

    @Dependency
    private PCS_Economy pcs_economy;

    private String permissionUser = ConfigManager.permissionUser;
    private String permissionAdmin = ConfigManager.permissionAdmin;

    public MoneyCommand(){

    }

    @Subcommand("balance")
    @CommandPermission("pcs.economy.user")
    public void onBalance(CommandSender sender){
        sender.sendMessage(MessageBuilder.buildMessage(true, ConfigManager.accountSuccessBalanceSelf, sender.getName(),
                pcs_economy.accountManager.getAccount(sender.getName()).getBalance()));
    }

    @Subcommand("balance")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    public void onBalancePlayer(CommandSender sender, OnlinePlayer player){
        sender.sendMessage(MessageBuilder.buildMessage(true, ConfigManager.accountSuccessBalanceOther, player.getPlayer().getName(),
                null, pcs_economy.accountManager.getAccount(player.getPlayer().getName()).getBalance(), 0));
    }

    @Subcommand("pay")
    @CommandPermission("pcs.economy.user")
    @CommandCompletion("@players")
    public void onPay(CommandSender sender, OnlinePlayer receiver, double amount){
        Account from = pcs_economy.accountManager.getAccount(sender.getName());
        Account to = pcs_economy.accountManager.getAccount(receiver.getPlayer().getName());
        EconomyResponse response = payHandler(sender, from, to, amount);
        if(response.type == EconomyResponse.ResponseType.SUCCESS){
            sender.sendMessage(MessageBuilder.buildMessage(true, ConfigManager.paymentSuccessSender, sender.getName(), receiver.getPlayer().getName(), amount, 0));
            receiver.getPlayer().sendMessage(MessageBuilder.buildMessage(true, ConfigManager.paymentSuccessReceiver, sender.getName(), receiver.getPlayer().getName(), amount, 0));
        }else{
            sender.sendMessage(MessageBuilder.buildMessage(true, response.errorMessage));
        }
    }

    @Subcommand("payo")
    @CommandPermission("pcs.economy.user")
    public void onPay(CommandSender sender, String receiver, double amount){
        Account from = pcs_economy.accountManager.getAccount(sender.getName());
        Account to = pcs_economy.accountManager.getAccount(receiver);
        EconomyResponse response = payHandler(sender, from, to, amount);
        if(response.type == EconomyResponse.ResponseType.SUCCESS){
            sender.sendMessage(MessageBuilder.buildMessage(true, ConfigManager.paymentSuccessSender, sender.getName(), receiver, amount, 0));
        }else{
            sender.sendMessage(MessageBuilder.buildMessage(true, response.errorMessage));
        }
    }

    @Subcommand("account clear")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    public void onClear(CommandSender sender, OnlinePlayer player){
        EconomyResponse response = pcs_economy.accountManager.getAccount(player.getPlayer()).clearBalance();
        if(response.type == EconomyResponse.ResponseType.FAILURE){
            sender.sendMessage(MessageBuilder.buildMessage(true, ConfigManager.accountErrorClear, player.getPlayer().getName()));
        }else{
            sender.sendMessage(MessageBuilder.buildMessage(true, ConfigManager.accountSuccessClear, player.getPlayer().getName()));
        }
    }

    @Subcommand("account delete")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    public void onAccountDelete(CommandSender sender, OnlinePlayer player){
        boolean response = pcs_economy.accountManager.deleteAccount(player.getPlayer());
        if(response){
            sender.sendMessage(MessageBuilder.buildMessage(true, ConfigManager.accountSuccessDelete, player.getPlayer().getName()));
        }else{
            sender.sendMessage(MessageBuilder.buildMessage(true, ConfigManager.accountErrorClear, player.getPlayer().getName()));
        }
    }

    @Subcommand("account create")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    public void onAccountCreate(CommandSender sender, OnlinePlayer player){
        boolean response = pcs_economy.accountManager.createAccount(player.getPlayer());
        if(response){
            sender.sendMessage(MessageBuilder.buildMessage(true, ConfigManager.accountSuccessCreate, player.getPlayer().getName()));
        }else{
            sender.sendMessage(MessageBuilder.buildMessage(true, ConfigManager.accountErrorCreate, player.getPlayer().getName()));
        }
    }

    @Subcommand("account set")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    public void onAccountSet(CommandSender sender, OnlinePlayer player, double amount){
        EconomyResponse response = pcs_economy.accountManager.getAccount(player.getPlayer()).setBalance(amount);
        if(response.type == EconomyResponse.ResponseType.SUCCESS){
            sender.sendMessage(MessageBuilder.buildMessage(true, ConfigManager.accountSuccessSet, player.getPlayer().getName(), amount));
        }else{
            sender.sendMessage(MessageBuilder.buildMessage(true, ConfigManager.accountErrorSet, player.getPlayer().getName()));
        }
    }

    @Subcommand("account add")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    public void onAccountAdd(CommandSender sender, OnlinePlayer player, double amount){
        EconomyResponse response = pcs_economy.accountManager.getAccount(player.getPlayer()).add(amount);
        String message;
        if(response.type == EconomyResponse.ResponseType.SUCCESS){
            sender.sendMessage(MessageBuilder.buildMessage(true, ConfigManager.accountSuccessAdd, player.getPlayer().getName(), amount));
        }else{
            sender.sendMessage(MessageBuilder.buildMessage(true, ConfigManager.accountErrorAdd, player.getPlayer().getName()));
        }
    }

    @Subcommand("account subtract")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    public void onAccountSubtract(CommandSender sender, OnlinePlayer player, double amount){
        EconomyResponse response = pcs_economy.accountManager.getAccount(player.getPlayer()).subtract(amount);
        if(response.type == EconomyResponse.ResponseType.SUCCESS){
            sender.sendMessage(MessageBuilder.buildMessage(true, ConfigManager.accountSuccessSubtract, player.getPlayer().getName(), amount));
        }else{
            sender.sendMessage(MessageBuilder.buildMessage(true, ConfigManager.accountErrorSubtract, player.getPlayer().getName()));
        }
    }

    @Subcommand("shop create")
    @CommandPermission("pcs.economy.user")
    public void onShopCreate(CommandSender sender, String name){
        if(sender instanceof Player){
            Player p = (Player) sender;
            Villager v = (Villager)p.getWorld().spawnEntity(p.getLocation(), EntityType.VILLAGER);

        }
    }

    @Subcommand("shop create admin")
    @CommandPermission("pcs.economy.admin")
    public void onShopCreateAdmin(CommandSender sender, String name){

    }

    @Subcommand("debug matlist")
    @CommandPermission("pcs.economy.admin")
    public void onDebugMatList(CommandSender sender){
        String fileContent = "";
        for(int i = 0; i < Material.values().length; i++){
            fileContent += Material.values()[i].name() + "\n";
        }
        try{
            PrintWriter fileWriter = new PrintWriter("mats.txt");
            fileWriter.write(fileContent);
            fileWriter.close();
        }catch (IOException e){
            sender.sendMessage(MessageBuilder.buildMessage(true, ConfigManager.errorGeneral));
        }
    }

    @Subcommand("debug db")
    @CommandPermission("pcs.economy.admin")
    public void onDebugDB(CommandSender sender){
        if(sender instanceof ConsoleCommandSender){
            sender.sendMessage(String.valueOf(pcs_economy.checkDBConnection()));
        }
    }

    @Subcommand("debug trader create")
    @CommandPermission("pcs.economy.admin")
    public void onDebugTraderCreate(CommandSender sender, String traderName){

    }

    @Subcommand("debug trader checknbt")
    @CommandPermission("pcs.economy.admin")
    public void onDebugTraderCheckNbt(CommandSender sender){
        if(sender instanceof Player){
            Player p = (Player)sender;
            ItemStack is = p.getInventory().getItemInMainHand();
            sender.sendMessage(is.getType().name());
            sender.sendMessage(NBTUtils.getStringValue(is, "trader"));
        }
    }

    @Subcommand("debug trader findowners")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    public void onDebugTraderFindOwners(CommandSender sender, Player p){
        if(sender instanceof Player){
            ArrayList<UUID> uuids = pcs_economy.traderManager.getTradersFromPlayer(p);
            if(uuids != null && uuids.size() > 0){
                uuids.forEach(uuid -> {
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

    @Subcommand("debug trader delete")
    @CommandPermission("pcs.economy.admin")
    public void onDebugTraderDelete(CommandSender sender){
        if(sender instanceof Player){

        }
    }

    @Subcommand("debug trader traderdiablock")
    @CommandPermission("pcs.economy.admin")
    public void onDebugTraderStick(CommandSender sender){
        if(sender instanceof Player){
            Player p = (Player) sender;
            if(p.getInventory().firstEmpty() != -1){
                ItemStack stack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName("Traderblock");
                stack.setItemMeta(meta);
                net.minecraft.server.v1_13_R2.ItemStack nmsDiaBlock = CraftItemStack.asNMSCopy(stack);
                NBTTagCompound compound = (nmsDiaBlock.hasTag()) ? nmsDiaBlock.getTag() : new NBTTagCompound();
                compound.set("trader", new NBTTagString("true"));
                nmsDiaBlock.setTag(compound);
                p.getInventory().addItem(CraftItemStack.asBukkitCopy(nmsDiaBlock));
            }
        }
    }

    @Subcommand("debug trader tojson")
    @CommandPermission("pcs.economy.admin")
    public void onDebugTraderToJson(CommandSender sender){
        pcs_economy.saveTraderManagerJSON();
    }

    private EconomyResponse payHandler(CommandSender sender, Account senderAccount, Account receiverAccount, double amount){
        if(senderAccount != null && receiverAccount != null){
            if(senderAccount.getHolder().getName().equalsIgnoreCase(receiverAccount.getHolder().getName())){
                return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, ConfigManager.paymentErrorSelf);
            }
            EconomyResponse ecoResponse = pcs_economy.accountManager.transferMoney(senderAccount, receiverAccount, amount);
            if(ecoResponse.type == EconomyResponse.ResponseType.FAILURE){
                return ecoResponse;
            }else{
                return ecoResponse;
            }
        }else{
            if(senderAccount == null){
                sender.sendMessage(MessageBuilder.buildMessage(true, ConfigManager.errorGeneral));
            }else{
                sender.sendMessage(MessageBuilder.buildMessage(true, ConfigManager.paymentErrorAccountNotExisting));
            }
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "");
        }
    }
}
