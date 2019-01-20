package com.z0cken.mc.checkpoint;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

import java.util.UUID;

class Util {

    private Util() {}

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
}
