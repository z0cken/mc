package com.z0cken.mc.core.util;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CoreUtil {

    private CoreUtil() {}

    public static List<String> getMojangNames(UUID uuid) throws IllegalArgumentException, UnirestException {
        List<String> list = new ArrayList<>();

        String request = "https://api.mojang.com/user/profiles/" + uuid.toString().replace('-', Character.MIN_VALUE) + "/names";
        HttpResponse<JsonNode> response = Unirest.get(request).header("accept", "application/json").asJson();

        if(response.getStatus() != 200) throw new IllegalArgumentException("Name history for " + uuid + "not available (Status " + response.getStatus() + ")");

        JSONArray array = response.getBody().getArray();
        for(int i = 0; i < array.length(); i++){
            list.add(array.getJSONObject(i).getString("name"));
        }

        return list;
    }

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
