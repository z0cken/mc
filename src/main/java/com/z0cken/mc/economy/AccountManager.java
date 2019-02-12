package com.z0cken.mc.economy;

import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.core.Database;
import com.z0cken.mc.economy.utils.DatabaseHelper;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.xml.crypto.Data;
import java.sql.*;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class AccountManager {

    private HashMap<UUID, Account> accounts;

    public AccountManager(){
        accounts = new HashMap<>();
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

    public Account getAccountFromDB(UUID uuid){
        String query = "select * from accounts where uuid = \'" + uuid.toString() + "\';";
        if(DatabaseHelper.checkConnection()){
            try(Connection con = Database.MAIN.getConnection();
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(query)){
                return getAccountFromResultSet(set, uuid);
            }catch (SQLException e){
                logError(Level.SEVERE, e.getMessage());
                return null;
            }
        }
        return null;
    }

    public Account getAccountFromDB(String playerName){
        String query = "select * from accounts where username = \'" + playerName + "\';";
        if(DatabaseHelper.checkConnection()){
            try(Connection con = Database.MAIN.getConnection();
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(query)){
                return getAccountFromResultSet(set, null);
            }catch (SQLException e){
                logError(Level.SEVERE, e.getMessage());
            }
        }
        return null;
    }

    public Account getAccountFromDB(Player player){
        return getAccountFromDB(player.getName());
    }

    public Account getAccountFromDB(OfflinePlayer offlinePlayer){
        return getAccount(offlinePlayer.getUniqueId());
    }

    public Account getAccount(UUID uuid){
        return accounts.get(uuid);
    }

    public Account getAccount(String playerName){
        return getAccount(PCS_Economy.pcs_economy.getServer().getPlayer(playerName).getUniqueId());
    }

    public Account getAccount(Player player){
        return getAccount(player.getUniqueId());
    }

    public Account getAccount(OfflinePlayer player){
        return getAccount(player.getUniqueId());
    }

    public boolean createAccount(String playerName){
        return createAccount(PCS_Economy.pcs_economy.getServer().getPlayer(playerName));
    }

    public boolean createAccount(OfflinePlayer player) {
        String query = "insert into accounts (username, uuid, balance) values (?, ?, ?);";
        if(DatabaseHelper.checkConnection()){
            try(Connection con = Database.MAIN.getConnection();
                PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)){
                stmt.setString(1, player.getName());
                stmt.setString(2, player.getUniqueId().toString());
                stmt.setDouble(3, 0);
                stmt.execute();
                try(ResultSet set = stmt.getGeneratedKeys()){
                    if(set.next()){
                        logError(Level.FINE, "TEST");
                        int pk = set.getInt(1);
                        AccountHolder holder = new AccountHolder(player);
                        Account account = new Account(holder, 0, pk);
                        accounts.put(player.getUniqueId(), account);
                        return true;
                    }
                }catch (SQLException e){
                    logError(Level.SEVERE, e.getMessage());
                    return false;
                }
                return false;
            }catch (SQLException e){
                logError(Level.SEVERE, e.getMessage());
            }
        }
        return false;
    }

    public void addAccountFromPlayer(Player p){
        if(hasAccount(p)){
            Account account = getAccountFromDB(p);
            accounts.put(p.getUniqueId(), account);
        }else{
            createAccount(p);
        }
    }

    public boolean deleteAccount(String playerName){
        String query = "delete from accounts where username = \'" + playerName + "\';";
        if(hasAccount(playerName)){
            executeSimpleStatement(query);
            removeAccountFromMap(playerName);
        }
        return true;
    }

    public boolean deleteAccount(UUID uuid){
        String query = "delete from accounts where uuid = \'" + uuid.toString() + "\';";
        if(hasAccount(uuid)){
            executeSimpleStatement(query);
            removeAccountFromMap(uuid);
            return true;
        }
        return false;
    }

    public boolean deleteAccount(OfflinePlayer player){
        return deleteAccount(player.getName());
    }

    public boolean deleteAccount(Player player){
        return deleteAccount(player.getName());
    }

    public void removeAccountFromMap(Player p){
        accounts.remove(p.getUniqueId());
    }

    public void removeAccountFromMap(UUID uuid){
        accounts.remove(uuid);
    }

    public void removeAccountFromMap(String playerName){
        accounts.remove(PCS_Economy.pcs_economy.getServer().getPlayer(playerName).getUniqueId());
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
        if(!DatabaseHelper.existsInDeque(account)){
            DatabaseHelper.addToDeque(account);
            return true;
        }
        return true;
    }

    private int getRowCount(String query){
        if(DatabaseHelper.checkConnection()){
            try(Connection con = Database.MAIN.getConnection();
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(query)){
                while(set.next()){
                    return set.getInt(1);
                }
            }catch (SQLException e){
                logError(Level.SEVERE, e.getMessage());
            }
        }
        return 0;
    }

    private boolean executeSimpleStatement(String query){
        if(DatabaseHelper.checkConnection()){
            try(Connection con = Database.MAIN.getConnection();
                Statement stmt = con.createStatement()){
                boolean bool = stmt.execute(query);
                logError(Level.FINE, "CHECK" + String.valueOf(bool));
                return bool;
            }catch (SQLException e){
                logError(Level.SEVERE, e.getMessage());
            }
        }
        return false;
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
                holder = new AccountHolder(op);

                PCS_Economy.pcs_economy.getLogger().info("AccountHolderName: " + holder.getName());
                PCS_Economy.pcs_economy.getLogger().info("AccountHolderUUID: " + holder.getUUID());
                account = new Account(holder, set.getDouble("balance"), set.getInt("accountID"));
            }
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
