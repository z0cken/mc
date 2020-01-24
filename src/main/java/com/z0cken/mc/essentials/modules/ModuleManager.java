package com.z0cken.mc.essentials.modules;


import com.google.common.collect.ImmutableMap;
import com.z0cken.mc.essentials.PCS_Essentials;

import java.util.*;
import java.util.logging.Level;

public class ModuleManager {

    private static final Map<String, Class<? extends Module>> MODULES = ImmutableMap.<String, Class<? extends Module>>builder()
            .put("chat", ModuleChat.class)
            .put("snowball", ModuleSnowball.class)
            .put("compass", ModuleCompass.class)
            .put("erosion", ModuleErosion.class)
            .put("discover", ModuleDiscover.class)
            .put("award", ModuleAward.class)
            .put("help", ModuleHelp.class)
            .put("wild", ModuleWild.class)
            .put("entity", ModuleEntity.class)
            .put("various", ModuleVarious.class)
            .put("minion", ModuleMinion.class)
            .put("donate", ModuleDonate.class)
            .put("inactive", ModuleInactive.class)
            .put("nether", ModuleNether.class)
            .put("elytra", ModuleElytra.class)
        .build();

    private static final Set<Module> activeModules = new HashSet<>();

    private ModuleManager() {}

    public static void loadModules() {

        for(Map.Entry<String, Class<? extends Module>> entry : MODULES.entrySet()) {
            boolean isEnabled = PCS_Essentials.getInstance().getConfig().getBoolean("modules." + entry.getKey());
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
                } catch (Throwable e) {
                    PCS_Essentials.getInstance().getLogger().log(Level.SEVERE, "Failed to enable module '" + entry.getKey() + "'", e);
                }
            }
        }
    }

    public static Set<Module> getActiveModules() {
        return Collections.unmodifiableSet(activeModules);
    }

    public static void shutdown() {
        getActiveModules().forEach(Module::onDisable);
    }
}
