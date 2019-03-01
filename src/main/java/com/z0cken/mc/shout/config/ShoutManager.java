package com.z0cken.mc.shout.config;

import com.z0cken.mc.shout.PCS_Shout;
import com.z0cken.mc.shout.Shout;
import com.z0cken.mc.shout.ShoutGroup;
import com.z0cken.mc.shout.gui.menu.Menu;
import io.netty.util.internal.SuppressJava6Requirement;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ShoutManager {
    private static File configFile;
    private static FileConfiguration config;
    public static final HashMap<Integer, ShoutGroup> SHOUTS = new HashMap<>();

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
                Material shoutMaterial = null;
                if(Material.valueOf(shoutMaterialString) != null){
                    shoutMaterial = Material.valueOf(shoutMaterialString);
                }
                double shoutPrice = shoutSection.contains("price") ? shoutSection.getDouble("price") : -1;
                Shout shout = new Shout(shoutID, shoutName, shoutPath, shoutPermission, shoutMaterial, shoutPrice, volume, pitch);
                group.addShout(shoutID, shout);
            }
            SHOUTS.put(groupID, group);
        }
    }

    public static Menu getShoutMenu(Player player){

        return null;
    }
}
