package com.z0cken.mc.essentials.modules;

import com.z0cken.mc.claim.PCS_Claim;
import com.z0cken.mc.core.persona.PersonaAPI;
import com.z0cken.mc.core.util.MessageBuilder;
import com.z0cken.mc.essentials.PCS_Essentials;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


public class ModuleChat extends Module implements Listener {

    private static Chat chat = null;

    private static final Pattern PATTERN_URL = Pattern.compile("^(?:(https?)://)([-\\w_.]{2,}\\.[a-z]{2,4})([/?]\\S*)?$");
    private String FORMAT, JOIN, QUIT, MENTION;
    private boolean LOG_CONSOLE;

    public ModuleChat(String configPath) {
        super(configPath);

        setupVaultChat();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        final Player player = event.getPlayer();

        PersonaAPI.getPersona(player.getUniqueId()).getComponent(player.getName(), 3, TimeUnit.SECONDS).thenAcceptAsync(component -> {
            BaseComponent[] msg = MessageBuilder.DEFAULT.define("PLAYER", component).define("PREFIX", getPrefix(player)).build(JOIN);
            Bukkit.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(msg));
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        final Player player = event.getPlayer();

        PersonaAPI.getPersona(player.getUniqueId()).getComponent(player.getName(), 3, TimeUnit.SECONDS).thenAcceptAsync(component -> {
            BaseComponent[] msg = MessageBuilder.DEFAULT.define("PLAYER", component).define("PREFIX", getPrefix(player)).build(QUIT);
            Bukkit.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(msg));
        });
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        final Player player = event.getPlayer();

        PersonaAPI.getPersona(player.getUniqueId()).getComponent(player.getName(), 250, TimeUnit.MILLISECONDS).thenAcceptAsync(component -> {
            BaseComponent[] msg = (BaseComponent[]) ArrayUtils.addAll(MessageBuilder.DEFAULT.define("PLAYER", component).define("PREFIX", getPrefix(player)).build(FORMAT), parseMessage(event.getMessage()));
            Bukkit.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(msg));
        });

        if(LOG_CONSOLE) Bukkit.getServer().getLogger().info(player.getName() + " > " + event.getMessage());

        //Without log4j
        //if(LOG_CONSOLE) Bukkit.getServer().getLogger().info(event.getPlayer().getName() + " >" + new TextComponent(message).toPlainText());
    }

    private BaseComponent[] parseMessage(String message) {
        String[] msg = message.split("\\s+");
        ComponentBuilder builder = new ComponentBuilder(" ").color(ChatColor.GRAY);

        for(int i = 0; i < msg.length; i++) {
            builder.append(new TextComponent(msg[i]));
            if(i+1 != msg.length) builder.append(" ");
        }

        List<BaseComponent> components = new ArrayList<>(Arrays.asList(builder.create()));
        parseLinks(components);
        parseMentions(components);
        return components.toArray(new BaseComponent[components.size()]);
    }

    private void parseMentions(List<BaseComponent> message) {
        ListIterator<BaseComponent> iterator = message.listIterator();
        while (iterator.hasNext()){
            BaseComponent c = iterator.next();
            if(!(c instanceof TextComponent)) continue;

            TextComponent component = (TextComponent) c;
            Player player = Bukkit.getPlayerExact(component.getText());

            if(player != null) {
                HoverEvent hoverEvent;
                try {
                    hoverEvent = PersonaAPI.getPersona(player.getUniqueId()).getHoverEvent(250, TimeUnit.MILLISECONDS).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    return;
                }

                iterator.remove();
                Arrays.asList(MessageBuilder.DEFAULT.define("NAME", player.getName()).define("BUBBLE", hoverEvent).define("PREFIX", getPrefix(player)).build(MENTION)).forEach(iterator::add);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1.75F);
            }
        }
    }

    private static void parseLinks(List<BaseComponent> message) {
        for(BaseComponent c : message) {
            if(!(c instanceof TextComponent)) continue;

            TextComponent component = (TextComponent) c;
            String text = component.getText();

            if(PATTERN_URL.matcher(text).matches()) {
                component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, text));
                component.setText(text.replaceAll("^(https?)://", "").replaceAll("^(www\\.)?(pr0gramm.com)", ""));
                component.setColor(ChatColor.AQUA);
            }
        }
    }

    @Override
    public void load() {
        FORMAT = getConfig().getString("formats.general");
        JOIN = getConfig().getString("formats.join");
        QUIT = getConfig().getString("formats.quit");
        MENTION = getConfig().getString("formats.mention");
        LOG_CONSOLE = getConfig().getBoolean("log-console");
    }

    private String getPrefix(Player player) {
        if(chat != null) {
            String prefix = chat.getPlayerPrefix(player);
            if(prefix.isEmpty() || prefix.equalsIgnoreCase("")) return prefix;
            else return ChatColor.translateAlternateColorCodes('&', prefix) + " ";
        }
        return "";
    }

    private void setupVaultChat() {
        if(Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Chat> rsp = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
            if(rsp != null) {
                chat = rsp.getProvider();
                PCS_Essentials.getInstance().getLogger().info("Vault-Chat ServiceProvider: " + chat.getName());
            } else PCS_Claim.getInstance().getLogger().warning("Vault-Chat ServiceProvider not found");
        } else PCS_Essentials.getInstance().getLogger().warning("Failed to hook into Vault-Chat");
    }
}
