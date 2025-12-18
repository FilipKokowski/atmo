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

    // Główne okno (do zmiany tła)
    @FXML private VBox rootPane;

    // Pola tekstowe i wybór
    @FXML private TextField cityInput;
    @FXML private ComboBox<String> themeSelector;

    // Etykiety
    @FXML private Label cityNameLabel;
    @FXML private Label temperatureLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label highLowLabel;

    // Kontenery na prognozy
    @FXML private HBox hourlyContainer;
    @FXML private HBox dailyContainer;

    private final WeatherService weatherService = new WeatherService();

    @FXML
    private void initialize() {
        if (themeSelector != null) {
            themeSelector.getItems().addAll("Chmurki", "Chmurki 2", "Chmurki 3", "Chmurki 4");
            themeSelector.setValue("Chmurki");

            themeSelector.setOnAction(event -> changeTheme(themeSelector.getValue()));
        }

        if (rootPane != null) {
            rootPane.getStyleClass().add("theme-clouds");
        }

        loadWeatherDataForCity("Szczecin");
    }

    private void changeTheme(String themeName) {
        if (rootPane == null) return;

        rootPane.getStyleClass().removeAll("theme-clouds", "theme-clouds2", "theme-clouds3", "theme-cr");

        switch (themeName) {
            case "Chmurki" -> rootPane.getStyleClass().add("theme-clouds");
            case "Chmurki 2"    -> rootPane.getStyleClass().add("theme-clouds2");
            case "Chmurki 3" -> rootPane.getStyleClass().add("theme-clouds3");
            case "Chmurki 4" -> rootPane.getStyleClass().add("theme-cr");
        }
    }

    @FXML
    private void handleSearch() {
        String cityName = cityInput.getText();
        if (cityName != null && !cityName.trim().isEmpty()) {
            loadWeatherDataForCity(cityName);
        }
    }

    private void loadWeatherDataForCity(String cityName) {
        new Thread(() -> {
            try {
                // 1. Pobieramy współrzędne ORAZ oficjalną nazwę
                WeatherService.Coordinates coords = weatherService.getCoordinatesForCity(cityName);

                if (coords == null) {
                    Platform.runLater(() -> {
                        cityNameLabel.setText("Nie znaleziono");
                        // ... reszta obsługi błędu ...
                    });
                    return;
                }

                // 2. Pobieramy pogodę, ale przekazujemy OFICJALNĄ nazwę z coords
                WeatherService.WeatherData data = weatherService.getWeatherData(
                        coords.locationName(), // <--- ZMIANA TUTAJ (było cityName)
                        String.valueOf(coords.latitude()),
                        String.valueOf(coords.longitude())
                );

                Platform.runLater(() -> updateUI(data));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    private void updateUI(WeatherService.WeatherData data) {
        cityNameLabel.setText(data.city());
        temperatureLabel.setText(String.format("%.0f°", data.currentTemp()));
        descriptionLabel.setText(data.currentCondition().getDescription());

        if (!data.dailyForecasts().isEmpty()) {
            WeatherService.DailyForecast today = data.dailyForecasts().get(0);
            highLowLabel.setText(String.format("↑: %.0f°  ↓: %.0f°", today.maxTemp(), today.minTemp()));
        }

        hourlyContainer.getChildren().clear();
        for (WeatherService.HourlyForecast h : data.hourlyForecasts()) {
            hourlyContainer.getChildren().add(createForecastTile(h.time(), h.condition().getIconFileName(), String.format("%.0f°", h.temp())));
        }

        dailyContainer.getChildren().clear();
        for (WeatherService.DailyForecast d : data.dailyForecasts()) {
            dailyContainer.getChildren().add(createForecastTile(d.day(), d.condition().getIconFileName(), String.format("%.0f°", d.maxTemp())));
        }
    }

    private VBox createForecastTile(String timeText, String iconName, String tempText) {
        VBox box = new VBox(5);
        box.getStyleClass().add("forecast-tile");

        Label timeLabel = new Label(timeText);
        timeLabel.getStyleClass().add("forecast-time");

        ImageView icon = new ImageView();
        try {
            String path = "/org/atmo/atmo/" + iconName; // Pełna ścieżka dla pewności
            if (getClass().getResource(iconName) != null) {
                icon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconName))));
            } else {
                icon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("cloud.png"))));
            }
            icon.setFitWidth(40);
            icon.setFitHeight(40);
        } catch (Exception e) {}

        Label tempLabel = new Label(tempText);
        tempLabel.getStyleClass().add("forecast-temp");

        box.getChildren().addAll(timeLabel, icon, tempLabel);
        return box;
    }
}