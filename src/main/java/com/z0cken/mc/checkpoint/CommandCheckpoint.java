package com.z0cken.mc.checkpoint;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandCheckpoint extends Command {

    public CommandCheckpoint() {
        super("checkpoint");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("pcs.checkpoint.manage")) {
            if(args.length > 0) {
                if(args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
                    PCS_Checkpoint.getInstance().reload();
                    sender.sendMessage("§a§l>_ §7Checkpoint neu geladen!");
                }
            }

        }
    }
}
