package com.z0cken.mc.economy.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.contexts.OnlinePlayer;
import com.z0cken.mc.core.Database;
import com.z0cken.mc.economy.Account;
import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.economy.utils.DatabaseHelper;
import com.z0cken.mc.economy.utils.MessageHelper;
import com.z0cken.mc.economy.utils.Transaction;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("pay")
public class PayCommand extends BaseCommand {

    @Dependency
    private PCS_Economy pcs_economy;

    @Default
    @CommandPermission("pcs.economy.user")
    @CommandCompletion("@cPlayers")
    public void onPay(CommandSender sender, String receiver, double amount, @Optional String payReason){
        if(sender instanceof Player){
            Player p = (Player)sender;
            Player rp = pcs_economy.getServer().getPlayer(receiver);
            double rounded = MessageHelper.roundToTwoDecimals(amount);
            Account from = pcs_economy.accountManager.getAccount(sender.getName());
            Account to = pcs_economy.accountManager.getAccount(receiver);
            if(to != null){
                EconomyResponse response = payHandler(sender, from, to, rounded);
                if(response.type == EconomyResponse.ResponseType.SUCCESS){
                    p.spigot().sendMessage(pcs_economy.getMessageBuilder()
                            .define("AMOUNT", String.valueOf(rounded))
                            .define("PLAYER", receiver)
                            .build(ConfigManager.paymentSuccessSender));
                    if(rp != null){
                        pcs_economy.getServer().getPlayer(receiver).spigot().sendMessage(pcs_economy.getMessageBuilder()
                                .define("AMOUNT", String.valueOf(rounded))
                                .define("PLAYER", p.getName())
                                .build(ConfigManager.paymentSuccessReceiver));
                    }
                    DatabaseHelper.addToTransactionDeque(new Transaction(from, to, rounded, payReason));
                }else{
                    p.spigot().sendMessage(pcs_economy.getMessageBuilder().build(response.errorMessage));
                }
            }else{
                p.spigot().sendMessage(pcs_economy.getMessageBuilder().build(ConfigManager.paymentErrorAccountNotExisting));
            }
        }
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
