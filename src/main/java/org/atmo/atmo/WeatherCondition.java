package org.atmo.atmo;

public enum WeatherCondition {
    CLEAR(0, "Czyste niebo", "sun.png"),
    MAINLY_CLEAR(1, "Głównie bezchmurnie", "sun.png"),

    PARTLY_CLOUDY(2, "Częściowe zachmurzenie", "cloud.png"),
    OVERCAST(3, "Pochmurno", "cloud.png"),
    FOG(45, "Mgła", "cloud.png"),
    RIME_FOG(48, "Mgła osadzająca szadź", "cloud.png"),

    DRIZZLE_LIGHT(51, "Lekka mżawka", "rain.png"),
    DRIZZLE_MODERATE(53, "Umiarkowana mżawka", "rain.png"),
    DRIZZLE_DENSE(55, "Gęsta mżawka", "rain.png"),
    RAIN_LIGHT(61, "Słaby deszcz", "rain.png"),
    RAIN_MODERATE(63, "Umiarkowany deszcz", "rain.png"),
    RAIN_HEAVY(65, "Ulewa", "rain.png"),
    RAIN_SHOWERS(80, "Przelotne opady", "rain.png"),
    THUNDERSTORM(95, "Burza", "rain.png"),
    THUNDERSTORM_HAIL(99, "Burza z gradem", "rain.png"),

    SNOW_LIGHT(71, "Słaby śnieg", "snow.png"),
    SNOW_MODERATE(73, "Umiarkowany śnieg", "snow.png"),
    SNOW_HEAVY(75, "Śnieżyca", "snow.png"),
    SNOW_SHOWERS(85, "Przelotne opady śniegu", "snow.png"),

    UNKNOWN(-1, "Nieznane", "cloud.png");

    private final int code;
    private final String description;
    private final String iconFileName;

    WeatherCondition(int code, String description, String iconFileName) {
        this.code = code;
        this.description = description;
        this.iconFileName = iconFileName;
    }

    public String getDescription() {
        return description;
    }

    public String getIconFileName() {
        return iconFileName;
    }

    public static WeatherCondition fromCode(int code) {
        if (code >= 51 && code <= 67) return RAIN_MODERATE;
        if (code >= 71 && code <= 77) return SNOW_MODERATE;
        if (code >= 80 && code <= 82) return RAIN_SHOWERS;
        if (code >= 85 && code <= 86) return SNOW_SHOWERS;
        if (code >= 95 && code <= 99) return THUNDERSTORM;
        return UNKNOWN;
    }
}