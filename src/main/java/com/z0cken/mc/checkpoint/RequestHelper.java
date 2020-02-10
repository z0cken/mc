package com.z0cken.mc.checkpoint;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.md_5.bungee.config.Configuration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.IntStream;


class RequestHelper {

    private static final String ENV_VAR_PATH = "";
    private static final String BASE_URL = "https://pr0gramm.com/api/";
    private static Configuration cfg;
    private static String username;
    private static String password /*System.getenv(ENV_VAR_PATH)*/;
    static String cookie;
    private static int timeout = 0;
    static long timestamp;

    static {
        load();
        authenticate();
    }

    private RequestHelper() {
    }

    static void load() {
        cfg = PCS_Checkpoint.getConfig().getSection("bot");
        username = cfg.getString("username");
        password = cfg.getString("password");
        timestamp = cfg.getLong("timestamp");
        cookie = cfg.getString("cookie");
    }

    private static boolean isLoggedIn() {
        HttpResponse<JsonNode> response;

        try {
            response = Unirest.post(BASE_URL + "user/loggedin").header("cookie", cookie).asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
            return false;
        }

        return response.getBody().getObject().getBoolean("loggedIn");
    }

    private static void authenticate() {
        PCS_Checkpoint.getInstance().loadConfig();
        cookie = cfg.getString("cookie");
        if(isLoggedIn()) {
            PCS_Checkpoint.getInstance().getLogger().info("Login validated");
            return;
        }

        HttpResponse<JsonNode> response;

        try {
            response = Unirest.post(BASE_URL + "user/login").header("content-type", "application/x-www-form-urlencoded").field("name", username).field("password", password).asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
            return;
        }

        if (response.getStatus() / 100 != 2) {
            PCS_Checkpoint.getInstance().getLogger().severe(String.format("Authentication failed (HTTP %d)", response.getStatus()));
        }

        if (response.getHeaders().containsKey("Set-Cookie")) {
            cookie = response.getHeaders().getFirst("Set-Cookie").split(";")[0];
            PCS_Checkpoint.getInstance().getLogger().info("Login successful");
        } else {
            PCS_Checkpoint.getInstance().getLogger().severe("Authentication failed (No cookie given)");

            //Reload config to read potential credential changes
            PCS_Checkpoint.getInstance().loadConfig();
            cfg = PCS_Checkpoint.getConfig().getSection("bot");

            String pw = cfg.getString("password");
            if (pw != null) {
                password = pw;
            } else {
                PCS_Checkpoint.getInstance().getLogger().warning("No fallback password defined in config");
                password = System.getenv(ENV_VAR_PATH);
            }
        }
    }

    static void fetchConversations() {
        long newTime = (int) (System.currentTimeMillis() / 1000L);

        final HttpResponse<JsonNode> conversationsResponse;

        try {
            conversationsResponse = Unirest.get(BASE_URL + "inbox/conversations").header("accept", "application/json").header("cookie", cookie).asJson();
        } catch (UnirestException e) {
            PCS_Checkpoint.getInstance().getLogger().log(Level.SEVERE, "Failed to fetch conversations", e);
            return;
        }

        final int status = conversationsResponse.getStatus();

        if (status == 200) {
            final JSONArray conversations = conversationsResponse.getBody().getObject().getJSONArray("conversations");

            try {
                CompletableFuture.allOf(IntStream.range(0, conversations.length())
                        .mapToObj(i -> CompletableFuture.runAsync(() -> handleConversation(conversations.getJSONObject(i))))
                        .toArray(CompletableFuture[]::new)
                ).get();
            } catch (InterruptedException | ExecutionException e) {
                PCS_Checkpoint.getInstance().getLogger().log(Level.SEVERE,"Failed to handle conversations", e);
                return;
            }

            timestamp = newTime;
        } else {
            PCS_Checkpoint.getInstance().getLogger().severe(String.format("Failed to fetch conversations (HTTP %d)", conversationsResponse.getStatus()));

            if (status == 403) {
                if (timeout-- <= 0) {
                    authenticate();
                    timeout = cfg.getInt("login-timeout-multiplier");
                } else
                    PCS_Checkpoint.getInstance().getLogger().info(String.format("Attempting authentication in %d seconds", ((timeout + 1) * cfg.getInt("interval"))));
            }
        }
    }

    static void handleConversation(final JSONObject conversation) {
        if (conversation.getLong("lastMessage") > timestamp) {
            final String name = conversation.getString("name");
            final HttpResponse<JsonNode> messagesResponse;

            try {
                messagesResponse = Unirest.get(BASE_URL + "inbox/messages?with=" + name).header("accept", "application/json").header("cookie", cookie).asJson();
            } catch (UnirestException e) {
                PCS_Checkpoint.getInstance().getLogger().log(Level.SEVERE, "Failed to fetch messages with " + name, e);
                return;
            }

            final JSONArray messages = messagesResponse.getBody().getObject().getJSONArray("messages");

            for (final Object o : messages) {
                JSONObject message = (JSONObject) o;
                if (message.getLong("created") < timestamp) break;
                if (message.getInt("sent") == 1) continue;

                final String msg = message.getString("message").trim();
                if(msg.startsWith("mc")) DatabaseHelper.verify(msg, message.getString("name"));
            }
        }
    }
}
