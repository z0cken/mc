package com.z0cken.mc.checkpoint;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.z0cken.mc.core.persona.Persona;
import com.z0cken.mc.core.persona.PersonaAPI;
import com.z0cken.mc.core.util.MessageBuilder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.HttpResponseException;

import java.sql.SQLException;


class CommandVerify extends Command {

    private static final Configuration cfg = PCS_Checkpoint.getConfig().getSection("messages.verify");
    private static final String SALT = "dm780";

    CommandVerify() {
        super("verify");
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if(commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            Persona persona;

            //noinspection Duplicates
            try {
                persona = PersonaAPI.getPersona(player.getUniqueId());
            } catch (SQLException | HttpResponseException | UnirestException e) {
                e.printStackTrace();

                player.sendMessage(MessageBuilder.DEFAULT.build(PCS_Checkpoint.getConfig().getString("messages.error")));
                return;
            }

            MessageBuilder builder = new MessageBuilder().define("A", PCS_Checkpoint.getConfig().getString("messages.accent-color"));

            if(persona != null && !persona.isGuest()) {
                builder = builder.define("NAME", persona.getName());
                player.sendMessage(builder.build(cfg.getString("denied-isverified")));

            } else {
                String md5Hex = "mc-" + DigestUtils.md5Hex(player.getUniqueId().toString() + SALT);
                DatabaseHelper.insertPending(player.getUniqueId(), md5Hex);

                builder = builder.define("HASH", md5Hex);

                for(String s : cfg.getStringList("info")) {
                    player.sendMessage(builder.build(s));
                }
            }
        }
    }
}
