package com.z0cken.mc.economy;

import com.z0cken.mc.economy.config.ConfigManager;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import sun.security.krb5.Config;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class AccountManager {

    private Connection conn;

    public AccountManager(Connection conn){
        this.conn = conn;
    }

    public boolean hasAccount(String playerName){
        String query = "select count(*) from accounts where username = \'" + playerName + "\';";
        return getRowCount(query) > 0;
    }

    public boolean hasAccount(OfflinePlayer player){
        String query = "select count(*) from accounts where username = \'" + player.getName() + "\';";
        return getRowCount(query) > 0;
    }

    public boolean hasAccount(Player player){
        String query = "select count(*) from accounts where username = \'" + player.getName() + "\';";
        return getRowCount(query) > 0;
    }

    public boolean hasAccount(UUID uuid){
        String query = "select count(*) from accounts where uuid = \'" + uuid.toString() + "\';";
        return getRowCount(query) > 0;
    }

    public Account getAccount(UUID uuid){
        String query = "select * from accounts where uuid = \'" + uuid.toString() + "\';";
        try(Statement stmt = conn.createStatement(); ResultSet set = stmt.executeQuery(query)){
            return getAccountFromResultSet(set, uuid);
        }catch (SQLException e){
            logError(Level.SEVERE, e.getMessage());
            return null;
        }
    }

    public Account getAccount(String playerName){
        String query = "select * from accounts where username = \'" + playerName + "\';";
        try(Statement stmt = conn.createStatement(); ResultSet set = stmt.executeQuery(query)){
            return getAccountFromResultSet(set, null);
        }catch (SQLException e){
            logError(Level.SEVERE, e.getMessage());
            return null;
        }
    }

    public Account getAccount(Player player){
        return getAccount(player.getName());
    }

    public Account getAccount(OfflinePlayer offlinePlayer){
        return getAccount(offlinePlayer.getUniqueId());
    }

    public boolean createAccount(String playerName){
        return createAccount(PCS_Economy.pcs_economy.getServer().getPlayer(playerName));
    }

    public boolean createAccount(OfflinePlayer player) {
        String query = "insert into accounts (username, uuid, balance) values (?, ?, ?);";
        try(PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setString(1, player.getName());
            stmt.setString(2, player.getUniqueId().toString());
            stmt.setDouble(3, 0);
            stmt.execute();
            return true;
        }catch (SQLException e){
            logError(Level.SEVERE, e.getMessage());
            return false;
        }
    }

    public boolean deleteAccount(String playerName){
        String query = "delete from accounts where username = \'" + playerName + "\';";
        return executeSimpleStatement(query);
    }

    public boolean deleteAccount(UUID uuid){
        String query = "delete from accounts where uuid = \'" + uuid.toString() + "\';";
        return executeSimpleStatement(query);
    }

    public boolean deleteAccount(OfflinePlayer player){
        return deleteAccount(player.getName());
    }

    public boolean deleteAccount(Player player){
        return deleteAccount(player.getName());
    }

    public EconomyResponse transferMoney(Account sender, Account receiver, double amount){
        if(amount <= sender.getBalance()){
            EconomyResponse response1 = sender.subtract(amount);
            if(response1.type == EconomyResponse.ResponseType.FAILURE){
                PCS_Economy.pcs_economy.getLogger().info("Konnte nicht subtrahieren");
                return response1;
            }
            EconomyResponse response2 = receiver.add(amount);
            if(response2.type == EconomyResponse.ResponseType.FAILURE){
                PCS_Economy.pcs_economy.getLogger().info("Konnte nicht addieren");
                return response2;
            }
            return new EconomyResponse(amount, sender.getBalance(), EconomyResponse.ResponseType.SUCCESS, ConfigManager.paymentSuccessReceiver);
        }else{
            return new EconomyResponse(amount, sender.getBalance(), EconomyResponse.ResponseType.FAILURE, ConfigManager.paymentErrorNotEnoughFunds);
        }
    }

    public boolean updateAccountBalance(Account account){
        String query = "update accounts set balance = ? where accountID = ? ;";
        try(PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setDouble(1, account.getBalance());
            stmt.setInt(2, account.getAccountID());
            stmt.execute();
            return true;
        }catch (SQLException e){
            logError(Level.SEVERE, e.getMessage());
            return false;
        }
    }

    private int getRowCount(String query){
        try(Statement stmt = conn.createStatement(); ResultSet set = stmt.executeQuery(query)){
            while(set.next()){
                return set.getInt(1);
            }
        }catch (SQLException e){
            logError(Level.SEVERE, e.getMessage());
            return 0;
        }
        return 0;
    }

    private boolean executeSimpleStatement(String query){
        try(Statement stmt = conn.createStatement()){
            return stmt.execute(query);
        }catch (SQLException e){
            logError(Level.SEVERE, e.getMessage());
            return false;
        }
    }

    private Account getAccountFromResultSet(ResultSet set, UUID uuid){
        Account account = null;
        try{
            while(set.next()){
                if(uuid == null){
                    uuid = UUID.fromString(set.getString("uuid"));
                }
                OfflinePlayer op = PCS_Economy.pcs_economy.getServer().getOfflinePlayer(uuid);
                AccountHolder holder;
                if(op == null){
                    holder = new AccountHolder(set.getString("username"));
                }else{
                    holder = new AccountHolder(op);
                }

                holder.accountHolderUUID = uuid;
                if(holder.getName() == null){
                    holder.accountHolderPN = set.getString("username");
                }
                PCS_Economy.pcs_economy.getLogger().info("AccountHolderName: " + holder.getName());
                PCS_Economy.pcs_economy.getLogger().info("AccountHolderUUID: " + holder.getUUID());
                account = new Account(holder, set.getDouble("balance"), set.getInt("accountID"));
            }
            set.close();
        }catch (SQLException e){
            logError(Level.SEVERE, e.getMessage());
            return null;
        }
        return account;
    }

    private void logError(Level level, String message){
        PCS_Economy.pcs_economy.getLogger().log(level, message);
    }
}
