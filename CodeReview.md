# Code Review: Projekt Aplikacji Pogodowej "Atmo"

## 1. Wstęp
Dokument ten stanowi techniczną analizę kodu źródłowego aplikacji "Atmo". Celem przeglądu jest weryfikacja zgodności projektu z założeniami programowania obiektowego (OOP), wykorzystania wzorców projektowych oraz poprawności implementacji w języku Java (JDK 11+).

Projekt realizuje funkcjonalność pobierania danych pogodowych z zewnętrznego API oraz zarządzania listą ulubionych lokalizacji.

## 2. Implementacja Zasad Programowania Obiektowego

### A. Hermetyzacja (Encapsulation)
**Lokalizacja:** `WeatherService.java`, `FavoritesManager.java`
**Opis:** Kod ukrywa wewnętrzny stan klas i udostępnia go jedynie poprzez zdefiniowane interfejsy. Chroni to przed niepożądaną modyfikacją danych z zewnątrz.

**Fragmenty kodu:**
W klasie `WeatherService` klient HTTP jest prywatny i oznaczony jako `final`, co uniemożliwia jego podmianę lub dostęp spoza klasy:
```java
public class WeatherService {
    // Pole prywatne, niedostępne dla innych klas
    private final HttpClient httpClient = HttpClient.newHttpClient();
    // ...
}
```

W klasie `FavoritesManager` lista ulubionych miast jest prywatna. Dostęp do niej jest możliwy tylko przez metody `getFavorites` (zwracającą kopię listy) lub metody modyfikujące:
```java
public class FavoritesManager {
    // Prywatna lista - brak bezpośredniego dostępu
    private final List<String> favorites = new ArrayList<>();

    public List<String> getFavorites() { 
        // Zwracanie kopii listy, aby chronić oryginał
        return new ArrayList<>(favorites); 
    }
}
```

### B. Polimorfizm (Polymorphism)

**Lokalizacja:** `WeatherCondition.java`, `HelloController.java` 
**Opis:** Wykorzystanie typów, które mogą przyjmować wiele form lub zachowań. W projekcie widoczne jest to w użyciu Enuma (który jest obiektem) oraz interfejsów funkcyjnych w obsłudze zdarzeń UI.

**Fragmenty kodu:** Enum `WeatherCondition` przechowuje stan (opis, ikonę) specyficzny dla każdej stałej. Metoda `getIconFileName()` zadziała poprawnie niezależnie od tego, czy obiektem jest `SUN` czy `RAIN`:
```java
public enum WeatherCondition {
    CLEAR(0, "Czyste niebo", "sun.png"),
    THUNDERSTORM(95, "Burza", "rain.png");

    private final String iconFileName;

    // Metoda wspólna, ale zwracająca różne dane dla różnych instancji enuma
    public String getIconFileName() {
        return iconFileName;
    }
}
```

### C. Dziedziczenie (Inheritance)

**Lokalizacja:** `Main.java` 
**Opis:** Klasa główna rozszerza funkcjonalność frameworka JavaFX, dziedzicząc po klasie `Application`. Dzięki temu przejmuje cykl życia aplikacji okienkowej.

**Fragmenty kodu:**
```java
// Dziedziczenie po klasie bibliotecznej Application
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Nadpisanie metody start() w celu inicjalizacji okna
        URL fxmlUrl = getClass().getResource("weather-view.fxml");
        // ...
    }
}
```

## 3. Zastosowane Wzorce Projektowe

### A. Wzorzec Singleton (Singleton Pattern)

**Lokalizacja:** `FavoritesManager.java` 
**Opis:** Wzorzec ten gwarantuje, że w całej aplikacji istnieje tylko jedna instancja menedżera ulubionych miast. Jest to kluczowe dla spójności danych (synchronizacja zapisu/odczytu z pliku).

**Fragmenty kodu:**

1.  **Prywatne pole statyczne** przechowywujące jedyną instancję:
	```java
	private static FavoritesManager instance;
	```
2. **Prywatny konstruktor** blokujący tworzenie obiektów operatorem `new`:
	```java
	private FavoritesManager() { loadFromFile(); }
	```
3. **Statyczna metoda dostępowa** (z `synchronized` dla bezpieczeństwa wątkowego):
	```java
	public static synchronized FavoritesManager getInstance() {
	    if (instance == null) instance = new FavoritesManager();
	    return instance;
	}
	```
### B. Wzorzec Fabryki (Static Factory Method)

**Lokalizacja:** `WeatherCondition.java` 
**Opis:** Metoda statyczna, która przejmuje odpowiedzialność za tworzenie instancji obiektu na podstawie podanych parametrów. Ukrywa skomplikowaną logikę mapowania kodów API na typy wyliczeniowe.

**Fragmenty kodu:**
```java
public static WeatherCondition fromCode(int code) {
    // Logika decyzyjna ukryta wewnątrz fabryki
    if (code >= 51 && code <= 67) return RAIN_MODERATE;
    if (code >= 71 && code <= 77) return SNOW_MODERATE;
    // ...
    return UNKNOWN; // Domyślny obiekt w przypadku braku dopasowania
}
```
## 4. Nowoczesne Funkcje Javy (Java 11 - 17)

