package com.pr0gramm.mc.verify;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.apache.commons.codec.digest.DigestUtils;

public class CommandVerify extends Command {

    private static final String SALT = "dm780";

    @SuppressWarnings("WeakerAccess")
    public CommandVerify() {
        super("verify");
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if(commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            Persona p = PCS_Checkpoint.getInstance().getPersona(player);
            if(p != null) {
                player.sendMessage("§c§l>_ §7Dein Account " + p.getUsername() + " ist bereits bestätigt!");

            } else {
                String md5Hex = DigestUtils.md5Hex(player.getUniqueId().toString() + SALT);
                DatabaseHelper.insertPending(player.getUniqueId(), md5Hex);

                player.sendMessage("§8[ ------------------ §c§l[ §f§l>_ §c§l] §8------------------ ]");
                player.sendMessage("§8[ - §7Sende diesen Code per PN an den Bot §bz0cken");

                TextComponent message = new TextComponent( md5Hex );
                message.setColor(ChatColor.getByChar('b'));
                message.setClickEvent( new ClickEvent( ClickEvent.Action.SUGGEST_COMMAND, "/" + md5Hex));

                player.sendMessage(new ComponentBuilder("§8[ - §b").append(message).create());
                player.sendMessage("§8[ - §7Klick drauf, um ihn ins Textfeld zu kopieren");
                player.sendMessage("§8[ ------------------------------------------- ]");

            }
        }
    }
}
