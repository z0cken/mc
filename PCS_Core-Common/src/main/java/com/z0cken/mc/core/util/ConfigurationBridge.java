package com.z0cken.mc.core.util;

import java.util.List;

public interface ConfigurationBridge {

    int getInt(String path);

    double getDouble(String path);

    long getLong(String path);

    boolean getBoolean(String path);

    String getString(String path);

    List<String> getStringList(String path);

}
