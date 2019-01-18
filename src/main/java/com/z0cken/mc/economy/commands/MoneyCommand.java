package com.z0cken.mc.economy.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.contexts.OnlinePlayer;
import com.z0cken.mc.economy.Account;
import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.economy.utils.MessageBuilder;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import sun.security.krb5.Config;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

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
        sender.sendMessage(MessageBuilder.buildMessage(ConfigManager.accountSuccessBalanceSelf, sender.getName(),
                pcs_economy.accountManager.getAccount(sender.getName()).getBalance()));
    }

    @Subcommand("balance")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    public void onBalancePlayer(CommandSender sender, OnlinePlayer player){
        sender.sendMessage(MessageBuilder.buildMessage(ConfigManager.accountSuccessBalanceOther, player.getPlayer().getName(),
                null, pcs_economy.accountManager.getAccount(player.getPlayer().getName()).getBalance()));
    }

    @Subcommand("pay")
    @CommandPermission("pcs.economy.user")
    @CommandCompletion("@players")
    public void onPay(CommandSender sender, OnlinePlayer receiver, double amount){
        Account from = pcs_economy.accountManager.getAccount(sender.getName());
        Account to = pcs_economy.accountManager.getAccount(receiver.getPlayer().getName());
        EconomyResponse response = payHandler(sender, from, to, amount);
        if(response.type == EconomyResponse.ResponseType.SUCCESS){
            sender.sendMessage(MessageBuilder.buildMessage(ConfigManager.paymentSuccessSender, sender.getName(), receiver.getPlayer().getName(), amount));
            receiver.getPlayer().sendMessage(MessageBuilder.buildMessage(ConfigManager.paymentSuccessReceiver, sender.getName(), receiver.getPlayer().getName(), amount));
        }else{
            sender.sendMessage(MessageBuilder.buildMessage(response.errorMessage));
        }
    }

    @Subcommand("payo")
    @CommandPermission("pcs.economy.user")
    public void onPay(CommandSender sender, String receiver, double amount){
        Account from = pcs_economy.accountManager.getAccount(sender.getName());
        Account to = pcs_economy.accountManager.getAccount(receiver);
        EconomyResponse response = payHandler(sender, from, to, amount);
        if(response.type == EconomyResponse.ResponseType.SUCCESS){
            sender.sendMessage(MessageBuilder.buildMessage(ConfigManager.paymentSuccessSender, sender.getName(), receiver, amount));
        }else{
            sender.sendMessage(MessageBuilder.buildMessage(response.errorMessage));
        }
    }

    @Subcommand("account clear")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    public void onClear(CommandSender sender, OnlinePlayer player){
        EconomyResponse response = pcs_economy.accountManager.getAccount(player.getPlayer()).clearBalance();
        if(response.type == EconomyResponse.ResponseType.FAILURE){
            sender.sendMessage(MessageBuilder.buildMessage(ConfigManager.accountErrorClear, player.getPlayer().getName()));
        }else{
            sender.sendMessage(MessageBuilder.buildMessage(ConfigManager.accountSuccessClear, player.getPlayer().getName()));
        }
    }

    @Subcommand("account delete")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    public void onAccountDelete(CommandSender sender, OnlinePlayer player){
        boolean response = pcs_economy.accountManager.deleteAccount(player.getPlayer());
        if(response){
            sender.sendMessage(MessageBuilder.buildMessage(ConfigManager.accountSuccessDelete, player.getPlayer().getName()));
        }else{
            sender.sendMessage(MessageBuilder.buildMessage(ConfigManager.accountErrorClear, player.getPlayer().getName()));
        }
    }

    @Subcommand("account create")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    public void onAccountCreate(CommandSender sender, OnlinePlayer player){
        boolean response = pcs_economy.accountManager.createAccount(player.getPlayer());
        if(response){
            sender.sendMessage(MessageBuilder.buildMessage(ConfigManager.accountSuccessCreate, player.getPlayer().getName()));
        }else{
            sender.sendMessage(MessageBuilder.buildMessage(ConfigManager.accountErrorCreate, player.getPlayer().getName()));
        }
    }

    @Subcommand("account set")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    public void onAccountSet(CommandSender sender, OnlinePlayer player, double amount){
        EconomyResponse response = pcs_economy.accountManager.getAccount(player.getPlayer()).setBalance(amount);
        if(response.type == EconomyResponse.ResponseType.SUCCESS){
            sender.sendMessage(MessageBuilder.buildMessage(ConfigManager.accountSuccessSet, player.getPlayer().getName(), amount));
        }else{
            sender.sendMessage(MessageBuilder.buildMessage(ConfigManager.accountErrorSet, player.getPlayer().getName()));
        }
    }

    @Subcommand("account add")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    public void onAccountAdd(CommandSender sender, OnlinePlayer player, double amount){
        EconomyResponse response = pcs_economy.accountManager.getAccount(player.getPlayer()).add(amount);
        String message;
        if(response.type == EconomyResponse.ResponseType.SUCCESS){
            sender.sendMessage(MessageBuilder.buildMessage(ConfigManager.accountSuccessAdd, player.getPlayer().getName(), amount));
        }else{
            sender.sendMessage(MessageBuilder.buildMessage(ConfigManager.accountErrorAdd, player.getPlayer().getName()));
        }
    }

    @Subcommand("account subtract")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    public void onAccountSubtract(CommandSender sender, OnlinePlayer player, double amount){
        EconomyResponse response = pcs_economy.accountManager.getAccount(player.getPlayer()).subtract(amount);
        if(response.type == EconomyResponse.ResponseType.SUCCESS){
            sender.sendMessage(MessageBuilder.buildMessage(ConfigManager.accountSuccessSubtract, player.getPlayer().getName(), amount));
        }else{
            sender.sendMessage(MessageBuilder.buildMessage(ConfigManager.accountErrorSubtract, player.getPlayer().getName()));
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
            fileContent += Material.values()[i].name() + "\n\n";

        }
        try{
            PrintWriter fileWriter = new PrintWriter("mats.txt");
            fileWriter.write(fileContent);
            fileWriter.close();
        }catch (IOException e){

        }

        sender.sendMessage(String.valueOf(Material.values().length));
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
                sender.sendMessage(MessageBuilder.buildMessage(ConfigManager.errorGeneral));
            }else{
                sender.sendMessage(MessageBuilder.buildMessage(ConfigManager.paymentErrorAccountNotExisting));
            }
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "");
        }
    }
}
