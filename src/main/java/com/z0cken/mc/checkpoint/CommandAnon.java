package com.z0cken.mc.checkpoint;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.z0cken.mc.core.persona.Persona;
import com.z0cken.mc.core.persona.PersonaAPI;
import com.z0cken.mc.core.util.MessageBuilder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import org.apache.http.client.HttpResponseException;

import java.sql.SQLException;

class CommandAnon extends Command {

    private static final Configuration cfg = PCS_Checkpoint.getConfig().getSection("messages.anon");

    CommandAnon() {
        super("anon");
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if(commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            MessageBuilder builder = new MessageBuilder().define("A", PCS_Checkpoint.getConfig().getString("messages.accent-color"));

            Persona persona;

            //noinspection Duplicates
            try {
                persona = PersonaAPI.getPersona(player.getUniqueId());
            } catch (SQLException | HttpResponseException | UnirestException e) {
                e.printStackTrace();

                player.sendMessage(builder.build(PCS_Checkpoint.getConfig().getString("messages.error")));
                return;
            }

            if(persona != null && !persona.isGuest()) {
                boolean isAnonymized = persona.isAnonymized();
                persona.setAnonymized(!isAnonymized);
                player.sendMessage(builder.build(cfg.getString("switched-to-" + Boolean.toString(!isAnonymized))));

            } else {
                player.sendMessage(builder.build(cfg.getString("denied-nomember")));
            }
        }
    }
}
