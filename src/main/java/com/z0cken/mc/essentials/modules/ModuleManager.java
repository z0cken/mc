package com.z0cken.mc.essentials.modules;


import com.z0cken.mc.essentials.PCS_Essentials;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class ModuleManager {

    private static final Map<String, Class<? extends Module>> MODULES = Map.of(
        "chat", ModuleChat.class,
        "snowball", ModuleSnowball.class,
        "compass", ModuleCompass.class
    );

    private static final Collection<Module> activeModules = new ArrayList<>();

    private ModuleManager() {}

    public static void loadModules() {

        for(Map.Entry<String, Class<? extends Module>> entry : MODULES.entrySet()) {
            boolean isEnabled = PCS_Essentials.getInstance().getConfig().getBoolean("modules." + entry.getKey() + ".enabled");
            boolean isRunning = false;

            Iterator<Module> iterator = activeModules.iterator();
            while (iterator.hasNext()) {
                Module module = iterator.next();
                if(entry.getValue().isInstance(module)) {
                    if(isEnabled) {
                        module.reload();
                        isRunning = true;
                    } else {
                        module.disable();
                        iterator.remove();
                    }
                }
            }

            if(isEnabled && !isRunning) {
                try {
                    activeModules.add(entry.getValue().getDeclaredConstructor(String.class).newInstance(entry.getKey()));
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
