package com.z0cken.mc.economy;
/*
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.core.Database;
import com.z0cken.mc.economy.utils.DatabaseHelper;
import com.z0cken.mc.economy.utils.Pair;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.xml.crypto.Data;
import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@SuppressWarnings("Duplicates")
public class AccountManager {

    private HashMap<UUID, Account> accounts;
    private LoadingCache<UUID, Account> accounts2;

    //TODO
    public AccountManager(){
        accounts = new HashMap<>();
        accounts2 = CacheBuilder.newBuilder()
                .maximumSize(2000)
                .expireAfterAccess(1, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<UUID, Account>() {
                            @Override
                            public Account load(UUID uuid) throws Exception {
                                return getAccountFromDB(uuid);
                            }
                        }
                );
    }

    public boolean hasAccount(String playerName){
        Player p = PCS_Economy.pcs_economy.getServer().getPlayer(playerName);
        if(p != null){
            return hasAccount(p.getUniqueId());
        }else{
            return hasAccount(PCS_Economy.pcs_economy.getServer().getOfflinePlayer(playerName));
        }
    }

    public boolean hasAccount(OfflinePlayer player){
        return hasAccount(player.getUniqueId());
    }

    public boolean hasAccount(Player player){
        return hasAccount(player.getUniqueId());
    }

    public boolean hasAccount(UUID uuid){
        if(accounts.get(uuid) != null) return true;
        String query = "select count(*) from accounts where uuid = \'" + uuid.toString() + "\';";
        return getRowCount(query) > 0;
    }

    public Account getAccountFromDB(UUID uuid){
        String query = "select * from accounts where uuid = \'" + uuid.toString() + "\';";
        if(DatabaseHelper.checkConnection()){
            try(Connection con = Database.MAIN.getConnection();
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(query)){
                Account fromResultSet = getAccountFromResultSet(set, uuid);
                return fromResultSet;
            }catch (SQLException e){
                logError(Level.SEVERE, e.getMessage());
                return null;
            }
        }
        return null;
    }

    public List<Pair<String, Double>> getBalanceTop(){
        if(DatabaseHelper.checkConnection()){
            String query = "select uuid, balance from accounts order by balance desc limit 10;";
            try(Connection con = Database.MAIN.getConnection();
                Statement stmt = con.createStatement();
                ResultSet set = stmt.executeQuery(query)){
                List<Pair<String, Double>> resultSet = new ArrayList<>();
                int i = 0;
                while(set.next()){
                    if(i == 10) break;
                    String playerName = PCS_Economy.pcs_economy.getServer().getOfflinePlayer(UUID.fromString(set.getString("uuid"))).getName();
                    double balance = set.getDouble("balance");
                    resultSet.add(new Pair(playerName, balance));
                    i++;
                }
                return resultSet;
            }catch (SQLException e){
                logError(Level.SEVERE, e.getMessage());
                return null;
            }
        }
        return null;
    }

    public Account getAccountFromDB(String playerName){
        Player p = PCS_Economy.pcs_economy.getServer().getPlayer(playerName);
        if(p != null){
            return getAccountFromDB(p.getUniqueId());
        }
        return null;
    }

    public Account getAccountFromDB(Player player){
        return getAccountFromDB(player.getUniqueId());
    }

    public Account getAccountFromDB(OfflinePlayer offlinePlayer){
        return getAccount(offlinePlayer.getUniqueId());
    }

    public Account getAccount(UUID uuid){
        Account account = accounts2.getUnchecked(uuid);
        if(account != null) return account;
        return getAccountFromDB(uuid);
    }

    public Account getAccount(String playerName){
        Player p = PCS_Economy.pcs_economy.getServer().getPlayer(playerName);
        Account account;
        if(p != null){
            account = getAccount(p.getUniqueId());
        }else{
            account = getAccount(PCS_Economy.pcs_economy.getServer().getOfflinePlayer(playerName).getUniqueId());
        }
        return account;
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
        String query = "insert into accounts (uuid, balance) values (?, ?);";
        if(DatabaseHelper.checkConnection()){
            try(Connection con = Database.MAIN.getConnection();
                PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)){
                stmt.setString(1, player.getUniqueId().toString());
                stmt.setDouble(2, 0);
                stmt.execute();
                try(ResultSet set = stmt.getGeneratedKeys()){
                    if(set.rowInserted()){
                        return true;
                    }
                    if(set.next()){
                        logError(Level.FINE, "TEST");
                        int pk = set.getInt(1);
                        AccountHolder holder = new AccountHolder(player.getUniqueId());
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
        Player p = PCS_Economy.pcs_economy.getServer().getPlayer(playerName);
        if(p != null){
            deleteAccount(p.getUniqueId());
            removeAccountFromMap(p.getUniqueId());
            return true;
        }
        return false;
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
                return response1;
            }
            EconomyResponse response2 = receiver.add(amount);
            if(response2.type == EconomyResponse.ResponseType.FAILURE){
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
                AccountHolder holder;
                holder = new AccountHolder(uuid);
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

    public int getCurrentAccounts(){
        return accounts.size();
    }
}*/
