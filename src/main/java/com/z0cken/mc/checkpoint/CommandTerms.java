package com.z0cken.mc.checkpoint;

import com.z0cken.mc.core.Shadow;
import com.z0cken.mc.core.persona.PersonaAPI;
import com.z0cken.mc.core.util.MessageBuilder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

import java.sql.SQLException;

public class CommandTerms extends Command {

    private static Configuration cfg;

    CommandTerms() {
        super("terms");
        load();
    }

    static void load() {
        cfg = PCS_Checkpoint.getConfig().getSection("messages.terms");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer) sender;
        if(!PCS_Checkpoint.getConfig().getBoolean("require-terms")) return;

        try {
            if(Shadow.TERMS.getBoolean(player.getUniqueId())) {
                sender.sendMessage(MessageBuilder.DEFAULT.build(cfg.getString("info")));
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        sender.sendMessage(MessageBuilder.DEFAULT.build(cfg.getString("accepted")));
        PersonaAPI.getPersona(player.getUniqueId()).setAcceptedTerms(true);
    }
}
