package com.z0cken.mc.core;

import com.z0cken.mc.core.util.ConfigurationBridge;
import com.z0cken.mc.core.util.ConfigurationType;
import com.z0cken.mc.core.util.CoreTask;

import java.io.File;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;

public interface ICore {

    default void init() {
        CoreBridge.init(this);
    }

    default void shutdown() {
        Stream.of(Database.values()).forEach(Database::disconnect);
    }

    File getDataFolder();

    Logger getLogger();

    void stopServer(String reason);

    int schedule(CoreTask task);

    void cancelTask(int id);

    boolean isOnline(UUID uuid);

    ConfigurationBridge getConfigBridge(ConfigurationType type);

}