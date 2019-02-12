package com.z0cken.mc.economy.config;

import com.z0cken.mc.economy.PCS_Economy;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    public static FileConfiguration config = null;

    public static String messagePrefix = null;
    public static int pushInterval = 0;

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

    public static String tradeSelectionBuyPrice = null;
    public static String tradeSelectionSellPrice = null;

    public static String tradeSellTitleSingle = null;
    public static String tradeSellTitleStack = null;
    public static String tradeSellTitleInv = null;
    public static String tradeSellDescriptionSingle = null;
    public static String tradeSellDescriptionStack = null;
    public static String tradeSellDescriptionInv = null;
    public static String tradeSell = null;

    public static String tradeBuyTitleSingle = null;
    public static String tradeBuyTitleStack = null;
    public static String tradeBuyTitleInv = null;
    public static String tradeBuyDescriptionSingle = null;
    public static String tradeBuyDescriptionStack = null;
    public static String tradeBuyDescriptionInv = null;
    public static String tradeBuy = null;

    public static String tradeInformation = null;
    public static String tradeQuantity = null;
    public static String tradeCost = null;
    public static String tradePurchase = null;
    public static String tradeBack = null;
    public static String tradeNotEnoughFunds = null;
    public static String tradeNotEnoughItems = null;
    public static String tradeInventoryFull = null;

    public static void loadConfig(){
        config = PCS_Economy.pcs_economy.getConfig();

        mysqlAddress = config.getString("mysql.server");
        mysqlPort = config.getString("mysql.port");
        mysqlUsername = config.getString("mysql.username");
        mysqlPassword = config.getString("mysql.password");
        mysqlDatabase = config.getString("mysql.database");

        messagePrefix = config.getString("economy.messagePrefix");
        pushInterval = config.getInt("economy.pushInterval");

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

        tradeSelectionBuyPrice = config.getString("economy.gui.selection.buyPrice");
        tradeSelectionSellPrice = config.getString("economy.gui.selection.sellPrice");

        tradeSellTitleSingle = config.getString("economy.gui.trade.sell.titleSingle");
        tradeSellTitleStack = config.getString("economy.gui.trade.sell.titleStack");
        tradeSellTitleInv = config.getString("economy.gui.trade.sell.titleInv");
        tradeSellDescriptionSingle = config.getString("economy.gui.trade.sell.descriptionSingle");
        tradeSellDescriptionStack = config.getString("economy.gui.trade.sell.descriptionStack");
        tradeSellDescriptionInv = config.getString("economy.gui.trade.sell.descriptionInv");
        tradeSell = config.getString("economy.gui.trade.sell.sell");

        tradeBuyTitleSingle = config.getString("economy.gui.trade.buy.titleSingle");
        tradeBuyTitleStack = config.getString("economy.gui.trade.buy.titleStack");
        tradeBuyTitleInv = config.getString("economy.gui.trade.buy.titleInv");
        tradeBuyDescriptionSingle = config.getString("economy.gui.trade.buy.descriptionSingle");
        tradeBuyDescriptionStack = config.getString("economy.gui.trade.buy.descriptionStack");
        tradeBuyDescriptionInv = config.getString("economy.gui.trade.buy.descriptionInv");
        tradeBuy = config.getString("economy.gui.trade.buy.purchase");

        tradeInformation = config.getString("economy.gui.trade.information");
        tradeQuantity = config.getString("economy.gui.trade.quantity");
        tradeCost = config.getString("economy.gui.trade.cost");
        tradePurchase = config.getString("economy.gui.trade.purchase");
        tradeBack = config.getString("economy.gui.trade.back");
        tradeNotEnoughFunds = config.getString("economy.gui.trade.notEnoughFunds");
        tradeNotEnoughItems = config.getString("economy.gui.trade.notEnoughItems");
        tradeInventoryFull = config.getString("economy.gui.trade.inventoryFull");
    }
}
