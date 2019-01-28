package com.z0cken.mc.core;

import java.io.File;
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
}