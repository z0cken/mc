package com.z0cken.mc.core.util;

import net.md_5.bungee.api.chat.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author Flare */
@SuppressWarnings({"unused", "WeakerAccess"})
public class MessageBuilder implements Cloneable {

    public static final MessageBuilder DEFAULT = new MessageBuilder();

    private static final Pattern PATTERN_COMPONENT = Pattern.compile("(?<=\\{#).+?(?=#})");

    private HashMap<String, TextComponent> components = new HashMap<>();
    private HashMap<String, String> values = new HashMap<>();
    private HashMap<String, Object> events = new HashMap<>();

    private static final Map<String, Object> ACTIONS = Map.of(
        "PAGE", ClickEvent.Action.CHANGE_PAGE,
        "FILE", ClickEvent.Action.OPEN_FILE,
        "URL", ClickEvent.Action.OPEN_URL,
        "RUN", ClickEvent.Action.RUN_COMMAND,
        "SUGGEST", ClickEvent.Action.SUGGEST_COMMAND,
        "ACHIEVEMENT", HoverEvent.Action.SHOW_ACHIEVEMENT,
        "ENTITY", HoverEvent.Action.SHOW_ENTITY,
        "ITEM", HoverEvent.Action.SHOW_ITEM,
        "TEXT", HoverEvent.Action.SHOW_TEXT
    );

    public MessageBuilder() {}


    public BaseComponent[] build(@Nonnull String message) {

        message = setValues(message);

        Matcher matcher = PATTERN_COMPONENT.matcher(message);

        int index = 0;
        ComponentBuilder builder = new ComponentBuilder("");
        if(matcher.find()) {
            do {
                builder.append(message.substring(index + (index == 0 ? 0 : 2), matcher.start() - 2));
                index = matcher.end();

                builder.reset().append(parseComponent(message.substring(matcher.start(), matcher.end())));

            } while(matcher.find());

            //Final segment
            String substring = message.substring(Math.min(index + 2, message.length()));
            builder.append(substring).retain(ComponentBuilder.FormatRetention.NONE);

            return builder.create();
        }


        return builder.reset().append(message).create();
    }

    public String setValues(@Nonnull String message) {
        for(Map.Entry<String, String> entry : values.entrySet()) {
            message = message.replaceAll("(?i)\\{" + entry.getKey() + "}", entry.getValue() == null ? "" : entry.getValue());
        }

        message = message.replaceAll("\\{[^#].+[^#]}", "");
        return message;
    }

    private TextComponent parseComponent(String literalComponent) {
        Matcher matcher = Pattern.compile("(?<=\\[).+?(?=(?<!\\\\)])").matcher(literalComponent);
        List<String> allMatches = new ArrayList<>();
        while (matcher.find()) {
            allMatches.add(matcher.group());
        }
        int index = allMatches.size() - 1;
        String last = allMatches.get(index).replaceAll(Pattern.quote("\\]"), "]");

        //Preset component
        if(allMatches.size() == 1) {
            TextComponent c = components.getOrDefault(last, null);
            if(c != null) return c;
        }

        TextComponent component = new TextComponent(last);
        allMatches.remove(index);

        if(allMatches.size() < 1 || allMatches.size() > 2) throw new IllegalArgumentException("Illegal amount of Event arguments: " + literalComponent);

        for(String match : allMatches) {
            String[] array = match.split("(?<!\\\\)" + Pattern.quote("|"));

            String s = array[0].toUpperCase();

            if(array.length == 1) {

                Object event = events.getOrDefault(s, null);

                if(event != null) {
                    if(event instanceof ClickEvent) component.setClickEvent((ClickEvent) event);
                    else component.setHoverEvent((HoverEvent) event);
                }

            } else if(array.length == 2) {
                //Clean up escape characters
                String value = array[1].replaceAll(Pattern.quote("\\|"), "|");

                Object action = ACTIONS.get(s);

                if(action != null) {

                    if(action instanceof ClickEvent.Action) component.setClickEvent(new ClickEvent((ClickEvent.Action) action, value));
                    else component.setHoverEvent(new HoverEvent((HoverEvent.Action) action, new ComponentBuilder(value).create()));

                } else throw new IllegalArgumentException("Action not found: " + s);

            } else throw new IllegalArgumentException("Illegal amount of arguments in Event: " + match + " of component " + literalComponent);
        }

        return component;
    }

    public MessageBuilder define(String key, @Nullable String value) {
        MessageBuilder clone = clone();
        clone.values.put(key.toUpperCase(), value);
        return clone;
    }

    public MessageBuilder define(String key, @Nullable ClickEvent value) {
        MessageBuilder clone = clone();
        clone.events.put(key.toUpperCase(), value);
        return clone;
    }

    public MessageBuilder define(String key, @Nullable HoverEvent value) {
        MessageBuilder clone = clone();
        clone.events.put(key.toUpperCase(), value);
        return clone;
    }

    public MessageBuilder define(String key, @Nullable TextComponent value) {
        MessageBuilder clone = clone();
        clone.components.put(key.toUpperCase(), value);
        return clone;
    }

    @Override
    public MessageBuilder clone() {
        MessageBuilder builder;
        try {
            builder = (MessageBuilder) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
        builder.values = (HashMap<String, String>) builder.values.clone();
        builder.events = (HashMap<String, Object>) builder.events.clone();
        builder.components = (HashMap<String, TextComponent>) builder.components.clone();
        return builder;
    }
}
