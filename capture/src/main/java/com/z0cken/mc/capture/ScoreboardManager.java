package com.z0cken.mc.capture;

import net.minecraft.server.v1_15_R1.ScoreboardObjective;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_15_R1.scoreboard.CraftScoreboard;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.reflect.Field;
import java.util.Arrays;

public class ScoreboardManager {
    private static final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

    private static Objective buffer = scoreboard.registerNewObjective("a", "dummy", ChatColor.GRAY + "Spielstand"), live = scoreboard.registerNewObjective("b", "dummy", ChatColor.GRAY + "Spielstand");
    private static String[] content = new String[4];
    static {
        Arrays.fill(content, "");

        Objective objective = scoreboard.registerNewObjective("showhealth", "health", ChatColor.RED+"\u2764");
        objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
    }

    private ScoreboardManager() {}

    public static Scoreboard getScoreboard() {
        return scoreboard;
    }

    public static void setText(int i, String text) {
        i = content.length - i;

        removeScore(buffer, content[i]);
        buffer.getScore(text).setScore(i);

        swapBuffer();

        removeScore(buffer, content[i]);
        buffer.getScore(text).setScore(i);

        content[i] = text;
    }

    private static void removeScore(Objective objective, String score) {
        ScoreboardObjective nmsObjective = null;
        try {
            Field field = Class.forName("org.bukkit.craftbukkit.v1_13_R2.scoreboard.CraftObjective").getDeclaredField("objective");
            field.setAccessible(true);
            nmsObjective = (ScoreboardObjective) field.get(objective);
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }
        ((CraftScoreboard)scoreboard).getHandle().resetPlayerScores(score, nmsObjective);
    }

    private static void swapBuffer() {
        buffer.setDisplaySlot(DisplaySlot.SIDEBAR);
        Objective temp = live;
        live = buffer;
        buffer = temp;
    }
}
