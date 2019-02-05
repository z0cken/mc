package com.z0cken.mc.essentials.modules;

import com.z0cken.mc.essentials.PCS_Essentials;
import com.z0cken.mc.persona.PCS_Persona;
import com.z0cken.mc.persona.Persona;
import com.z0cken.mc.util.MessageBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.chat.Chat;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.regex.Pattern;


public class ModuleChat extends Module implements Listener {

    private static boolean instantiated = false;
    private static Chat chat = null;

    private static final Pattern PATTERN_URL = Pattern.compile("^(?:(https?)://)([-\\w_.]{2,}\\.[a-z]{2,4})([/?]\\S*)?$");
    private String FORMAT;
    private boolean LOG_CONSOLE;

    public ModuleChat(String configPath) {
        super(configPath);
        if(instantiated) throw new IllegalStateException(getClass().getName() + " cannot be instantiated twice!");
        instantiated = true;

        this.load();
        setupVaultChat();
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        BaseComponent[] message = parseMessage(event.getMessage());
        event.getRecipients().forEach(r -> r.spigot().sendMessage((BaseComponent[]) ArrayUtils.addAll(getFormat(event.getPlayer()), message)));
        if(LOG_CONSOLE) Bukkit.getServer().getLogger().info(event.getPlayer().getName() + " >" + new TextComponent(message).toPlainText());
    }

    private BaseComponent[] getFormat(Player player) {

        MessageBuilder builder = new MessageBuilder()
                .define("PLAYER", player.getName())
                .define("G", ChatColor.GOLD.toString());

        builder = builder.define("PREFIX", chat == null ? null : chat.getPlayerPrefix(player));

        Persona persona = PCS_Persona.getPersona(player);
        if(persona != null) {
            if(!persona.isGuest()) {
                builder = builder.define("MARK", " " + persona.getMark().getSymbol());
            }
            builder = builder.define("PERSONA", persona.getHoverEvent());
        }

        return builder.build(FORMAT);

    }

    private BaseComponent[] parseMessage(String message) {
        String[] msg = message.split("\\s+");
        ComponentBuilder builder = new ComponentBuilder(" ").color(ChatColor.GRAY);

        for(int i = 0; i < msg.length; i++) {
            builder.append(new TextComponent(msg[i]));
            if(i+1 != msg.length) builder.append(" ");
        }

        BaseComponent[] components = builder.create();
        parseLinks(components);
        parseMentions(components);
        return components;
    }

    private void parseMentions(BaseComponent... message) {
        for(int i = 0; i < message.length; i++) {
            if(!(message[i] instanceof TextComponent)) continue;

            TextComponent component = (TextComponent) message[i];
            Player player = Bukkit.getPlayerExact(component.getText());

            if(player != null) {

                Persona persona = PCS_Persona.getPersona(player);
                if(persona != null) component.setHoverEvent(persona.getHoverEvent());

                component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + player.getName()));
                component.setColor(ChatColor.AQUA);
                message[i] = component;

                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1.75F);
            }
        }
    }

    private static void parseLinks(BaseComponent... message) {
        for(int i = 0; i < message.length; i++) {
            if(!(message[i] instanceof TextComponent)) continue;

            TextComponent component = (TextComponent) message[i];
            String text = component.getText();

            if(PATTERN_URL.matcher(text).matches()) {
                component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, text));
                component.setText(text.replaceAll("^(https?)://", "").replaceAll("^(www\\.)?(pr0gramm.com)", ""));
                component.setColor(ChatColor.AQUA);
                message[i] = component;
            }
        }
    }

    @Override
    public void load() {
        FORMAT = config.getString("format");
        LOG_CONSOLE = config.getBoolean("log-console");
    }

    private void setupVaultChat() {
        if(Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Chat> rsp = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
            chat = rsp.getProvider();
        } else PCS_Essentials.getInstance().getLogger().warning("Failed to hook into Vault-Chat");
    }
}
