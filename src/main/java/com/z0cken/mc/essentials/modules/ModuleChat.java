package com.z0cken.mc.essentials.modules;

import com.z0cken.mc.core.persona.Persona;
import com.z0cken.mc.core.persona.PersonaAPI;
import com.z0cken.mc.core.util.MessageBuilder;
import com.z0cken.mc.essentials.PCS_Essentials;
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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.List;
import java.util.regex.Pattern;


public class ModuleChat extends Module implements Listener {

    private static Chat chat = null;

    private static final Pattern PATTERN_URL = Pattern.compile("^(?:(https?)://)([-\\w_.]{2,}\\.[a-z]{2,4})([/?]\\S*)?$");
    private String FORMAT, JOIN, QUIT;
    private boolean LOG_CONSOLE;

    private List<String> hoverGuest = getConfig().getStringList("hover-event.guest");
    private List<String> hoverMember = getConfig().getStringList("hover-event.member");

    public ModuleChat(String configPath) {
        super(configPath);

        setupVaultChat();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Bukkit.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(format(JOIN, event.getPlayer())));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        Bukkit.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(format(QUIT, event.getPlayer())));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        BaseComponent[] message = parseMessage(event.getMessage());
        event.getRecipients().forEach(r -> r.spigot().sendMessage((BaseComponent[]) ArrayUtils.addAll(format(FORMAT, event.getPlayer()), message)));
        if(LOG_CONSOLE) Bukkit.getServer().getLogger().info(event.getPlayer().getName() + " >" + new TextComponent(message).toPlainText());

        //Without log4j
        //if(LOG_CONSOLE) Bukkit.getServer().getLogger().info(event.getPlayer().getName() + " >" + new TextComponent(message).toPlainText());
    }

    private BaseComponent[] format(String s, Player player) {

        MessageBuilder builder = new MessageBuilder().define("PLAYER", player.getName()).define("PREFIX", chat == null ? null : chat.getPlayerPrefix(player));

        Persona persona = PersonaAPI.getPersona(player.getUniqueId());
        if(persona != null) {
            if(!persona.isGuest()) builder = builder.define("MARK", " " + persona.getMark().getSymbol());
            builder = builder.define("PERSONA", persona.getHoverEvent(hoverGuest, hoverMember));
        }

        return builder.build(s);

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

                Persona persona = PersonaAPI.getPersona(player.getUniqueId());
                if(persona != null) component.setHoverEvent(persona.getHoverEvent(hoverGuest, hoverMember));

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
        FORMAT = getConfig().getString("format");
        JOIN = getConfig().getString("messages.join");
        QUIT = getConfig().getString("messages.quit");
        LOG_CONSOLE = getConfig().getBoolean("log-console");
    }

    private void setupVaultChat() {
        if(Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Chat> rsp = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
            if(rsp != null) {
                chat = rsp.getProvider();
                PCS_Essentials.getInstance().getLogger().info("Vault-Chat ServiceProvider: " + chat.getName());
            }
        } else PCS_Essentials.getInstance().getLogger().warning("Failed to hook into Vault-Chat");
    }
}
