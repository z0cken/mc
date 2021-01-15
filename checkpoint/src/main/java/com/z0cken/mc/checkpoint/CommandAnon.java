package com.z0cken.mc.checkpoint;

import com.z0cken.mc.core.persona.Persona;
import com.z0cken.mc.core.persona.PersonaAPI;
import com.z0cken.mc.core.util.MessageBuilder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

class CommandAnon extends Command {

    private static Configuration cfg;

    CommandAnon() {
        super("anon");
        load();
    }

    static void load() {
        cfg = PCS_Checkpoint.getConfig().getSection("messages.anon");
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if(commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            Persona persona = PersonaAPI.getPersona(player.getUniqueId());

            if(persona != null && !persona.isGuest()) {
                boolean isAnonymized = persona.isAnonymized();
                persona.setAnonymized(!isAnonymized);
                player.sendMessage(MessageBuilder.DEFAULT.build(cfg.getString("switched-to-" + Boolean.toString(!isAnonymized))));
            } else {
                player.sendMessage(MessageBuilder.DEFAULT.build(cfg.getString("denied-nomember")));
            }
        }
    }
}
