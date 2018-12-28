package com.z0cken.mc.checkpoint;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.md_5.bungee.api.ChatColor;
import org.json.JSONObject;

import java.util.UUID;

public class Util {

    public static UUID getMojangUUID(String name) throws IllegalArgumentException, UnirestException {
        String request = "https://api.mojang.com/users/profiles/minecraft/" + name;
        HttpResponse<JsonNode> response = Unirest.get(request).header("accept", "application/json").asJson();

        if(response.getStatus() != 200) throw new IllegalArgumentException("UUID for " + name + "not available (Status " + response.getStatus() + ")");

        JSONObject json = response.getBody().getObject();
        return UUID.fromString(insertDashUUID(json.getString("id")));
    }

    private static String insertDashUUID(String uuid) {
        StringBuilder sb = new StringBuilder(uuid);
        sb.insert(8, "-").insert(13, "-").insert(18, "-").insert(23, "-");

        return sb.toString();
    }

    static class MessageBuilder {

        private static  ChatColor accent = ChatColor.getByChar(PCS_Checkpoint.getInstance().getConfig().getString("messages.accent-color").charAt(0));
        private String name;
        private Integer invites;

        MessageBuilder(String name, Integer invites) {
            this.name = name;
            this.invites = invites;
        }

        String digest(String s) {
            if(accent != null) s = s.replace("{A}", accent.toString());
            if(name != null) s = s.replace("{NAME}", name);
            if(invites != null) s = s.replace("{INVITES}", Integer.toString(invites));

            return s;
        }

        public static ChatColor getAccentColor() {
            return accent;
        }
    }
}
