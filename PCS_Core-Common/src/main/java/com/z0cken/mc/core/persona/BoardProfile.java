package com.z0cken.mc.core.persona;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.z0cken.mc.core.CoreBridge;
import net.md_5.bungee.api.ChatColor;
import org.apache.http.client.HttpResponseException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.logging.Level;

public class BoardProfile {

    private String name;
    private Long registered;
    private boolean banned;
    private LocalDateTime bannedUntil;
    private int benis;
    private BoardProfile.Mark mark;
    private Team team;

    private BoardProfile(String name) throws UnirestException, HttpResponseException {
        this.name = name;
        fetch();
    }

    public static BoardProfile of(String name) {
        try {
            return new BoardProfile(name);
        } catch (UnirestException e) {
            CoreBridge.getPlugin().getLogger().log(Level.WARNING, String.format("Could not fetch profile of %s ", name), e);
        } catch (HttpResponseException e) {
            CoreBridge.getPlugin().getLogger().log(Level.WARNING, String.format("Could not fetch profile of %s (HTTP %d: %s)", name, e.getStatusCode(), e.getMessage()));
        }
        return null;
    }

    void fetch() throws UnirestException, HttpResponseException {
        GetRequest request = Unirest.get("https://pr0gramm.com/api/profile/info?name=" + name);

        HttpResponse<JsonNode> response;

        HttpResponse<String> httpResponse = request.asString();
        int status = httpResponse.getStatus();

        if(status / 100 != 2) throw new HttpResponseException(httpResponse.getStatus(), httpResponse.getStatusText());

        response = request.asJson();

        JSONObject userObject = response.getBody().getObject().getJSONObject("user");
        registered = userObject.getLong("registered");
        benis = userObject.getInt("score");
        mark = BoardProfile.Mark.getById(userObject.getInt("mark"));
        banned = userObject.getInt("banned") == 1;
        team = userObject.getInt("id") % 2 == 0 ? Team.BLUE : Team.RED;

        if(banned) {
            if(!userObject.isNull("bannedUntil"))
                bannedUntil = LocalDateTime.ofInstant(Instant.ofEpochSecond(userObject.getLong("bannedUntil")), TimeZone.getTimeZone(ZoneOffset.ofHours(1)).toZoneId());
        }

        JSONArray badgeArray = response.getBody().getObject().getJSONArray("badges");
        if(badgeArray != null) {
            for(int i = 0; i < badgeArray.length(); i++) {
                //TODO badges?
            }
        }
    }

    public String getName() {
        return name;
    }

    public long getRegistered() {
        return registered;
    }

    public boolean isBanned() {
        return banned;
    }

    public LocalDateTime getBannedUntil() {
        return bannedUntil;
    }

    public BoardProfile.Mark getMark() {
        return mark;
    }

    public Team getTeam() { return team; }

    public int getBenis() {
        return benis;
    }

    private static final NavigableMap<Integer, String> suffixes = new TreeMap<>() {{
        put(1_000, "k");
        put(1_000_000, "M");
    }};

    public String getBenisFormatted() {
        if (Math.abs(benis) < 1000) {
        if(benis < 0) {
            return "< 0";
        }
        return "< 1k";
    }

        Map.Entry<Integer, String> e = suffixes.floorEntry(Math.abs(benis));
        Integer divideBy = e.getKey();
        String suffix = e.getValue();

        int truncated = Math.abs(benis) / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        String result = hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
        return benis < 0 ? "-" + result : result;
    }

    /** @noinspection SpellCheckingInspection*/
    public enum Mark {
        SCHWUCHTEL("Schwuchtel", '●', ChatColor.WHITE, 2, 5),
        NEUSCHWUCHTEL("Neuschwuchtel", '●', ChatColor.LIGHT_PURPLE, 1, 2),
        ALTSCHWUCHTEL("Altschwuchtel", '●', ChatColor.DARK_GREEN, 3, 10),
        ADMIN("Admin", '●', ChatColor.GOLD, 1337, Integer.MAX_VALUE),
        GEBANNT("Gebannt", 'X', ChatColor.GRAY, 0, 0),
        MODERATOR("Moderator", '●', ChatColor.DARK_BLUE, 3, 10),
        FLIESENTISCH("Fliesentischbesitzer", '●', ChatColor.GRAY, 0, 1),
        LEGENDE("Lebende Legende", '✫', ChatColor.DARK_AQUA, 3, 10),
        WICHTEL("Wichtel", '✉', ChatColor.RED, 3, 10),
        SPENDER("Edler Spender", '⦿', ChatColor.DARK_AQUA, 3, 10),
        MITTELALTSCHWUCHTEL("Mittelaltschwuchtel", '●', ChatColor.GREEN, 3, 10),
        ALTMOD("Alt-Moderator", '●', ChatColor.BLUE, 3, 10),
        COMMUNITYHELFER("Communityhelfer", '❤', ChatColor.DARK_RED, 3, 10);

        private String title;
        private char symbol;
        private ChatColor color;
        private int startInvites, maxInvites;

        Mark(String title, char symbol, ChatColor color, int startInvites, int maxInvites) {
            this.title = title;
            this.symbol = symbol;
            this.color = color;
            this.startInvites = startInvites;
            this.maxInvites = maxInvites;
        }

        public static Mark getById(int id) {
            return values()[id];
        }

        public String getTitle() {
            return color + title;
        }

        public String getSymbol() {
            return color.toString() + symbol;
        }

        public ChatColor getColor() {
            return color;
        }

        public int getStartInvites() {
            return startInvites;
        }

        public int getMaxInvites() {
            return maxInvites;
        }
    }

    public enum Team {
        BLUE(ChatColor.BLUE), RED(ChatColor.RED);

        private ChatColor color;

        Team(ChatColor color) {
            this.color = color;
        }

        public ChatColor getColor() {
            return color;
        }
    }
}
