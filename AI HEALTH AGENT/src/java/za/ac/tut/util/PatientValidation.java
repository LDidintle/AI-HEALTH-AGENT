package za.ac.tut.util;

public final class PatientValidation {

    private PatientValidation() {
    }

    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String normalizePhone(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }

        String digits = trimmed.replaceAll("[^0-9]", "");
        if (digits.startsWith("27") && digits.length() == 11) {
            return "0" + digits.substring(2);
        }
        return digits;
    }

    public static boolean isValidSouthAfricanPhone(String value) {
        String normalized = normalizePhone(value);
        return normalized != null && normalized.matches("0[6-8][0-9]{8}");
    }

    public static boolean samePhone(String first, String second) {
        String normalizedFirst = normalizePhone(first);
        String normalizedSecond = normalizePhone(second);
        return normalizedFirst != null && normalizedFirst.equals(normalizedSecond);
    }

    public static boolean isValidIdNumber(String value) {
        String trimmed = trimToNull(value);
        return trimmed != null && trimmed.matches("[0-9]{13}");
    }
}
