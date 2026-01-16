package org.atmo.atmo;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.Objects;

public class HelloController {

    @FXML private VBox rootPane;
    @FXML private TextField cityInput;
    @FXML private ComboBox<String> themeSelector;
    @FXML private ComboBox<String> favoritesSelector;

    @FXML private Label cityNameLabel;
    @FXML private Label starIcon;
    @FXML private Label temperatureLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label highLowLabel;

    @FXML private HBox hourlyContainer;
    @FXML private HBox dailyContainer;

    private final WeatherService weatherService = new WeatherService();
    private String currentCityRawName = "";

    @FXML
    private void initialize() {
        if (themeSelector != null) {
            themeSelector.getItems().addAll("Chmurki", "Chmurki 2", "Chmurki 3", "Chmurki 4");
            themeSelector.setValue("Chmurki");
            themeSelector.setOnAction(e -> changeTheme(themeSelector.getValue()));
        }

        Platform.runLater(() -> {
            if (rootPane != null) {
                changeTheme("Chmurki");
            }
        });

        refreshFavoritesList();
        
        if (favoritesSelector != null) {
            favoritesSelector.setOnAction(e -> {
                String selected = favoritesSelector.getValue();
                if (selected != null && !selected.isEmpty()) loadWeatherDataForCity(selected);
            });
        }

        loadWeatherDataForCity("Szczecin");
    }

    private void changeTheme(String themeName) {
        if (rootPane == null) return;
        rootPane.getStyleClass().removeAll("theme-clouds", "theme-clouds2", "theme-clouds3", "theme-cr");

        switch (themeName) {
            case "Chmurki"   -> rootPane.getStyleClass().add("theme-clouds");
            case "Chmurki 2" -> rootPane.getStyleClass().add("theme-clouds2");
            case "Chmurki 3" -> rootPane.getStyleClass().add("theme-clouds3");
            case "Chmurki 4" -> rootPane.getStyleClass().add("theme-cr");
        }
    }

    private void refreshFavoritesList() {
        Platform.runLater(() -> {
            if (favoritesSelector != null) {
                favoritesSelector.getItems().clear();
                favoritesSelector.getItems().addAll(FavoritesManager.getInstance().getFavorites());
            }
        });
    }

    private void updateStarIcon(String city) {
        if (starIcon != null) {
            starIcon.setText(FavoritesManager.getInstance().isFavorite(city) ? "★" : "☆");
        }
    }

    @FXML
    private void handleToggleFavorite() {
        if (currentCityRawName == null || currentCityRawName.isEmpty()) return;
        
        if (FavoritesManager.getInstance().isFavorite(currentCityRawName)) {
            FavoritesManager.getInstance().removeFavorite(currentCityRawName);
        } else {
            FavoritesManager.getInstance().addFavorite(currentCityRawName);
        }
        updateStarIcon(currentCityRawName);
        refreshFavoritesList();
    }

    @FXML
    private void handleSearch() {
        String cityName = cityInput.getText();
        if (cityName != null && !cityName.trim().isEmpty()) loadWeatherDataForCity(cityName);
    }

    private void loadWeatherDataForCity(String cityName) {
        Platform.runLater(() -> { if (starIcon != null) starIcon.setText("☆"); });

        new Thread(() -> {
            try {
                WeatherService.Coordinates coords = weatherService.getCoordinatesForCity(cityName);
                if (coords == null) {
                    Platform.runLater(() -> cityNameLabel.setText("Nie znaleziono"));
                    return;
                }

                String fullName = coords.locationName();
                String rawName = fullName.contains(" (") ? fullName.split(" \\(")[0].trim() : fullName.trim();

                WeatherService.WeatherData data = weatherService.getWeatherData(
                        fullName,
                        String.valueOf(coords.latitude()),
                        String.valueOf(coords.longitude())
                );

                Platform.runLater(() -> {
                    this.currentCityRawName = rawName;
                    updateUI(data);
                    updateStarIcon(rawName);
                });
            } catch (Exception e) { 
                e.printStackTrace(); 
            }
        }).start();
    }

    private void updateUI(WeatherService.WeatherData data) {
        if (cityNameLabel != null) cityNameLabel.setText(data.city());
        if (temperatureLabel != null) temperatureLabel.setText(String.format("%.0f°", data.currentTemp()));
        if (descriptionLabel != null) descriptionLabel.setText(data.currentCondition().getDescription());

        if (data.dailyForecasts() != null && !data.dailyForecasts().isEmpty() && highLowLabel != null) {
            WeatherService.DailyForecast today = data.dailyForecasts().get(0);
            highLowLabel.setText(String.format("↑: %.0f°  ↓: %.0f°", today.maxTemp(), today.minTemp()));
        }

        if (hourlyContainer != null) {
            hourlyContainer.getChildren().clear();
            for (WeatherService.HourlyForecast h : data.hourlyForecasts()) {
                hourlyContainer.getChildren().add(createForecastTile(h.time(), h.condition().getIconFileName(), String.format("%.0f°", h.temp())));
            }
        }

        if (dailyContainer != null) {
            dailyContainer.getChildren().clear();
            for (WeatherService.DailyForecast d : data.dailyForecasts()) {
                dailyContainer.getChildren().add(createForecastTile(d.day(), d.condition().getIconFileName(), String.format("%.0f°", d.maxTemp())));
            }
        }
    }

    private VBox createForecastTile(String time, String iconName, String temp) {
        VBox box = new VBox(5);
        box.getStyleClass().add("forecast-tile");

        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("forecast-time");

        ImageView iv = new ImageView();
        try {
            // Próba załadowania ikony, jeśli nie ma - ładujemy domyślną cloud.png
            if (getClass().getResource(iconName) != null) {
                iv.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconName))));
            } else {
                iv.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("cloud.png"))));
            }
            iv.setFitWidth(40);
            iv.setFitHeight(40);
        } catch (Exception e) {
            System.err.println("Nie udało się załadować ikony: " + iconName);
        }

        Label tempLabel = new Label(temp);
        tempLabel.getStyleClass().add("forecast-temp");

        box.getChildren().addAll(timeLabel, iv, tempLabel);
        return box;
    }
}