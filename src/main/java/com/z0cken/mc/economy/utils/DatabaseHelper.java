package com.z0cken.mc.economy.utils;

import com.z0cken.mc.economy.PCS_Economy;
import com.z0cken.mc.economy.config.ConfigManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedDeque;

/*
* Bin noch am schauen wie man das am besten löst
* */
public class DatabaseHelper {
    private static Connection connection;
    private static final ConcurrentLinkedDeque<PreparedStatement> deque = new ConcurrentLinkedDeque<>();

    static {
        new BukkitRunnable(){
            @Override
            public void run(){
                push();
            }
        }.runTaskTimerAsynchronously(PCS_Economy.pcs_economy, 100, ConfigManager.pushInterval * 20);
    }

    static void push(){
        connection = PCS_Economy.pcs_economy.connectToDB();


    }
}
