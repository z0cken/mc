package com.z0cken.mc.checkpoint;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;


public class RequestHelper {

    static long lastCheck = PCS_Checkpoint.getInstance().getConfig().getLong("lastCheck");
    private static final String COOKIE = PCS_Checkpoint.getInstance().getConfig().getString("cookie");

    public static boolean isBanned(String name) throws UnirestException {

        String request = "https://pr0gramm.com/api/profile/info?name=" + name;
        JsonNode jsonBody = Unirest.get(request).header("accept", "application/json").asJson().getBody();
        JSONObject json = jsonBody.getObject();

        return 1 == json.getJSONObject("user").getInt("banned");
    }

    static void checkVerifications() {

        long newTime = System.currentTimeMillis();

        String url = "https://pr0gramm.com/api/inbox/messages";
        HttpResponse<JsonNode> response;

        try {
            response = Unirest.get(url).header("accept", "application/json").header("cookie", COOKIE).asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
            return;
        }

        //TODO Check if UnirestException covers this
        if(response.getStatus() != 200) {
            PCS_Checkpoint.getInstance().getLogger().severe("Could not check for verifications (HTTP " + response.getStatus() + ")");
            return;
        }

        JSONArray array = response.getBody().getObject().getJSONArray("messages");

        for(Object o : array){
            JSONObject obj = (JSONObject) o;

            if(obj.getLong("created") > lastCheck) {
                if(obj.getBoolean("sent")) continue;
                DatabaseHelper.verify(obj.getString("message"), obj.getString("senderName"));
            }
        }
        lastCheck = newTime;
    }

}
