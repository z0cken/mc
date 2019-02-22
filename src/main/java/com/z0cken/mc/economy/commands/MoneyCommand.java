package com.z0cken.mc.economy.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.contexts.OnlinePlayer;
import com.z0cken.mc.economy.Account;
import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.economy.utils.MessageHelper;
import net.md_5.bungee.api.chat.BaseComponent;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

@CommandAlias("m0ney|m")
public class MoneyCommand extends BaseCommand {

    @Dependency
    private PCS_Economy pcs_economy;

    public MoneyCommand(){

    }

    @Default
    @CommandPermission("pcs.economy.user")
    public void onBalance(CommandSender sender){
        if(sender instanceof Player){
            Player p = (Player)sender;
            double rounded = MessageHelper.roundToTwoDecimals(pcs_economy.accountManager.getAccount(sender.getName()).getBalance());
            p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                    .define("PREFIX", ConfigManager.messagePrefix)
                    .define("AMOUNT", String.valueOf(rounded))
                    .build(ConfigManager.accountSuccessBalanceSelf));

        }
    }

    @Subcommand("balance|b")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    @Description("Kontostand eines anderen Spielers einsehen.")
    public void onBalancePlayer(CommandSender sender, OnlinePlayer player){
        if(sender instanceof Player){
            Player p = (Player)sender;
            double rounded = MessageHelper.roundToTwoDecimals(pcs_economy.accountManager.getAccount(player.getPlayer()).getBalance());
            p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                    .define("AMOUNT", String.valueOf(rounded))
                    .define("PLAYER", player.getPlayer().getName())
                    .build(ConfigManager.accountSuccessBalanceOther));
        }
    }

    @Subcommand("account clear")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    public void onClear(CommandSender sender, OnlinePlayer player){
        if(sender instanceof Player){
            Player p = (Player)sender;
            EconomyResponse response = pcs_economy.accountManager.getAccount(player.getPlayer()).clearBalance();
            if(response.type == EconomyResponse.ResponseType.FAILURE){
                p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                        .define("PLAYER", player.getPlayer().getName())
                        .build(ConfigManager.accountErrorClear));
            }else{
                p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                        .define("PLAYER", player.getPlayer().getName())
                        .build(ConfigManager.accountSuccessClear));
            }
        }
    }

    @Subcommand("account delete")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    public void onAccountDelete(CommandSender sender, OnlinePlayer player){
        if(sender instanceof Player){
            Player p = (Player)sender;
            boolean response = pcs_economy.accountManager.deleteAccount(player.getPlayer());
            if(response){
                p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                        .define("PLAYER", player.getPlayer().getName())
                        .build(ConfigManager.accountSuccessDelete));
            }else{
                p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                        .define("PLAYER", player.getPlayer().getName())
                        .build(ConfigManager.accountErrorClear));
            }
        }
    }

    @Subcommand("account create")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    public void onAccountCreate(CommandSender sender, OnlinePlayer player){
        if(sender instanceof Player){
            Player p = (Player)sender;
            boolean response = pcs_economy.accountManager.createAccount(player.getPlayer());
            if(response){
                p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                        .define("PLAYER", player.getPlayer().getName())
                        .build(ConfigManager.accountSuccessCreate));
            }else{
                p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                        .define("PLAYER", player.getPlayer().getName())
                        .build(ConfigManager.accountErrorCreate));
            }
        }
    }

    @Subcommand("account set")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    public void onAccountSet(CommandSender sender, OnlinePlayer player, double amount){
        if(sender instanceof Player){
            Player p = (Player)sender;
            double rounded = MessageHelper.roundToTwoDecimals(amount);
            pcs_economy.getServer().getLogger().info(String.valueOf(rounded));
            EconomyResponse response = pcs_economy.accountManager.getAccount(player.getPlayer()).setBalance(rounded);
            if(response.type == EconomyResponse.ResponseType.SUCCESS){
                p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                        .define("PLAYER", player.getPlayer().getName())
                        .define("AMOUNT", String.valueOf(rounded))
                        .build(ConfigManager.accountSuccessSet));
            }else{
                p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                        .define("PLAYER", player.getPlayer().getName())
                        .build(ConfigManager.accountErrorSet));
            }
        }
    }

    @Subcommand("account add")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    public void onAccountAdd(CommandSender sender, OnlinePlayer player, double amount){
        if(sender instanceof Player){
            Player p = (Player)sender;
            double rounded = MessageHelper.roundToTwoDecimals(amount);
            EconomyResponse response = pcs_economy.accountManager.getAccount(player.getPlayer()).add(rounded);
            if(response.type == EconomyResponse.ResponseType.SUCCESS){
                p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                        .define("PLAYER", player.getPlayer().getName())
                        .define("AMOUNT", String.valueOf(rounded))
                        .build(ConfigManager.accountSuccessAdd));
            }else{
                p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                        .define("PLAYER", player.getPlayer().getName())
                        .build(ConfigManager.accountErrorAdd));
            }
        }
    }

    @Subcommand("account subtract")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    public void onAccountSubtract(CommandSender sender, OnlinePlayer player, double amount){
        if(sender instanceof Player){
            Player p = (Player)sender;
            double rounded = MessageHelper.roundToTwoDecimals(amount);
            EconomyResponse response = pcs_economy.accountManager.getAccount(player.getPlayer()).subtract(rounded);
            if(response.type == EconomyResponse.ResponseType.SUCCESS){
                p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                        .define("PLAYER", player.getPlayer().getName())
                        .define("AMOUNT", String.valueOf(rounded))
                        .build(ConfigManager.accountSuccessSubtract));
            }else{
                p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                        .define("PLAYER", player.getPlayer().getName())
                        .build(ConfigManager.accountErrorSubtract));
            }
        }
    }

    @Subcommand("debug matlist")
    @CommandPermission("pcs.economy.debug")
    public void onDebugMatList(CommandSender sender){
        if(sender instanceof Player){
            Player p = (Player)sender;
            String fileContent = "";
            for(int i = 0; i < Material.values().length; i++){
                fileContent += Material.values()[i].name() + "\n";
            }
            try{
                PrintWriter fileWriter = new PrintWriter("mats.txt");
                fileWriter.write(fileContent);
                fileWriter.close();
            }catch (IOException e){
                p.spigot().sendMessage(pcs_economy.getMessageBuilder().build(ConfigManager.errorGeneral));
            }
        }
    }
}
