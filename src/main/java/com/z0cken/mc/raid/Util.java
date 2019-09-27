package com.z0cken.mc.raid;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.z0cken.mc.raid.json.BukkitExclusionStrategy;
import com.z0cken.mc.raid.json.LocationAdapter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
public final class Util {

    private Util() {}
    private static final Gson gson;

    static {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                //.registerTypeHierarchyAdapter(ConfigurationSerializable.class, new BukkitTypeAdapter())
                //.registerTypeHierarchyAdapter(BukkitRunnable.class, new BukkitRunnableAdapter())
                .registerTypeHierarchyAdapter(Location.class, new LocationAdapter())
                .setExclusionStrategies(new BukkitExclusionStrategy())
                /*.registerTypeAdapter(Float.class, (JsonSerializer<Float>) (src, typeOfSrc, context) -> {
                    if(src == src.intValue()) return new JsonPrimitive(src.intValue());
                    return new JsonPrimitive(src);
                })
                .registerTypeAdapter(Double.class, (JsonSerializer<Double>) (src, typeOfSrc, context) -> {
                    if(src == src.intValue()) return new JsonPrimitive(src.intValue());
                    return new JsonPrimitive(src);
                })*/
                .create();
    }

    public static Gson getGson() {
        return gson;
    }

    public static <T> List[] splitHalf(List<T> list) {
        List<T> first = new ArrayList<>();
        List<T> second = new ArrayList<>();

        int size = list.size();

        for (int i = 0; i < size / 2; i++)
            first.add(list.get(i));

        for (int i = size / 2; i < size; i++)
            second.add(list.get(i));

        return new List[] { first, second };
    }

    public static double getHorizontalDistanceSq(Location l1, Location l2) {
        l1.setY(0);
        l2.setY(0);

        return l1.distanceSquared(l2);
    }

    public static boolean isNegative(PotionEffectType type) {
        switch (type.getId()) {
            case 2:
            case 4:
            case 7:
            case 9:
            case 15:
            case 17:
            case 18:
            case 19:
            case 20:
            case 24:
            case 27:
                return true;
            default:
                return false;
        }
    }

    public static void saveCollection(Collection collection, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(gson.toJson(collection));
            writer.flush();
        }
    }

    public static <T> void loadCollection(Collection<T> set, File file, TypeToken<? extends Collection<T>> token) throws FileNotFoundException {
        set.clear();
        final Collection<? extends T> collection = gson.fromJson(new FileReader(file), token.getType());
        if(collection != null) set.addAll(collection);
    }

    public static void saveMap(Map map, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(gson.toJson(map));
            writer.flush();
        }
    }

    public static <K, V> void loadMap(Map<K, V> collection, File file,  TypeToken<? extends Map<K, V>> token) throws FileNotFoundException {
        collection.clear();
        collection.putAll(gson.fromJson(new FileReader(file), token.getType()));
    }

    public static void runAndCatch(ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface ThrowingRunnable<T extends Exception> {
        void run() throws T;
    }

    public static void clearPotionEffects(Player player) {
        player.getActivePotionEffects().forEach(pe -> player.removePotionEffect(pe.getType()));
    }

    public static void cleanPlayer(Player player, boolean clearXp) {
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        clearPotionEffects(player);
        if(clearXp) {
            player.setTotalExperience(0);
            player.setLevel(0);
        }
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.getInventory().clear();
    }

    public static boolean isLikelyParticipant(Player player) {
        return true; //TODO Enable
       //return !player.isOp() && player.getGameMode() == GameMode.SURVIVAL;
    }

    public static void writePlayerStats(List<GamePlayer> players, File csvFile) {
        System.out.println("Saving points to " + csvFile.getName());

        try {
            csvFile.createNewFile();

            FileWriter csvWriter = new FileWriter(csvFile);
            csvWriter.append("UUID");
            csvWriter.append(",");
            csvWriter.append("Score");
            csvWriter.append("\n");

            for (GamePlayer player : players) {
                csvWriter.append(String.join(",", player.getUniqueId().toString(), Integer.toString(player.getScore())));
                csvWriter.append("\n");
            }

            csvWriter.flush();
            csvWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            players.forEach(gp -> System.out.printf("%s:%d", gp.getUniqueId().toString(), gp.getScore()));
        }
    }
}
