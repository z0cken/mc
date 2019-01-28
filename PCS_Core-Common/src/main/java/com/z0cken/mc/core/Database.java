package com.z0cken.mc.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

@SuppressWarnings("unused")
public enum Database {

    MAIN(true);

    private static ICore plugin = CoreBridge.getPlugin();
    private HikariDataSource dataSource;
    private final boolean crucial;

    Database(boolean crucial) {
        this.crucial = crucial;
    }

    void connect() {
        if(isConnected()) disconnect();

        try {
            HikariConfig hikariConfig = new HikariConfig(plugin.getDataFolder() + "//hikari.properties");
            File childProperties = new File(plugin.getDataFolder(),name().toLowerCase() + ".properties");
            if(childProperties.exists()) hikariConfig.setDataSourceProperties(new HikariConfig(childProperties.getPath()).getDataSourceProperties());
            else plugin.getLogger().warning("Database config not found: " + childProperties.getPath());

            dataSource = new HikariDataSource(hikariConfig);
        } catch (RuntimeException e) {
            e.printStackTrace();
            failFast();
        }

        if(dataSource.isReadOnly()) plugin.getLogger().warning("Connection to \"" + name().toLowerCase() + "\" is read-only");
    }

    boolean isConnected() {
        return dataSource != null && dataSource.isRunning();
    }

    public Connection getConnection() throws SQLException {
        if (!isConnected()) connect();

        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to connect to \"" + name().toLowerCase() + "\"");
            plugin.getLogger().severe("SQLException: " + e.getMessage());
            plugin.getLogger().severe("SQLState: " + e.getSQLState());
            plugin.getLogger().severe("VendorError: " + e.getErrorCode());
            failFast();
            throw e;
        }
    }

    void disconnect() {
        if(isConnected()) dataSource.close();
    }

    private void failFast() {
        if (crucial) {
            plugin.getLogger().severe("Database \"" + name().toLowerCase() + "\" is crucial -> Server shutting down");
            plugin.stopServer("Database Error");
        }
    }
}
