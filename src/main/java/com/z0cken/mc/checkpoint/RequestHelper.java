package com.z0cken.mc.checkpoint;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.md_5.bungee.config.Configuration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.logging.Level;


class RequestHelper {

    private static final String ENV_VAR_PATH = "";
    private static final String BASE_URL = "https://pr0gramm.com/api/";
    private static Configuration cfg;
    private static String username;
    private static String password /*System.getenv(ENV_VAR_PATH)*/ ;
    private static String cookie;
    private static int timeout = 0;

    static long timestamp;

    static {
        load();
        authenticate();
    }

    private RequestHelper() {}

    static void load() {
        cfg = PCS_Checkpoint.getConfig().getSection("bot");
        username = cfg.getString("username");
        password = cfg.getString("password");
        timestamp = cfg.getLong("timestamp");
    }

    private static void authenticate() {
        PCS_Checkpoint.getInstance().loadConfig();
        cookie = cfg.getString("cookie");

        HttpResponse<JsonNode> response;

        try {
            response = Unirest.post(BASE_URL + "user/login").header("content-type", "application/x-www-form-urlencoded").field("name", username).field("password", password).asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
            return;
        }

        if(response.getStatus() / 100 != 2) {
            PCS_Checkpoint.getInstance().getLogger().severe(String.format("Authentication failed (HTTP %d)", response.getStatus()));
        }

        if (response.getHeaders().containsKey("Set-Cookie")) {
            cookie = response.getHeaders().getFirst("Set-Cookie").split(";")[0];
        } else {
            PCS_Checkpoint.getInstance().getLogger().severe("Authentication failed (No cookie given)");

            PCS_Checkpoint.getInstance().loadConfig();
            cfg = PCS_Checkpoint.getConfig().getSection("bot");
            String s = cfg.getString("password");

            if(s != null) {
                password = s;
            } else {
                PCS_Checkpoint.getInstance().getLogger().warning("No fallback password defined in config");
                password = System.getenv(ENV_VAR_PATH);
            }
        }
    }

    static void fetchMessages() {
        long newTime = (int) (System.currentTimeMillis() / 1000L);

        HttpResponse<JsonNode> response;

        try {
            response = Unirest.get(BASE_URL + "inbox/messages").header("accept", "application/json").header("cookie", cookie).asJson();
        } catch (UnirestException e) {
            PCS_Checkpoint.getInstance().getLogger().log(Level.SEVERE, "Failed to fetch messages", e);
            return;
        }

        final int status = response.getStatus();

        if(status != 200) {
            PCS_Checkpoint.getInstance().getLogger().severe(String.format("Failed to fetch messages (HTTP %d)", response.getStatus()));

            if(status == 403) {
                if(timeout-- <= 0) {
                    authenticate();
                    timeout = cfg.getInt("login-timeout-multiplier");
                } else PCS_Checkpoint.getInstance().getLogger().info(String.format("Attempting authentication in %d seconds", ((timeout + 1) * cfg.getInt("interval")) ));
            }


        } else {
            JSONArray array = response.getBody().getObject().getJSONArray("messages");

            for(Object o : array){
                JSONObject obj = (JSONObject) o;

                if(obj.getLong("created") > timestamp) {
                    if(obj.getBoolean("sent")) continue;
                    DatabaseHelper.verify(obj.getString("message").trim(), obj.getString("senderName"));
                }
            }

            timestamp = newTime;
        }
    }
}
