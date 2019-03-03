package com.z0cken.mc.shout.config;

import com.z0cken.mc.shout.PCS_Shout;
import com.z0cken.mc.shout.Shout;
import com.z0cken.mc.shout.ShoutGroup;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ShoutManager {
    private static File configFile;
    private static FileConfiguration config;
    private static String bypassPermission;
    private static int defaultCooldown;
    public static final HashMap<Integer, ShoutGroup> SHOUTS = new HashMap<>();
    public static final ArrayList<String> MAIN_COOLDOWN = new ArrayList<>();

    public static void init(){
    }

    @SuppressWarnings("Duplicates")
    public static void load(){
        if(SHOUTS.size() > 0){
            SHOUTS.clear();
        }
        configFile = new File(PCS_Shout.getInstance().getDataFolder() + "/shouts.yml");
        if(!configFile.exists()){
            PCS_Shout.getInstance().saveResource("shouts.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        float defaultVolume = (float)config.getDouble("shouts.settings.defaultVolume");
        float defaultPitch = (float)config.getDouble("shouts.settings.defaultPitch");
        bypassPermission = config.getString("shouts.permissions.bypassCooldown");
        defaultCooldown = config.getInt("shouts.settings.defaultCooldown");

        ConfigurationSection groupsSection = config.getConfigurationSection("shouts.groups");
        for (String groupKey : groupsSection.getKeys(false)){
            ConfigurationSection groupSection = groupsSection.getConfigurationSection(groupKey);
            int groupID = groupSection.getInt("id");
            String groupName = groupSection.getString("name");
            String groupPermission = groupSection.contains("permission") ? groupSection.getString("permission") : null;
            String groupMaterialString = groupSection.getString("material");
            double groupPrice = groupSection.contains("price") ? groupSection.getDouble("price") : -1;
            Material groupMaterial = null;
            if(Material.valueOf(groupMaterialString) != null){
                groupMaterial = Material.valueOf(groupMaterialString);
            }
            ShoutGroup group = new ShoutGroup(groupID, groupPrice, groupName, groupPermission, groupMaterial);
            ConfigurationSection shoutsSection = groupSection.getConfigurationSection("shouts");
            for(String shoutKey : shoutsSection.getKeys(false)){
                ConfigurationSection shoutSection = shoutsSection.getConfigurationSection(shoutKey);
                int shoutID = shoutSection.getInt("id");
                String shoutName = shoutSection.getString("name");
                String shoutPermission = shoutSection.contains("permission") ? shoutSection.getString("permission") : null;
                String shoutPath = shoutSection.getString("path");
                String shoutMaterialString = shoutSection.getString("material");
                float volume = shoutSection.contains("volume") ? (float)shoutSection.getDouble("volume") : defaultVolume;
                float pitch = shoutSection.contains("pitch") ? (float)shoutSection.getDouble("pitch") : defaultPitch;
                int timeInSeconds = shoutSection.contains("time") ? shoutSection.getInt("time") : -1;
                int cooldown = shoutSection.contains("cooldown") ? shoutSection.getInt("cooldown") : defaultCooldown;
                Material shoutMaterial = null;
                if(Material.valueOf(shoutMaterialString) != null){
                    shoutMaterial = Material.valueOf(shoutMaterialString);
                }
                double shoutPrice = shoutSection.contains("price") ? shoutSection.getDouble("price") : -1;
                Shout shout = new Shout(groupID, shoutID, shoutName, shoutPath, shoutPermission, shoutMaterial, shoutPrice, volume, pitch, timeInSeconds, cooldown);
                group.addShout(shoutID, shout);
            }
            SHOUTS.put(groupID, group);
        }
    }

    public static String getBypassPermission(){
        return bypassPermission;
    }

    public static int getDefaultCooldown(){
        return defaultCooldown;
    }
}
