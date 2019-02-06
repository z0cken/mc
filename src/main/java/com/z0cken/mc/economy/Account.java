package com.z0cken.mc.economy;

import com.z0cken.mc.economy.config.ConfigManager;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

public class Account {
    private double balance = 0;
    private AccountHolder holder = null;
    private int accountID = 0;
    private boolean isDirty = false;

    public Account(AccountHolder holder, double balance, int accountID){
        this.holder = holder;
        this.balance = balance;
        this.accountID = accountID;
    }

    public double getBalance(){
        return this.balance;
    }

    public int getAccountID(){
        return this.accountID;
    }

    public AccountHolder getHolder(){
        return this.holder;
    }

    public EconomyResponse add(double amount){
        this.isDirty = true;
        if(amount <= 0){
            return new EconomyResponse(amount, this.getBalance(), ResponseType.FAILURE, ConfigManager.paymentErrorNegativeValue);
        }
        this.balance += amount;
        return responseFailureOrGeneral(amount);
    }

    public EconomyResponse subtract(double amount){
        this.isDirty = true;
        if(amount < 0){
            return new EconomyResponse(0, this.getBalance(), ResponseType.FAILURE, ConfigManager.paymentErrorNegativeValue);
        }
        this.balance -= amount;
        return responseFailureOrGeneral(amount);
    }

    public EconomyResponse clearBalance(){
        this.balance = 0;
        return updateAccount(0);
    }

    public EconomyResponse setBalance(double amount){
        this.balance = amount;
        return updateAccount(amount);
    }

    public boolean has(double amount){
        return amount <= balance;
    }

    private boolean checkNegative(double amount){
        if(amount < 0){
            return false;
        }
        return true;
    }

    private EconomyResponse updateAccount(double amount){
        if(PCS_Economy.pcs_economy.accountManager.updateAccountBalance(this)){
            this.isDirty = true;
            return new EconomyResponse(amount, this.getBalance(), ResponseType.SUCCESS, ConfigManager.successGeneral);
        }
        this.isDirty = false;
        return new EconomyResponse(amount, this.getBalance(), ResponseType.FAILURE, ConfigManager.errorGeneral);
    }

    private EconomyResponse responseFailureOrGeneral(double amount){
        if(updateAccount(amount).type == ResponseType.FAILURE) {
            this.isDirty = false;
            return new EconomyResponse(amount, this.getBalance(), ResponseType.FAILURE, ConfigManager.errorGeneral);
        }
        this.isDirty = true;
        return new EconomyResponse(0, this.getBalance(), ResponseType.SUCCESS, ConfigManager.successGeneral);
    }
}
