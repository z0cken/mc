package com.z0cken.mc.economy.utils;

import com.z0cken.mc.economy.Account;

public class Transaction {
    private Account account1;
    private Account account2;
    private double amount;
    private String reason;

    public Transaction(Account account1, Account account2, double amount, String reason){
        this.account1 = account1;
        this.account2 = account2;
        this.amount = amount;
        this.reason = reason;
    }

    public Account getAccount1(){
        return this.account1;
    }

    public Account getAccount2(){
        return this.account2;
    }

    public double getAmount(){
        return this.amount;
    }

    public String getReason() { return this.reason; }
}
