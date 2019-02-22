package com.z0cken.mc.economy.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.contexts.OnlinePlayer;
import com.z0cken.mc.economy.Account;
import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.economy.utils.MessageHelper;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("pay")
public class PayCommand extends BaseCommand {

    @Dependency
    private PCS_Economy pcs_economy;

    @Default
    @CommandPermission("pcs.economy.user")
    @CommandCompletion("@players")
    public void onPay(CommandSender sender, OnlinePlayer receiver, double amount){
        if(sender instanceof Player){
            Player p = (Player)sender;
            double rounded = MessageHelper.roundToTwoDecimals(amount);
            Account from = pcs_economy.accountManager.getAccount(sender.getName());
            Account to = pcs_economy.accountManager.getAccount(receiver.getPlayer().getName());
            EconomyResponse response = payHandler(sender, from, to, rounded);
            if(response.type == EconomyResponse.ResponseType.SUCCESS){
                p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                        .define("AMOUNT", String.valueOf(rounded))
                        .define("PLAYER", receiver.getPlayer().getName())
                        .build(ConfigManager.paymentSuccessSender));
                receiver.getPlayer().spigot().sendMessage(pcs_economy.getMessageBuilder()
                        .define("AMOUNT", String.valueOf(rounded))
                        .define("PLAYER", p.getName())
                        .build(ConfigManager.paymentSuccessReceiver));
            }else{
                p.spigot().sendMessage(pcs_economy.getMessageBuilder().build(response.errorMessage));
            }
        }
    }

    @Subcommand("o")
    @CommandPermission("pcs.economy.admin")
    public void onPay(CommandSender sender, String receiver, double amount){
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

    private EconomyResponse payHandler(CommandSender sender, Account senderAccount, Account receiverAccount, double amount){
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
