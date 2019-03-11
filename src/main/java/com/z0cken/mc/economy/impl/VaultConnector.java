package com.z0cken.mc.economy.impl;

import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.config.ConfigManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class VaultConnector implements Economy {

    public VaultConnector () {}

    public boolean isEnabled() {
        return PCS_Economy.pcs_economy != null && PCS_Economy.pcs_economy.isEnabled();
    }

    public String getName() {
        return "PCS_Economy";
    }

    public boolean hasBankSupport() {
        return false;
    }

    public int fractionalDigits() {
        return 2;
    }

    public String format(double v) {
        return String.valueOf(v);
    }

    public String currencyNamePlural() {
        return ConfigManager.currencyPlural;
    }

    public String currencyNameSingular() {
        return ConfigManager.currencySingular;
    }

    public boolean hasAccount(String s) {
        return PCS_Economy.pcs_economy.accountManager.hasAccount(s);
    }

    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return PCS_Economy.pcs_economy.accountManager.hasAccount(offlinePlayer);
    }

    public boolean hasAccount(String s, String s1) {
        return PCS_Economy.pcs_economy.accountManager.hasAccount(s);
    }

    public boolean hasAccount(OfflinePlayer offlinePlayer, String s) {
        return PCS_Economy.pcs_economy.accountManager.hasAccount(offlinePlayer);
    }

    public double getBalance(String s) {
        return PCS_Economy.pcs_economy.accountManager.getAccount(s).getBalance();
    }

    public double getBalance(OfflinePlayer offlinePlayer) {
        return PCS_Economy.pcs_economy.accountManager.getAccount(offlinePlayer).getBalance();
    }

    public double getBalance(String s, String s1) {
        return PCS_Economy.pcs_economy.accountManager.getAccount(s).getBalance();
    }

    public double getBalance(OfflinePlayer offlinePlayer, String s) {
        return PCS_Economy.pcs_economy.accountManager.getAccount(offlinePlayer).getBalance();
    }

    public boolean has(String s, double v) {
        return PCS_Economy.pcs_economy.accountManager.getAccount(s).has(v);
    }

    public boolean has(OfflinePlayer offlinePlayer, double v) {
        return PCS_Economy.pcs_economy.accountManager.getAccount(offlinePlayer).has(v);
    }

    public boolean has(String s, String s1, double v) {
        return PCS_Economy.pcs_economy.accountManager.getAccount(s).has(v);
    }

    public boolean has(OfflinePlayer offlinePlayer, String s, double v) {
        return PCS_Economy.pcs_economy.accountManager.getAccount(offlinePlayer).has(v);
    }

    public EconomyResponse withdrawPlayer(String s, double v) {
        return PCS_Economy.pcs_economy.accountManager.getAccount(s).subtract(v);
    }

    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double v) {
        return PCS_Economy.pcs_economy.accountManager.getAccount(offlinePlayer).subtract(v);
    }

    public EconomyResponse withdrawPlayer(String s, String s1, double v) {
        return PCS_Economy.pcs_economy.accountManager.getAccount(s).subtract(v);
    }

    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        return PCS_Economy.pcs_economy.accountManager.getAccount(offlinePlayer).subtract(v);
    }

    public EconomyResponse depositPlayer(String s, double v) {
        return PCS_Economy.pcs_economy.accountManager.getAccount(s).add(v);
    }

    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double v) {
        return PCS_Economy.pcs_economy.accountManager.getAccount(offlinePlayer).add(v);
    }

    public EconomyResponse depositPlayer(String s, String s1, double v) {
        return PCS_Economy.pcs_economy.accountManager.getAccount(s).add(v);
    }

    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        return PCS_Economy.pcs_economy.accountManager.getAccount(offlinePlayer).add(v);
    }

    public EconomyResponse createBank(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "not implemented");
    }

    public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "not implemented");
    }

    public EconomyResponse deleteBank(String s) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "not implemented");
    }

    public EconomyResponse bankBalance(String s) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "not implemented");
    }

    public EconomyResponse bankHas(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "not implemented");
    }

    public EconomyResponse bankWithdraw(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "not implemented");
    }

    public EconomyResponse bankDeposit(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "not implemented");
    }

    public EconomyResponse isBankOwner(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "not implemented");
    }

    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "not implemented");
    }

    public EconomyResponse isBankMember(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "not implemented");
    }

    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "not implemented");
    }

    public List<String> getBanks() {
        return null;
    }

    public boolean createPlayerAccount(String s) {
        return PCS_Economy.pcs_economy.accountManager.createAccount(s);
    }

    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        return PCS_Economy.pcs_economy.accountManager.createAccount(offlinePlayer);
    }

    public boolean createPlayerAccount(String s, String s1) {
        return false;
    }

    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
        return false;
    }
}
