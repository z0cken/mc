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
        SCHWUCHTEL("Schwuchtel", '●', ChatColor.WHITE, 3, 5),
        NEUSCHWUCHTEL("Neuschwuchtel", '●', ChatColor.of("#e108e9"), 2, 2),
        ALTSCHWUCHTEL("Altschwuchtel", '●', ChatColor.of("#5bb91c"), 5, 10),
        ADMIN("Admin", '●', ChatColor.of("#ff9900"), 1337, Integer.MAX_VALUE),
        GEBANNT("Gebannt", '●', ChatColor.of("#444444"), 0, 0),
        MODERATOR("Moderator", '●', ChatColor.of("#008fff"), 5, 10),
        FLIESENTISCH("Fliesentischbesitzer", '●', ChatColor.of("#6c432b"), 1, 1),
        LEGENDE("Lebende Legende", '✫', ChatColor.of("#1cb992"), 3, 10),
        WICHTEL("Wichtel", '✉', ChatColor.of("#c52b2f"), 3, 10),
        SPENDER("Edler Spender", '⦿', ChatColor.of("#1cb992"), 3, 10),
        MITTELALTSCHWUCHTEL("Mittelaltschwuchtel", '●', ChatColor.of("#addc8d"), 4, 10),
        ALTMOD("Alt-Moderator", '●', ChatColor.of("#7fc7ff"), 5, 10),
        NUTZERBOT("Nutzer-Bot", '●', ChatColor.of("#10366f"), 3, 10),
        SYSTEMBOT("System-Bot", '●', ChatColor.of("#ffc166"), 3, 10),
        ALTHELFER("Alt-Helfer", '●', ChatColor.of("#ea9fa1"), 5, 10),
        COMMUNITYHELFER("Communityhelfer", '❤', ChatColor.of("#c52b2f"), 5, 10);

        private final String title;
        private final char   symbol;
        private final ChatColor color;
        private final int       startInvites;
        private final int maxInvites;

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
        BLUE(ChatColor.of("#337fd2")), RED(ChatColor.of("#c52b2f"));

        private final ChatColor color;

        Team(ChatColor color) {
            this.color = color;
        }

        public ChatColor getColor() {
            return color;
        }
    }
}
