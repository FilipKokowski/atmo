package org.atmo.atmo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WeatherConditionTest {

    @Test
    void fromCodeTest() {
        WeatherCondition result = WeatherCondition.fromCode(51);

        assertEquals(WeatherCondition.RAIN_MODERATE, result);
        assertEquals("rain.png", result.getIconFileName());
    }

    @Test
    void fromCodeRangeTest() {
        assertEquals(WeatherCondition.RAIN_MODERATE, WeatherCondition.fromCode(53));
        assertEquals(WeatherCondition.RAIN_MODERATE, WeatherCondition.fromCode(61));
    }

    @Test
    void fromCodeUnknownTest() {
        WeatherCondition result = WeatherCondition.fromCode(999);
        assertEquals(WeatherCondition.UNKNOWN, result);
    }
}