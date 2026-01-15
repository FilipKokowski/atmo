package org.atmo.atmo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WeatherService {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public record Coordinates(double latitude, double longitude, String locationName) {}

    public record WeatherData(
            String city,
            double currentTemp,
            WeatherCondition currentCondition,
            List<HourlyForecast> hourlyForecasts,
            List<DailyForecast> dailyForecasts
    ) {}

    public record HourlyForecast(String time, double temp, WeatherCondition condition) {}
    public record DailyForecast(String day, double maxTemp, double minTemp, WeatherCondition condition) {}

    public Coordinates getCoordinatesForCity(String cityName) throws Exception {
        String encodedCityName = URLEncoder.encode(cityName, StandardCharsets.UTF_8);
        String apiUrl = String.format(
                "https://geocoding-api.open-meteo.com/v1/search?name=%s&count=1&language=pl&format=json",
                encodedCityName
        );

        HttpRequest request = HttpRequest.newBuilder().uri(new URI(apiUrl)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject root = new JSONObject(response.body());
        JSONArray results = root.optJSONArray("results");

        if (results == null || results.isEmpty()) {
            return null;
        }

        JSONObject firstResult = results.getJSONObject(0);
        double latitude = firstResult.getDouble("latitude");
        double longitude = firstResult.getDouble("longitude");

        String name = firstResult.getString("name");
        String country = firstResult.optString("country", "");

        String fullName = name;
        if (!country.isEmpty()) {
            fullName += " (" + country + ")";
        }

        return new Coordinates(latitude, longitude, fullName);
    }

    public WeatherData getWeatherData(String cityName, String latitude, String longitude) throws Exception {
        String apiUrl = String.format(
                "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s" +
                        "&current=temperature_2m,weather_code" +
                        "&hourly=temperature_2m,weather_code" +
                        "&daily=weather_code,temperature_2m_max,temperature_2m_min" +
                        "&timezone=auto",
                latitude, longitude
        );

        HttpRequest request = HttpRequest.newBuilder().uri(new URI(apiUrl)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject root = new JSONObject(response.body());

        //System.out.println(root);

        JSONObject current = root.getJSONObject("current");
        double currentTemp = current.getDouble("temperature_2m");
        int currentCode = current.getInt("weather_code");

        JSONObject hourly = root.getJSONObject("hourly");
        JSONArray hourlyTimes = hourly.getJSONArray("time");
        JSONArray hourlyTemps = hourly.getJSONArray("temperature_2m");
        JSONArray hourlyCodes = hourly.getJSONArray("weather_code");

        List<HourlyForecast> hourlyList = new ArrayList<>();
        int currentHourIndex = findCurrentHourIndex(hourlyTimes);
        for (int i = currentHourIndex; i < currentHourIndex + 24 && i < hourlyTimes.length(); i++) {
            String timeStr = hourlyTimes.getString(i);
            LocalDateTime dt = LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String hourFormatted = dt.format(DateTimeFormatter.ofPattern("HH:mm"));

            hourlyList.add(new HourlyForecast(
                    hourFormatted,
                    hourlyTemps.getDouble(i),
                    WeatherCondition.fromCode(hourlyCodes.getInt(i))
            ));
        }

        JSONObject daily = root.getJSONObject("daily");
        JSONArray dailyTimes = daily.getJSONArray("time");
        JSONArray dailyMax = daily.getJSONArray("temperature_2m_max");
        JSONArray dailyMin = daily.getJSONArray("temperature_2m_min");
        JSONArray dailyCodes = daily.getJSONArray("weather_code");

        List<DailyForecast> dailyList = new ArrayList<>();
        for (int i = 0; i < dailyTimes.length(); i++) {
            String dateStr = dailyTimes.getString(i);
            LocalDate date = LocalDate.parse(dateStr);
            String dayName = translateDayOfWeek(date.getDayOfWeek().getValue());

            dailyList.add(new DailyForecast(
                    dayName + " " + date.format(DateTimeFormatter.ofPattern("dd.MM")),
                    dailyMax.getDouble(i),
                    dailyMin.getDouble(i),
                    WeatherCondition.fromCode(dailyCodes.getInt(i))
            ));
        }

        return new WeatherData(cityName, currentTemp, WeatherCondition.fromCode(currentCode), hourlyList, dailyList);
    }

    private int findCurrentHourIndex(JSONArray times) {
        LocalDateTime now = LocalDateTime.now();
        for(int i=0; i<times.length(); i++) {
            LocalDateTime dt = LocalDateTime.parse(times.getString(i));
            if(dt.isAfter(now) || dt.isEqual(now)) return i;
        }
        return 0;
    }

    private String translateDayOfWeek(int day) {
        return switch (day) {
            case 1 -> "Pon"; case 2 -> "Wt"; case 3 -> "Śr";
            case 4 -> "Czw"; case 5 -> "Pt"; case 6 -> "Sob"; case 7 -> "Ndz";
            default -> "";
        };
    }
}