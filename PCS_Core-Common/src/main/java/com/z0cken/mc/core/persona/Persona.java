package com.z0cken.mc.core.persona;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.z0cken.mc.core.CoreBridge;
import com.z0cken.mc.core.Shadow;
import com.z0cken.mc.core.util.ConfigurationType;
import com.z0cken.mc.core.util.MessageBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.http.client.HttpResponseException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class Persona {

    private static final List<String> guestList = CoreBridge.getPlugin().getConfigBridge(ConfigurationType.CORE).getStringList("hover-event.guest");
    private static final List<String> memberList = CoreBridge.getPlugin().getConfigBridge(ConfigurationType.CORE).getStringList("hover-event.member");

    private UUID uuid;

    //pr0gramm
    private String name;
    private long registered;
    private boolean banned;
    private LocalDateTime bannedUntil;
    private int benis;
    private Mark mark;
    private boolean anonymized;

    //Guest
    private UUID host;
    private long invited;

    SortedSet<Badge> badges;

    Persona(@Nonnull UUID uuid) throws SQLException, HttpResponseException, UnirestException  {
        this.uuid = uuid;

        String name = DatabaseHelper.getUsername(uuid);

        if(name != null) {
            this.name = name;
            anonymized = DatabaseHelper.isAnonymized(uuid);
            fetchProfile();
        } else {
            String host = DatabaseHelper.getHost(uuid);
            if(host != null) {
                this.host = UUID.fromString(host);
                invited = DatabaseHelper.getInvited(uuid);
            }
        }

        badges = DatabaseHelper.getBadges(uuid);
    }

    void fetchProfile() throws HttpResponseException, UnirestException {
        GetRequest request = Unirest.get("https://pr0gramm.com/api/profile/info?name=" + name).header("accept", "application/json");

        HttpResponse<String> httpResponse = request.asString();
        int status = httpResponse.getStatus();

        if(status / 100 != 2) {
            CoreBridge.getPlugin().getLogger().severe(String.format("Could not fetch profile of %s (HTTP %d)", name, status));
            throw new HttpResponseException(status, httpResponse.getStatusText());
        }

        HttpResponse<JsonNode> response = request.asJson();

        JSONObject userObject = response.getBody().getObject().getJSONObject("user");
        registered = userObject.getLong("registered");
        benis = userObject.getInt("score");
        mark = Mark.getById(userObject.getInt("mark"));
        banned = userObject.getInt("banned") == 1;

        if(banned) {
            if(!userObject.isNull("bannedUntil"))
            bannedUntil = LocalDateTime.ofInstant(Instant.ofEpochMilli(userObject.getLong("bannedUntil")), TimeZone.getTimeZone(ZoneOffset.ofHours(1)).toZoneId());
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

    public int getBenis() {
        return benis;
    }

    public Mark getMark() {
        return mark;
    }

    public boolean isGuest() {
        return host != null;
    }

    public UUID getHost() {
        return host;
    }

    public long getInvited() {
        return invited;
    }

    public boolean isAnonymized() {
        return anonymized;
    }

    public void setAnonymized(boolean anonymized) {
        DatabaseHelper.setAnonymized(uuid, anonymized);
        this.anonymized = anonymized;
    }

    public SortedSet<Badge> getBadges() {
        return Collections.unmodifiableSortedSet(badges);
    }

    public void awardBadge(Badge badge) throws SQLException {
        if(badges.contains(badge)) return;
        DatabaseHelper.awardBadge(uuid, badge);
        badges.add(badge);
    }

    public HoverEvent getHoverEvent() {

        MessageBuilder messageBuilder = MessageBuilder.DEFAULT;
        ComponentBuilder componentBuilder = new ComponentBuilder("");
        List<String> list;

        String bubble;

        if(isGuest()) {
            list = guestList;
            String hostName = "- Nicht verfügbar -";
            try {
                hostName = Shadow.NAME.getString(host);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            messageBuilder = messageBuilder
                    .define("PLAYER", hostName)
                    .define("DATE", new SimpleDateFormat("dd.MM.yy").format(new java.util.Date(invited)));
        } else {
            list = memberList;
            messageBuilder = messageBuilder
                    .define("PLAYER", anonymized ? ChatColor.MAGIC + "XXXXXXXX" : getName())
                    .define("BENIS", (getBenis() < 0 ? ChatColor.RED : "") + formatBenis(getBenis()))
                    .define("MARK", getMark().getColor() + getMark().getTitle());
        }

        for(int i = 0; i < list.size(); i++) {
            componentBuilder.append(new TextComponent(messageBuilder.build(list.get(i))).toLegacyText());
            if(i + 1 < list.size()) componentBuilder.append("\n");
        }

        if(!badges.isEmpty()) componentBuilder.append("\n");
        for(Badge badge : badges) {
            componentBuilder.append("\n" + badge.getTitle(), ComponentBuilder.FormatRetention.NONE).color(badge.getColor());
        }

        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, componentBuilder.create());
    }

    private static final NavigableMap<Integer, String> suffixes = new TreeMap<>() {{
        put(1_000, "k");
        put(1_000_000, "M");
    }};

    static String formatBenis(int value) {
        if (Math.abs(value) < 1000) {
            if(value < 0) {
                return "< 0";
            }
            return "< 1k";
        }

        Map.Entry<Integer, String> e = suffixes.floorEntry(Math.abs(value));
        Integer divideBy = e.getKey();
        String suffix = e.getValue();

        int truncated = Math.abs(value) / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        String result = hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
        return value < 0 ? "-" + result : result;
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
        MITTELALTSCHWUCHTEL("Mittelaltschwuchtel", '●', ChatColor.GREEN,3, 10),
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

    public enum Badge {
        EARLY_SUPPORTER("Early Supporter", ChatColor.DARK_AQUA);

        private String title;
        private ChatColor color;

        Badge(String title, ChatColor color) {
            this.title = title;
            this.color = color;
        }

        public String getTitle() {
            return title;
        }

        public ChatColor getColor() {
            return color;
        }
    }

}
