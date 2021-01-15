package com.z0cken.mc.checkpoint;

import com.z0cken.mc.core.persona.Persona;
import com.z0cken.mc.core.persona.PersonaAPI;
import com.z0cken.mc.core.util.MessageBuilder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import org.apache.commons.codec.digest.DigestUtils;


class CommandVerify extends Command {

    private static Configuration cfg;
    private static final String SALT = "dm780";

    CommandVerify() {
        super("verify");
        load();
    }

    static void load() {
       cfg = PCS_Checkpoint.getConfig().getSection("messages.verify");
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if(commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            Persona persona = PersonaAPI.getPersona(player.getUniqueId());

            if(persona.isVerified()) player.sendMessage(MessageBuilder.DEFAULT.build(cfg.getString("denied-isverified")));
            else {
                String md5Hex = "mc-" + DigestUtils.md5Hex(player.getUniqueId().toString() + SALT);
                DatabaseHelper.insertPending(player.getUniqueId(), md5Hex);

                for(String s : cfg.getStringList("info")) {
                    player.sendMessage(MessageBuilder.DEFAULT.define("HASH", md5Hex).build(s));
                }
            }
        }
    }
}
