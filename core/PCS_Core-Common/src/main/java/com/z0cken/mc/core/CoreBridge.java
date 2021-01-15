package com.z0cken.mc.core;

public final class CoreBridge {

    private static ICore plugin;

    static void init(ICore plugin) {
        if(CoreBridge.plugin != null) throw new IllegalStateException("CoreBridge already initialized");
        CoreBridge.plugin = plugin;

        //Setup Tables
        FriendsAPI.setupTables();
        Shadow.setupTables();
    }

    public static ICore getPlugin() {
        if(plugin == null) throw new IllegalStateException("CoreBridge not yet initialized");
        return plugin;
    }

}
