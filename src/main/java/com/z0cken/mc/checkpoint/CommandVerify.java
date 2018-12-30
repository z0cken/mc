package com.z0cken.mc.checkpoint;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import org.apache.commons.codec.digest.DigestUtils;


public class CommandVerify extends Command {

    private Configuration cfg = PCS_Checkpoint.getInstance().getConfig().getSection("messages.verify");
    private static final String SALT = "dm780";

    @SuppressWarnings("WeakerAccess")
    public CommandVerify() {
        super("verify");
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if(commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            Persona p = PCS_Checkpoint.getInstance().getPersona(player);
            Util.MessageBuilder builder = new Util.MessageBuilder(null, null);

            if(p != null) {
                builder = new Util.MessageBuilder(p.getUsername(), null);
                player.sendMessage(builder.digest(cfg.getString("denied-isverified")));

            } else {
                String md5Hex = "mc-" + DigestUtils.md5Hex(player.getUniqueId().toString() + SALT);
                DatabaseHelper.insertPending(player.getUniqueId(), md5Hex);

                TextComponent button = new TextComponent( md5Hex );
                button.setColor(Util.MessageBuilder.getAccentColor());
                button.setClickEvent( new ClickEvent( ClickEvent.Action.SUGGEST_COMMAND, "/" + md5Hex));

                for(String s : cfg.getStringList("confirmed")) {

                    s = builder.digest(s);

                    String[] strings = s.split("\\{BUTTON}");

                    if(s.contains("{BUTTON}")) {
                        player.sendMessage(new ComponentBuilder(strings[0]).append(button).append(strings.length > 1 ? strings[1] : "").create());
                    } else {
                        player.sendMessage(s);
                    }
                }
            }
        }
    }
}
