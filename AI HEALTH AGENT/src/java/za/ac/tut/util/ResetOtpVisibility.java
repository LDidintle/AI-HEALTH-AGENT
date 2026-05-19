package za.ac.tut.util;

public final class ResetOtpVisibility {

    public static final String CONFIG_KEY = "SMARTHEALTH_SHOW_RESET_OTP";

    private ResetOtpVisibility() {
    }

    public static boolean isDemoOtpVisible() {
        return isDemoOtpVisible(System.getProperty(CONFIG_KEY), System.getenv(CONFIG_KEY));
    }

    public static boolean isDemoOtpVisible(String propertyValue, String environmentValue) {
        String configured = trimToNull(propertyValue);
        if (configured == null) {
            configured = trimToNull(environmentValue);
        }
        return Boolean.parseBoolean(configured);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
