package com.z0cken.mc.economy.config;

import com.z0cken.mc.economy.PCS_Economy;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    public static FileConfiguration config = null;

    public static String messagePrefix = null;

    public static String mysqlAddress = null;
    public static String mysqlPort = null;
    public static String mysqlUsername = null;
    public static String mysqlPassword = null;
    public static String mysqlDatabase = null;

    public static String currencySingular = null;
    public static String currencyPlural = null;
    public static String currencySymbol = null;

    public static String permissionAdmin = null;
    public static String permissionUser = null;

    public static String errorGeneral = null;
    public static String successGeneral = null;

    public static String accountSuccessBalanceSelf = null;
    public static String accountErrorBalanceSelf = null;
    public static String accountSuccessBalanceOther = null;
    public static String accountErrorBalanceOther = null;
    public static String accountSuccessDelete = null;
    public static String accountErrorDelete = null;
    public static String accountSuccessClear = null;
    public static String accountErrorClear = null;
    public static String accountSuccessCreate = null;
    public static String accountErrorCreate = null;
    public static String accountSuccessSet = null;
    public static String accountErrorSet = null;
    public static String accountSuccessAdd = null;
    public static String accountErrorAdd = null;
    public static String accountSuccessSubtract = null;
    public static String accountErrorSubtract = null;

    public static String paymentError = null;
    public static String paymentErrorNegativeValue = null;
    public static String paymentErrorNotEnoughFunds = null;
    public static String paymentErrorAccountNotExisting = null;
    public static String paymentErrorSelf = null;

    public static String paymentSuccessSender = null;
    public static String paymentSuccessReceiver = null;

    public static String errorNoPermission = null;

    public static void loadConfig(){
        config = PCS_Economy.pcs_economy.getConfig();

        mysqlAddress = config.getString("mysql.server");
        mysqlPort = config.getString("mysql.port");
        mysqlUsername = config.getString("mysql.username");
        mysqlPassword = config.getString("mysql.password");
        mysqlDatabase = config.getString("mysql.database");

        messagePrefix = config.getString("economy.messagePrefix");

        currencySingular = config.getString("economy.currency.currencySingular");
        currencyPlural = config.getString("economy.currency.currencyPlural");
        currencySymbol = config.getString("economy.currency.currencySymbol");

        permissionAdmin = config.getString("economy.permissions.admin");
        permissionUser = config.getString("economy.permissions.user");

        errorGeneral = config.getString("economy.messages.error.errorGeneral");
        successGeneral = config.getString("economy.messages.success.successGeneral");

        accountSuccessBalanceSelf = config.getString("economy.messages.action.accounts.success.accountSuccessBalanceSelf");
        accountErrorBalanceSelf = config.getString("economy.messages.action.accounts.error.accountErrorBalanceSelf");
        accountSuccessBalanceOther = config.getString("economy.messages.action.accounts.success.accountSuccessBalanceOther");
        accountErrorBalanceOther = config.getString("economy.messages.action.accounts.error.accountErrorBalanceOther");
        accountSuccessDelete = config.getString("economy.messages.action.accounts.success.accountSuccessDelete");
        accountErrorDelete = config.getString("economy.messages.action.accounts.error.accountErrorDelete");
        accountSuccessClear = config.getString("economy.messages.action.accounts.success.accountSuccessClear");
        accountErrorClear = config.getString("economy.messages.action.accounts.error.accountErrorClear");
        accountSuccessCreate = config.getString("economy.messages.action.accounts.success.accountSuccessCreate");
        accountErrorCreate = config.getString("economy.messages.action.accounts.error.accountErrorCreate");
        accountSuccessSet = config.getString("economy.messages.action.accounts.success.accountSuccessSet");
        accountErrorSet = config.getString("economy.messages.action.accounts.error.accountErrorSet");
        accountSuccessAdd = config.getString("economy.messages.action.accounts.success.accountSuccessAdd");
        accountErrorAdd = config.getString("economy.messages.action.accounts.error.accountErrorAdd");
        accountSuccessSubtract = config.getString("economy.messages.action.accounts.success.accountSuccessSubtract");
        accountErrorSubtract = config.getString("economy.messages.action.accounts.success.accountErrorSubtract");

        paymentError = config.getString("economy.messages.payment.error.paymentError");
        paymentErrorNegativeValue = config.getString("economy.messages.payment.error.paymentErrorNegativeValue");
        paymentErrorNotEnoughFunds = config.getString("economy.messages.payment.error.paymentErrorNotEnoughFunds");
        paymentErrorAccountNotExisting = config.getString("economy.messages.payment.error.paymentErrorAccountNotExisting");
        paymentErrorSelf = config.getString("economy.messages.payment.error.paymentErrorSelf");

        paymentSuccessSender = config.getString("economy.messages.payment.success.paymentSuccessSender");
        paymentSuccessReceiver = config.getString("economy.messages.payment.success.paymentSuccessReceiver");

        errorNoPermission = config.getString("economy.messages.permission.errorNoPermission");
    }

    public static void saveConfig(){

    }
}