### A. Rekordy (Records) – Java 14+

**Lokalizacja:** `WeatherService.java` 
**Opis:** Zastosowanie rekordów do modelowania danych (DTO - Data Transfer Objects). Zastępują one rozwlekłe klasy POJO, automatycznie generując konstruktory, gettery (`latitude()`), `equals`, `hashCode` i `toString`.

**Fragmenty kodu:** Definicja struktury danych w jednej linijce:
```java
public record Coordinates(double latitude, double longitude, String locationName) {}

public record HourlyForecast(String time, double temp, WeatherCondition condition) {}
```

### B. Nowa składnia Switch (Switch Expressions) – Java 14+

**Lokalizacja:** `WeatherService.java` 
**Opis:** Zwięzła forma instrukcji sterującej `switch`, która zwraca wartość i eliminuje konieczność używania słowa kluczowego `break`.

**Fragmenty kodu:**
```java
private String translateDayOfWeek(int day) {
    return switch (day) {
        case 1 -> "Pon"; case 2 -> "Wt"; case 3 -> "Śr";
        case 4 -> "Czw"; case 5 -> "Pt"; case 6 -> "Sob"; case 7 -> "Ndz";
        default -> "";
    };
}
```

### C. HttpClient – Java 11

**Lokalizacja:** `WeatherService.java` 
**Opis:** Użycie nowoczesnego API do komunikacji sieciowej zamiast przestarzałego `HttpURLConnection`. Pozwala na wygodne budowanie zapytań i obsługę odpowiedzi.

**Fragmenty kodu:**
```java
// Budowanie zapytania HTTP GET
HttpRequest request = HttpRequest.newBuilder().uri(new URI(apiUrl)).GET().build();

// Wysłanie zapytania i odebranie odpowiedzi jako String
HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
```

## 5. Wielowątkowość i Asynchroniczność

**Lokalizacja:** `HelloController.java` 
**Opis:** Aplikacja oddziela logikę interfejsu użytkownika (UI) od logiki biznesowej (pobieranie danych). Operacje sieciowe są wykonywane w tle, a aktualizacja widoku następuje na wątku głównym JavaFX.

**Fragmenty kodu:**

1.  **Wątek w tle (Background Thread):** Pobieranie danych nie blokuje aplikacji.
    ```java
    new Thread(() -> {
        try {
            // Długotrwała operacja pobierania danych
            WeatherService.Coordinates coords = weatherService.getCoordinatesForCity(cityName);
            // ...
    ```
    
2.  **Aktualizacja UI (Platform.runLater):** Bezpieczne przekazanie danych do kontrolek JavaFX.
    ```java
            Platform.runLater(() -> {
                // Kod wykonany na wątku JavaFX Application Thread
                updateUI(data);
                updateStarIcon(rawName);
            });
        } catch (Exception e) { e.printStackTrace(); }
    }).start();
	```
## 6. Testy Jednostkowe (Unit Testing)

**Lokalizacja:** `FavoritesManagerTest.java` 
**Opis:** Kluczowa logika biznesowa jest testowana automatycznie przy użyciu biblioteki JUnit 5. Testy zapewniają, że dodawanie i usuwanie ulubionych działa zgodnie z oczekiwaniami.

**Fragmenty kodu:** Przygotowanie stanu przed każdym testem (`@BeforeEach`):
```java
@BeforeEach
void setUp() {
    manager = FavoritesManager.getInstance();
    manager.clearAll(); // Zapewnia czyste środowisko testowe
}
```

Test weryfikujący dodawanie danych:
```java
@Test
void addCityToFavorites() {
    manager.addFavorite("TestCity");
    // Asercja sprawdzająca poprawność
    assertTrue(manager.isFavorite("TestCity"));
}
```

## 8. Wnioski

Projekt "Atmo" realizuje założenia aplikacji okienkowej w technologii JavaFX. Kod źródłowy potwierdza spełnienie wszystkich wymagań.

1.  **Programowanie Obiektowe:** Aplikacja posiada modularną strukturę. Wykorzystano hermetyzację, dziedziczenie oraz polimorfizm.
2.  **Wzorce Projektowe:** Zaimplementowano wzorzec Singleton w klasie `FavoritesManager` do zarządzania stanem aplikacji oraz metodę wytwórczą w `WeatherCondition` do mapowania danych z API.
3.  **Jakość kodu:** Zastosowano nowoczesne funkcje Javy (Records, Switch Expressions, HttpClient), co zwiększa czytelność i redukuje długość kodu.
4.  **Testowanie:** Kluczowa logika (zarządzanie ulubionymi, mapowanie pogody) została pokryta testami jednostkowymi przy użyciu frameworka JUnit 5.

Aplikacja działa stabilnie, wykorzystuje wielowątkowość do operacji sieciowych i poprawnie oddziela warstwę logiczną od interfejsu użytkownika.
