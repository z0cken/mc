package com.z0cken.mc.core.bungee;

import com.z0cken.mc.core.ICore;
import com.z0cken.mc.core.bungee.commands.CommandFriend;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.*;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class PCS_Core extends Plugin implements ICore {

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        saveResource("hikari.properties", false);
        ICore.super.init();

        getProxy().getPluginManager().registerCommand(this, new CommandFriend());
    }

    @Override
    public void onDisable() {
        ICore.super.shutdown();
    }

    @Override
    public void stopServer(String reason) {
        this.getProxy().stop(reason);
    }

    void saveResource(String resourcePath, boolean replace) {
        if (resourcePath != null && !resourcePath.equals("")) {
            resourcePath = resourcePath.replace('\\', '/');
            InputStream in = this.getResourceAsStream(resourcePath);
            if (in == null) {
                throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + this.getFile());
            } else {
                File outFile = new File(this.getDataFolder(), resourcePath);
                int lastIndex = resourcePath.lastIndexOf(47);
                File outDir = new File(this.getDataFolder(), resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));
                if (!outDir.exists()) {
                    outDir.mkdirs();
                }

                try {
                    if(!outFile.exists() || replace) {
                        OutputStream out = new FileOutputStream(outFile);
                        byte[] buf = new byte[1024];

                        int len;
                        while((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }

                        out.close();
                        in.close();
                    }
                } catch (IOException var10) {
                    this.getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, var10);
                }

            }
        } else {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
    }
}
