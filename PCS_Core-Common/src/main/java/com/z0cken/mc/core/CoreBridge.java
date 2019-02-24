package com.z0cken.mc.core;

final class CoreBridge {

    private static ICore plugin;

    static void init(ICore plugin) {
        if(CoreBridge.plugin != null) throw new IllegalStateException("CoreBridge already initialized");
        CoreBridge.plugin = plugin;

        //Setup Tables
        FriendsAPI.setupTables();
        Shadow.setupTables();
    }

    static ICore getPlugin() {
        if(plugin == null) throw new IllegalStateException("CoreBridge not yet initialized");
        return plugin;
    }

}
