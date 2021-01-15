package com.z0cken.mc.economy.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.contexts.OnlinePlayer;
import com.z0cken.mc.economy.Account;
import com.z0cken.mc.economy.CacheAccountManager;
import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.economy.utils.MessageHelper;
import com.z0cken.mc.economy.utils.Pair;
import net.md_5.bungee.api.chat.BaseComponent;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
            Account account = pcs_economy.accountManager.getAccount(p);
            if(account != null){
                double rounded = MessageHelper.roundToTwoDecimals(account.getBalance());
                p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                        .define("PREFIX", ConfigManager.messagePrefix)
                        .define("AMOUNT", String.valueOf(rounded))
                        .build(ConfigManager.accountSuccessBalanceSelf));
            }
        }
    }

    @Subcommand("balance|b")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@cPlayers")
    @Description("Kontostand eines anderen Spielers einsehen.")
    public void onBalancePlayer(CommandSender sender, String player){
        if(sender instanceof Player){
            Player p = (Player)sender;
            Account account = pcs_economy.accountManager.getAccount(player);
            if(account != null){
                double rounded = MessageHelper.roundToTwoDecimals(account.getBalance());
                p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                        .define("AMOUNT", String.valueOf(rounded))
                        .define("PLAYER", player)
                        .build(ConfigManager.accountSuccessBalanceOther));
            }else{
                p.spigot().sendMessage(pcs_economy.getMessageBuilder().build(ConfigManager.paymentErrorAccountNotExisting));
            }
        }
    }

    @Subcommand("top")
    @CommandPermission("pcs.economy.top")
    @Description("Balancetop-Command")
    public void onBalanceTop(CommandSender sender){
        if(sender instanceof Player){
            Player player = (Player)sender;
            List<Pair<String, Double>> set = PCS_Economy.pcs_economy.accountManager.getBalanceTop();
            String finalString = MessageHelper.convertBcToString(pcs_economy.getMessageBuilder().build(ConfigManager.accountBalanceTop)) + "\n";
            for(int i = 0; i < set.size(); i++){
                Pair<String, Double> pair = set.get(i);
                finalString += MessageHelper.convertBcToString(
                        pcs_economy.getMessageBuilder()
                                .define("N", String.valueOf(i + 1))
                                .define("PLAYER", pair.getLeft())
                                .define("AMOUNT", String.valueOf(MessageHelper.roundToTwoDecimals(pair.getRight())))
                                .build(ConfigManager.accountBalanceEntry)) + "\n";
            }
            player.sendMessage(finalString);
        }
    }

    @Subcommand("account clear")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@cPlayers")
    public void onClear(CommandSender sender, String player){
        if(sender instanceof Player){
            Player p = (Player)sender;
            Account account = pcs_economy.accountManager.getAccount(player);
            if(account != null){
                EconomyResponse response = account.clearBalance();
                if(response.type == EconomyResponse.ResponseType.FAILURE){
                    p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                            .define("PLAYER", player)
                            .build(ConfigManager.accountErrorClear));
                }else{
                    p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                            .define("PLAYER", player)
                            .build(ConfigManager.accountSuccessClear));
                }
            }else{
                p.spigot().sendMessage(pcs_economy.getMessageBuilder().build(ConfigManager.paymentErrorAccountNotExisting));
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
    @CommandCompletion("@cPlayers")
    public void onAccountSet(CommandSender sender, String player, double amount){
        if(sender instanceof Player){
            Player p = (Player)sender;
            double rounded = MessageHelper.roundToTwoDecimals(amount);
            Account account = pcs_economy.accountManager.getAccount(player);
            if(account != null){
                EconomyResponse response = account.setBalance(rounded);
                if(response.type == EconomyResponse.ResponseType.SUCCESS){
                    p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                            .define("PLAYER", player)
                            .define("AMOUNT", String.valueOf(rounded))
                            .build(ConfigManager.accountSuccessSet));
                }else{
                    p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                            .define("PLAYER", player)
                            .build(ConfigManager.accountErrorSet));
                }
            }else{
                p.spigot().sendMessage(pcs_economy.getMessageBuilder().build(ConfigManager.paymentErrorAccountNotExisting));
            }
        }
    }

    @Subcommand("account add")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@cPlayers")
    public void onAccountAdd(CommandSender sender, String player, double amount){
        if(sender instanceof Player){
            Player p = (Player)sender;
            double rounded = MessageHelper.roundToTwoDecimals(amount);
            Account account = pcs_economy.accountManager.getAccount(player);
            if(account != null){
                EconomyResponse response = account.add(rounded);
                if(response.type == EconomyResponse.ResponseType.SUCCESS){
                    p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                            .define("PLAYER", player)
                            .define("AMOUNT", String.valueOf(rounded))
                            .build(ConfigManager.accountSuccessAdd));
                }else{
                    p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                            .define("PLAYER", player)
                            .build(ConfigManager.accountErrorAdd));
                }
            }else{
                p.spigot().sendMessage(pcs_economy.getMessageBuilder().build(ConfigManager.paymentErrorAccountNotExisting));
            }
        }
    }

    @Subcommand("account subtract")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@cPlayers")
    public void onAccountSubtract(CommandSender sender, String player, double amount){
        if(sender instanceof Player){
            Player p = (Player)sender;
            double rounded = MessageHelper.roundToTwoDecimals(amount);
            Account account = pcs_economy.accountManager.getAccount(player);
            if(account != null){
                EconomyResponse response = account.subtract(rounded);
                if(response.type == EconomyResponse.ResponseType.SUCCESS){
                    p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                            .define("PLAYER", player)
                            .define("AMOUNT", String.valueOf(rounded))
                            .build(ConfigManager.accountSuccessSubtract));
                }else{
                    p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                            .define("PLAYER", player)
                            .build(ConfigManager.accountErrorSubtract));
                }
            }else{
                p.spigot().sendMessage(PCS_Economy.messageBuilder.build(ConfigManager.paymentErrorAccountNotExisting));
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

    @Subcommand("reload|r")
    @CommandPermission("pcs.economy.admin")
    public void onEconomyReload(CommandSender sender){
        if(sender instanceof Player){
            Player p = (Player)sender;
            ConfigManager.loadConfig();
            p.sendMessage("Config neu geladen!");
        }
    }
}
