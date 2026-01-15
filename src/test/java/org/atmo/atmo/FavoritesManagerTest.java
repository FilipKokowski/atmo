package org.atmo.atmo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FavoritesManagerTest {

    private FavoritesManager manager;

    @BeforeEach
    void setUp() {
        manager = FavoritesManager.getInstance();
        manager.clearAll();
    }

    @Test
    void addCityToFavorites() {
        manager.addFavorite("TestCity");
        assertTrue(manager.isFavorite("TestCity"));
    }

    @Test
    void removeCityFromFavorites() {
        manager.addFavorite("TestCity");
        manager.removeFavorite("TestCity");

        assertFalse(manager.isFavorite("TestCity"));
    }

    @AfterEach
    void clear() {
        manager = FavoritesManager.getInstance();
        manager.clearAll();
    }
}