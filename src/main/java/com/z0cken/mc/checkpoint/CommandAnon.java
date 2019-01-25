package com.z0cken.mc.checkpoint;

import com.z0cken.mc.core.util.MessageBuilder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

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

            boolean isMember = PCS_Checkpoint.getPersona(player) != null;

            if(isMember) {
                boolean isAnonymous = DatabaseHelper.isAnonymous(player.getUniqueId());
                DatabaseHelper.setAnonymous(player.getUniqueId(), !isAnonymous);

                player.sendMessage(builder.build(cfg.getString("switched-to-" + Boolean.toString(!isAnonymous))));


            } else {
                player.sendMessage(builder.build(cfg.getString("denied-nomember")));
            }
        }
    }
}
