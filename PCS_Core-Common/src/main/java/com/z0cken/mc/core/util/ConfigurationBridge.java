package com.z0cken.mc.core.util;

import java.util.List;

public abstract class ConfigurationBridge {

    public abstract int getInt(String path);

    public abstract String getString(String path);

    public abstract List<String> getStringList(String path);

}
