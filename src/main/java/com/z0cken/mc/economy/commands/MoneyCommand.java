package com.z0cken.mc.economy.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.contexts.OnlinePlayer;
import com.z0cken.mc.economy.Account;
import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.config.ConfigManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.PrintWriter;

@CommandAlias("m0ney")
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
            p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                    .define("PREFIX", ConfigManager.messagePrefix)
                    .define("AMOUNT", String.valueOf(pcs_economy.accountManager.getAccount(sender.getName()).getBalance()))
                    .build(ConfigManager.accountSuccessBalanceSelf));
        }
    }

    @Subcommand("balance")
    @CommandPermission("pcs.economy.admin")
    @CommandCompletion("@players")
    public void onBalancePlayer(CommandSender sender, OnlinePlayer player){
        if(sender instanceof Player){
            Player p = (Player)sender;
            p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                    .define("AMOUNT", String.valueOf(pcs_economy.accountManager.getAccount(player.getPlayer()).getBalance()))
                    .define("PLAYER", player.getPlayer().getName())
                    .build(ConfigManager.accountSuccessBalanceOther));
        }
    }

    @Subcommand("pay")
    @CommandPermission("pcs.economy.user")
    @CommandCompletion("@players")
    public void onPay(CommandSender sender, OnlinePlayer receiver, int amount){
        if(sender instanceof Player){
            Player p = (Player)sender;
            Account from = pcs_economy.accountManager.getAccount(sender.getName());
            Account to = pcs_economy.accountManager.getAccount(receiver.getPlayer().getName());
            EconomyResponse response = payHandler(sender, from, to, amount);
            if(response.type == EconomyResponse.ResponseType.SUCCESS){
                p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                        .define("AMOUNT", String.valueOf(amount))
                        .define("PLAYER", receiver.getPlayer().getName())
                        .build(ConfigManager.paymentSuccessSender));
                receiver.getPlayer().spigot().sendMessage(pcs_economy.getMessageBuilder()
                        .define("AMOUNT", String.valueOf(amount))
                        .define("PLAYER", p.getName())
                        .build(ConfigManager.paymentSuccessReceiver));
            }else{
                p.spigot().sendMessage(pcs_economy.getMessageBuilder().build(response.errorMessage));
            }
        }
    }

    @Subcommand("payo")
    @CommandPermission("pcs.economy.admin")
    public void onPay(CommandSender sender, String receiver, int amount){
        if(sender instanceof Player){
            Player p = (Player)sender;
            p.sendMessage("NOCH NICHT BEREIT MANN! HAB GEDULD ALTER!!!1!11elf");
        }
        /*if(sender instanceof Player){
            Player p = (Player)sender;
            Account from = pcs_economy.accountManager.getAccount(sender.getName());
            Account to = pcs_economy.accountManager.getAccount(receiver);
            EconomyResponse response = payHandler(sender, from, to, amount);
        }
        if(response.type == EconomyResponse.ResponseType.SUCCESS){
            sender.sendMessage(MessageBuilder.buildMessage(true, ConfigManager.paymentSuccessSender, sender.getName(), receiver, amount, 0));
        }else{
            sender.sendMessage(MessageBuilder.buildMessage(true, response.errorMessage));
        }*/
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
    public void onAccountSet(CommandSender sender, OnlinePlayer player, int amount){
        if(sender instanceof Player){
            Player p = (Player)sender;
            EconomyResponse response = pcs_economy.accountManager.getAccount(player.getPlayer()).setBalance(amount);
            if(response.type == EconomyResponse.ResponseType.SUCCESS){
                p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                        .define("PLAYER", player.getPlayer().getName())
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
    public void onAccountAdd(CommandSender sender, OnlinePlayer player, int amount){
        if(sender instanceof Player){
            Player p = (Player)sender;
            EconomyResponse response = pcs_economy.accountManager.getAccount(player.getPlayer()).add(amount);
            if(response.type == EconomyResponse.ResponseType.SUCCESS){
                p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                        .define("PLAYER", player.getPlayer().getName())
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
    public void onAccountSubtract(CommandSender sender, OnlinePlayer player, int amount){
        if(sender instanceof Player){
            Player p = (Player)sender;
            EconomyResponse response = pcs_economy.accountManager.getAccount(player.getPlayer()).subtract(amount);
            if(response.type == EconomyResponse.ResponseType.SUCCESS){
                p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                        .define("PLAYER", player.getPlayer().getName())
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

    private EconomyResponse payHandler(CommandSender sender, Account senderAccount, Account receiverAccount, int amount){
        if(sender instanceof Player){
            Player p = (Player)sender;
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
                    p.spigot().sendMessage(pcs_economy.getMessageBuilder().build(ConfigManager.errorGeneral));
                }else{
                    p.spigot().sendMessage(pcs_economy.getMessageBuilder().build(ConfigManager.paymentErrorAccountNotExisting));
                }
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "");
            }
        }
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "You are no player");
    }
}
