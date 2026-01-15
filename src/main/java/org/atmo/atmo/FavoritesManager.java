package org.atmo.atmo;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FavoritesManager {
    private static FavoritesManager instance;
    private static final String FILE_PATH = "favorites.json";
    private final List<String> favorites = new ArrayList<>();

    private FavoritesManager() { loadFromFile(); }

    public static synchronized FavoritesManager getInstance() {
        if (instance == null) instance = new FavoritesManager();
        return instance;
    }

    public void addFavorite(String city) {
        if (city == null || city.isBlank()) return;
        if (!isFavorite(city)) {
            favorites.add(city.trim());
            saveToFile();
        }
    }

    public void removeFavorite(String city) {
        favorites.removeIf(f -> f.equalsIgnoreCase(city.trim()));
        saveToFile();
    }

    public boolean isFavorite(String city) {
        if (city == null) return false;
        return favorites.stream().anyMatch(f -> f.equalsIgnoreCase(city.trim()));
    }

    public List<String> getFavorites() { return new ArrayList<>(favorites); }

    private void saveToFile() {
        try {
            JSONObject json = new JSONObject();
            json.put("favorites", new JSONArray(favorites));
            Files.writeString(Paths.get(FILE_PATH), json.toString(4));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadFromFile() {
        try {
            File file = new File(FILE_PATH);
            if (file.exists()) {
                JSONObject json = new JSONObject(Files.readString(Paths.get(FILE_PATH)));
                JSONArray array = json.getJSONArray("favorites");
                favorites.clear();
                for (int i = 0; i < array.length(); i++) favorites.add(array.getString(i));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void clearAll() {
        favorites.clear();
        saveToFile();
    }
}