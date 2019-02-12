package com.z0cken.mc.economy.utils;

import com.z0cken.mc.core.Database;
import com.z0cken.mc.economy.Account;
import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.config.ConfigManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.stream.IntStream;

/*
* Bin noch am schauen wie man das am besten löst
* */
public class DatabaseHelper {
    private static final ConcurrentLinkedDeque<Account> deque = new ConcurrentLinkedDeque<>();

    static {
        new BukkitRunnable(){
            @Override
            public void run(){
                push();
            }
        }.runTaskTimerAsynchronously(PCS_Economy.pcs_economy, 100, ConfigManager.pushInterval * 20);
    }

    public static void createTable(){
        try(Connection con = Database.MAIN.getConnection();
            Statement stmt = con.createStatement()){
            String query =
                    "CREATE TABLE IF NOT EXISTS accounts" +
                            " (accountID INT AUTO_INCREMENT PRIMARY KEY," +
                            " username VARCHAR(30) NOT NULL UNIQUE," +
                            " uuid VARCHAR(36) NOT NULL UNIQUE," +
                            " balance DOUBLE DEFAULT 0 NOT NULL );";
            stmt.execute(query);
        }catch (SQLException e){
            PCS_Economy.pcs_economy.getLogger().log(Level.SEVERE, e.getMessage());
        }
    }

    static void push(){
        if(!deque.isEmpty()){
            String query = "UPDATE accounts SET balance = ? WHERE accountID = ? ;";
            try(Connection con = Database.MAIN.getConnection(); PreparedStatement stmt = con.prepareStatement(query)){
                PCS_Economy.pcs_economy.getLogger().info("Beginning push to database");
                while(!deque.isEmpty()){
                    Account acc = deque.peek();
                    stmt.setDouble(1, acc.getBalance());
                    stmt.setInt(2, acc.getAccountID());
                    stmt.addBatch();
                    deque.pop();
                }
                int[] sum = stmt.executeBatch();
                PCS_Economy.pcs_economy.getLogger().fine("[PUSH] UPDATE " + IntStream.of(sum).sum());
                PCS_Economy.pcs_economy.getLogger().info("Push completed");
            }catch (SQLException e){
                PCS_Economy.pcs_economy.getLogger().log(Level.SEVERE, e.getMessage());
            }
        }
    }

    public static void addToDeque(Account account){
        deque.add(account);
    }

    public static boolean existsInDeque(Account account){
        return deque.contains(account);
    }

    //TODO
    public static void addToDequeDEBUG(){
        if(checkConnection()){
            try(Connection con = Database.MAIN.getConnection(); PreparedStatement stmt = con.prepareStatement("SELECT * FROM accounts")){

            }catch (SQLException e){
                PCS_Economy.pcs_economy.getLogger().log(Level.SEVERE, e.getMessage());
            }
        }
    }

    public static boolean checkConnection(){
        try(Connection con = Database.MAIN.getConnection()){
            if(con.isValid(40)){
                return true;
            }
            return false;
        }catch (SQLException e){
            PCS_Economy.pcs_economy.getLogger().log(Level.SEVERE, e.getMessage());
            return false;
        }
    }
}
