package com.z0cken.mc.essentials.modules;

import com.z0cken.mc.core.util.MessageBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.regex.Pattern;

public class ModuleHelp extends Module implements CommandExecutor {

    private static Map<String, HelpMenu> menus = new HashMap<>();
    private static MessageBuilder messageBuilder = MessageBuilder.DEFAULT;

    ModuleHelp(String configPath) {
        super(configPath);
    }

    @Override
    protected void load() {
        menus.clear();
        getConfig().getKeys(false).forEach(key -> {
            registerCommand(key);
            menus.put(key, new HelpMenu(key, getConfig().getConfigurationSection(key)));
        });

        menus.values().forEach(HelpMenu::build);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        HelpMenu menu = find(command.getName().toLowerCase() + "." + String.join(".", args));

        if(menu != null) {
            menu.getText().forEach(sender.spigot()::sendMessage);
        }

        return false;
    }

    static HelpMenu find(String path) {
        String[] nodes = path.split(Pattern.quote("."));

        return find(menus.get(nodes[0]), nodes, 0);
    }

    private static HelpMenu find(HelpMenu menu, String[] nodes, int index) {
        if(index < nodes.length - 1) {
            ++index;
            for(HelpMenu child : menu.children) {
                if(child.hasName(nodes[index])) return find(child, nodes, index);
            }
        } else {
            return menu;
        }

        return null;
    }

    static class HelpMenu {

        Set<String> alias = new HashSet<>();
        List<String> textStrings = new ArrayList<>();
        List<BaseComponent[]> text = new ArrayList<>();
        BaseComponent[] description;
        Set<HelpMenu> children = new HashSet<>();

        HelpMenu(String key, ConfigurationSection section) {

            alias.add(key.toLowerCase());
            alias.addAll(section.getStringList("alias"));

            description = MessageBuilder.DEFAULT.build(section.getString("description"));

            messageBuilder = messageBuilder.define(section.getCurrentPath() + "-C", new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + String.join(" ", section.getCurrentPath().split(Pattern.quote(".")))))
                                             .define(section.getCurrentPath() + "-H", new HoverEvent(HoverEvent.Action.SHOW_TEXT, getDescription()));


            textStrings.add(section.getString("title"));
            textStrings.addAll(section.getStringList("text"));

            for(String s : section.getKeys(false)) {
                switch (s) {
                    case "alias":
                    case "title":
                    case "description":
                    case "text":
                        break;
                    default:
                        final ConfigurationSection childSection = section.getConfigurationSection(s);
                        final HelpMenu childMenu = new HelpMenu(s, childSection);
                        children.add(childMenu);
                        break;
                }
            }
        }

        boolean hasName(String name) {
            return alias.contains(name.toLowerCase());
        }

        public BaseComponent[] getDescription() {
            return description;
        }

        public List<BaseComponent[]> getText() {
            return text;
        }

        public void build() {
            textStrings.forEach(s -> text.add(messageBuilder.build(s)));
            textStrings = null;
            children.forEach(HelpMenu::build);
        }
    }

}
