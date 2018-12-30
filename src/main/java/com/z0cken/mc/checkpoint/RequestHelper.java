package com.z0cken.mc.checkpoint;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.md_5.bungee.config.Configuration;
import org.json.JSONArray;
import org.json.JSONObject;


public class RequestHelper {

    private static final String baseUrl = "https://pr0gramm.com/api/";
    private static Configuration cfg = PCS_Checkpoint.getInstance().getConfig().getSection("bot");
    private static String username = cfg.getString("username");
    private static String password = cfg.getString("password");
    private static String cookie = cfg.getString("cookie");
    private static int timeout = 0;

    static long timestamp = cfg.getLong("timestamp");

    private static void login() {

        String url = "user/login";
        HttpResponse<JsonNode> response;

        try {
            response = Unirest.post(baseUrl + url).header("content-type", "application/x-www-form-urlencoded").field("name", username).field("password", password).asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
            return;
        }

        //TODO Check if UnirestException covers this
        if(response.getStatus() != 200) {
            PCS_Checkpoint.getInstance().getLogger().severe("Could not login - HTTP " + response.getStatus());
            return;
        }

        try {
            cookie = response.getHeaders().get("Set-Cookie").get(0).split(";")[0];
        } catch (NullPointerException e) {
            PCS_Checkpoint.getInstance().getLogger().severe("Could not login - No cookie given");
            return;
        }

        PCS_Checkpoint.getInstance().getLogger().info("Login successful");
    }

    static void checkVerifications() {

        long newTime = (int) (System.currentTimeMillis() / 1000L);

        String url = "inbox/messages";
        HttpResponse<JsonNode> response;

        try {
            response = Unirest.get(baseUrl + url).header("accept", "application/json").header("cookie", cookie).asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
            return;
        }

        //TODO Check if UnirestException covers this
        if(response.getStatus() != 200) {
            PCS_Checkpoint.getInstance().getLogger().severe("Could not check for verifications (HTTP " + response.getStatus() + ")");
            if(response.getStatus() == 403) {
                if(timeout-- <= 0) {
                    login();
                    timeout = cfg.getInt("login-timeout-multiplier");
                } else {

                }
            }
            return;
        }

        JSONArray array = response.getBody().getObject().getJSONArray("messages");

        for(Object o : array){
            JSONObject obj = (JSONObject) o;

            if(obj.getLong("created") > timestamp) {
                if(obj.getBoolean("sent")) continue;
                DatabaseHelper.verify(obj.getString("message"), obj.getString("senderName"));
            }
        }

        timestamp = newTime;
    }

    public static boolean isBanned(String name) throws UnirestException {

        String request = "profile/info?name=" + name;
        JsonNode jsonBody = Unirest.get(baseUrl).header("accept", "application/json").asJson().getBody();
        JSONObject json = jsonBody.getObject();

        return 1 == json.getJSONObject("user").getInt("banned");
    }
}
