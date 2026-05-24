package za.ac.tut.util;

import java.math.BigDecimal;

public final class WatchTemperaturePolicy {

    public static final String SAMSUNG_HEALTH_SOURCE = "SAMSUNG_HEALTH_DATA";

    private WatchTemperaturePolicy() {
    }

    public static boolean isSleepTemperatureTrend(String source) {
        return source != null && SAMSUNG_HEALTH_SOURCE.equalsIgnoreCase(source.trim());
    }

    public static String statusFor(String source, BigDecimal temperature) {
        if (isSleepTemperatureTrend(source)) {
            return "TREND";
        }
        if (temperature.compareTo(new BigDecimal("36.00")) < 0) {
            return "LOW";
        }
        if (temperature.compareTo(new BigDecimal("37.50")) > 0) {
            return "HIGH";
        }
        return "NORMAL";
    }

    public static BigDecimal temperatureForAlertEvaluation(String source, BigDecimal temperature) {
        return isSleepTemperatureTrend(source) ? null : temperature;
    }
}
