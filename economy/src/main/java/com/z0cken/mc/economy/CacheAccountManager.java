package com.z0cken.mc.economy;

import com.google.common.cache.*;
import com.z0cken.mc.core.Database;
import com.z0cken.mc.economy.config.ConfigManager;
import com.z0cken.mc.economy.exceptions.AccountNotFoundException;
import com.z0cken.mc.economy.utils.DatabaseHelper;
import com.z0cken.mc.economy.utils.Pair;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@SuppressWarnings("Duplicates")
public class CacheAccountManager {
    private LoadingCache<UUID, Account> accounts;

    public CacheAccountManager(){
        accounts = CacheBuilder.newBuilder()
                .maximumSize(2000)
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<UUID, Account>() {
                            @Override
                            public Account load(UUID uuid) throws Exception {
                                Account account = getAccountFromDB(uuid);
                                if(account != null){
                                    return account;
                                }else{
                                    throw new AccountNotFoundException();
                                }
                            }
                        }
                );
    }

    public Account getAccountFromDB(UUID uuid){
        if(!hasAccount(uuid)){
            OfflinePlayer p = PCS_Economy.pcs_economy.getServer().getOfflinePlayer(uuid);
            if(p.hasPlayedBefore()) {
                createAccount(p);
            }
        }
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

    public boolean createAccount(String playerName){
        Player p = PCS_Economy.pcs_economy.getServer().getPlayer(playerName);
        if(p != null){
            return createAccount(p);
        }else{
            return createAccount(PCS_Economy.pcs_economy.getServer().getOfflinePlayer(playerName));
        }
    }

    public boolean createAccount(OfflinePlayer player) {
        if(player == null) return false;
        return createAccount(player.getUniqueId());
    }

    public boolean createAccount(UUID uuid){
        if (uuid == null) return false;
        if (hasAccount(uuid)) return false;
        String query = "insert into accounts (uuid, balance) values (?, ?);";
        if(DatabaseHelper.checkConnection()){
            try(Connection con = Database.MAIN.getConnection();
                PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)){
                stmt.setString(1, uuid.toString());
                stmt.setDouble(2, ConfigManager.config.getDouble("economy.currency.initialBalance"));
                stmt.execute();
                try(ResultSet set = stmt.getGeneratedKeys()){
                    if(set.rowInserted()){
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
        Map<UUID, Account> map = accounts.asMap();
        if(map.get(uuid) != null) return true;
        String query = "SELECT COUNT(*) FROM accounts WHERE uuid = \'" + uuid.toString() + "\';";
        return getRowCount(query) > 0;
    }

    public Account getAccount(String playerName){
        if(playerName == null) return null;
        Player p = PCS_Economy.pcs_economy.getServer().getPlayer(playerName);
        if(p != null){
            return getAccount(p.getUniqueId());
        }else{
            return getAccount(PCS_Economy.pcs_economy.getServer().getOfflinePlayer(playerName));
        }
    }

    public Account getAccount(Player player){
        if(player == null) return null;
        return getAccount(player.getUniqueId());
    }

    public Account getAccount(OfflinePlayer player){
        if(player == null) return null;
        return getAccount(player.getUniqueId());
    }

    public Account getAccount(UUID uuid){
        if(uuid == null) return null;
        try {
            Account account = accounts.get(uuid);
            if(account == null && !hasAccount(uuid)){
                createAccount(uuid);
                return accounts.get(uuid);
            }
            return accounts.get(uuid);
        } catch (Exception e) {
            return null;
        }
    }

    private void logError(Level level, String message){
        PCS_Economy.pcs_economy.getLogger().log(level, message);
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

    public boolean updateAccountBalance(Account account){
        if(!DatabaseHelper.existsInDeque(account)){
            DatabaseHelper.addToDeque(account);
        }
        return true;
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

    public boolean deleteAccount(String playerName){
        if(playerName != null){
            Player p = PCS_Economy.pcs_economy.getServer().getPlayer(playerName);
            if(p != null){
                return deleteAccount(p.getUniqueId());
            }else{
                return deleteAccount(PCS_Economy.pcs_economy.getServer().getOfflinePlayer(playerName));
            }
        }
        return false;
    }

    public boolean deleteAccount(OfflinePlayer player){
        if(player != null){
            return deleteAccount(player.getUniqueId());
        }
        return false;
    }

    public boolean deleteAccount(Player player){
        if(player != null){
            return deleteAccount(player.getUniqueId());
        }
        return false;
    }

    public boolean deleteAccount(UUID uuid){
        String query = "delete from accounts where uuid = \'" + uuid.toString() + "\';";
        if(hasAccount(uuid)){
            Map<UUID, Account> map = accounts.asMap();
            if(map.containsKey(uuid)){
                accounts.invalidate(map.get(uuid));
            }
            if(executeSimpleStatement(query)) return true;
        }
        return false;
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
}
