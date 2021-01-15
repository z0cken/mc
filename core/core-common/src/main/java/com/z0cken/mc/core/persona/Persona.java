package com.z0cken.mc.core.persona;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.z0cken.mc.core.CoreBridge;
import com.z0cken.mc.core.Shadow;
import com.z0cken.mc.core.util.ConfigurationType;
import com.z0cken.mc.core.util.MessageBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class Persona {

    private static final AsyncLoadingCache<String, BoardProfile> cache = Caffeine.newBuilder()
            .expireAfterAccess(CoreBridge.getPlugin().getConfigBridge(ConfigurationType.CORE).getInt("profile.expire"), TimeUnit.MINUTES)
            .refreshAfterWrite(CoreBridge.getPlugin().getConfigBridge(ConfigurationType.CORE).getInt("profile.refresh"), TimeUnit.MINUTES)
            .buildAsync(BoardProfile::of);

    private static final List<String> guestBubble = CoreBridge.getPlugin().getConfigBridge(ConfigurationType.CORE).getStringList("hover-event.guest");
    private static final List<String> memberBubble = CoreBridge.getPlugin().getConfigBridge(ConfigurationType.CORE).getStringList("hover-event.member");

    private final UUID uuid;

    private BoardProfile boardProfile;
    private boolean anonymized, acceptedTerms;
    private String boardName;

    //Guest
    private UUID host;
    private Long invited;

    private final SortedSet<Badge> badges;

    Persona(@Nonnull UUID uuid) throws SQLException {
        this.uuid = uuid;

        boardName = DatabaseHelper.getUsername(uuid);

        if(isVerified()) {
            anonymized = DatabaseHelper.isAnonymized(uuid);
        } else {
            String host = DatabaseHelper.getHost(uuid);
            if(host != null) {
                this.host = UUID.fromString(host);
                invited = DatabaseHelper.getInvited(uuid);
            }
        }

        badges = DatabaseHelper.getBadges(uuid);
        acceptedTerms = Shadow.TERMS.getBoolean(uuid);
    }

    public CompletableFuture<BoardProfile> getBoardProfile() {
        if(!isVerified()) throw new IllegalStateException();
        return cache.get(boardName);
    }

    public static final HoverEvent NOT_AVAILABLE = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("§cProfil nicht verfügbar")});
    public static final HoverEvent NOT_VERIFIED = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("§cSpieler nicht verifiziert")});

    private HoverEvent getHoverEvent() {
        StringBuilder badgeText = new StringBuilder(getBadges().isEmpty() ? "" : "\n");
        getBadges().forEach(badge -> badgeText.append("\n").append(badge.getColor()).append(badge.getTitle()));

        if(isVerified()) {
            BoardProfile profile;
            try {
                profile = getBoardProfile().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return NOT_AVAILABLE;
            }

            return profile == null ? NOT_AVAILABLE : new HoverEvent(HoverEvent.Action.SHOW_TEXT, MessageBuilder.DEFAULT
                    .define("NAME", anonymized ? ChatColor.MAGIC + "XXXXXXXX" : boardName)
                    .define("BENIS", (profile.getBenis() < 0 ? ChatColor.RED : "") + profile.getBenisFormatted())
                    .define("MARK", profile.getMark().getColor() + profile.getMark().getTitle())
                    .define("BADGES", badgeText.toString())
                    .build(memberBubble.stream().collect(Collectors.joining(ChatColor.RESET.toString()))));
        } else if(isGuest()) {

            String hostName = null;
            try { hostName = Shadow.NAME.getString(host);
            } catch (SQLException e) { e.printStackTrace(); }
            if(hostName == null) hostName = ChatColor.RED + "- Nicht verfügbar -";

            return  new HoverEvent(HoverEvent.Action.SHOW_TEXT, MessageBuilder.DEFAULT
                    .define("NAME", hostName)
                    .define("DATE", new SimpleDateFormat("dd.MM.yy").format(new java.util.Date(invited)))
                    .define("BADGES", badgeText.toString())
                    .build(guestBubble.stream().collect(Collectors.joining(ChatColor.RESET.toString()))));
        } else return NOT_VERIFIED;
    }

    public CompletableFuture<HoverEvent> getHoverEvent(long timeout, TimeUnit unit) {
        CompletableFuture<HoverEvent> future = new CompletableFuture<>();

        if(isVerified() || isGuest()) future.completeOnTimeout(NOT_AVAILABLE, timeout, unit).completeAsync(this::getHoverEvent);
        else future.complete(NOT_VERIFIED);

        return future;
    }

    public CompletableFuture<TextComponent> getComponent(String name, long timeout, TimeUnit unit) {
        CompletableFuture<TextComponent> future = new CompletableFuture<>();
        TextComponent component = new TextComponent(name);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + name + " "));

        if(isVerified() || isGuest()) {

            future.completeAsync(() -> {

                HoverEvent hoverEvent;
                try {
                    hoverEvent = getHoverEvent(timeout, unit).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    return component;
                }

                component.setHoverEvent(hoverEvent);

                if(isVerified()) {
                    final BoardProfile profile = getBoardProfile().getNow(null);
                    if(profile != null) {
                        if(CoreBridge.getPlugin().getConfigBridge(ConfigurationType.CORE).getBoolean("display-team") && profile.getTeam() != null) component.setColor(profile.getTeam().getColor());
                        component.addExtra(" " + profile.getMark().getSymbol());
                    }
                }

                return component;
            });

        } else {
            component.setHoverEvent(NOT_VERIFIED);
            future.complete(component);
        }

        return future;
    }

    public boolean isVerified() { return boardName != null; }

    public boolean isGuest() { return host != null; }

    public UUID getHost() { return host; }

    public long getInvited() { return invited; }

    public SortedSet<Badge> getBadges() { return Collections.unmodifiableSortedSet(badges); }

    public boolean isAnonymized() { return anonymized; }

    public void setAnonymized(boolean anonymized) {
        DatabaseHelper.setAnonymized(uuid, anonymized);
        this.anonymized = anonymized;
    }

    public boolean hasAcceptedTerms() { return acceptedTerms; }

    public void setAcceptedTerms(boolean b) {
        acceptedTerms = b;
        Shadow.TERMS.setBoolean(uuid, b);
    }

    public boolean addBadge(Badge badge) throws SQLException {
        if(!badges.add(badge)) return false;
        DatabaseHelper.addBadge(uuid, badge);
        return true;
    }

    public boolean removeBadge(Badge badge) throws SQLException {
        if(!badges.remove(badge)) return false;
        DatabaseHelper.removeBadge(uuid, badge);
        return true;
    }

    public enum Badge {
        EARLY_SUPPORTER("Früher Vogel", ChatColor.DARK_AQUA),
        MINION("Unverbesserlich", ChatColor.YELLOW),
        BEDWARS("Fiese Fliese", ChatColor.GRAY),
        COVID("Pr0mart Preller", ChatColor.GOLD);

        private final String title;
        private final ChatColor color;

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
