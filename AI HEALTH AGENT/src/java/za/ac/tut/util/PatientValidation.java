package za.ac.tut.util;

import java.sql.Date;
import java.time.LocalDate;

public final class PatientValidation {

    private static final int MAX_PATIENT_AGE_YEARS = 120;

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
        return trimmed != null && trimmed.matches("[0-9]{13}") && dateFromIdNumber(trimmed) != null;
    }

    public static boolean idNumberMatchesDateOfBirth(String idNumber, String dateOfBirth) {
        LocalDate idDate = dateFromIdNumber(idNumber);
        Date parsedDate = parseDateOfBirth(dateOfBirth);
        return idDate != null && parsedDate != null && idDate.equals(parsedDate.toLocalDate());
    }

    public static Date parseDateOfBirth(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }

        try {
            LocalDate dateOfBirth = LocalDate.parse(trimmed);
            LocalDate today = LocalDate.now();
            if (!dateOfBirth.isBefore(today) || dateOfBirth.isBefore(today.minusYears(MAX_PATIENT_AGE_YEARS))) {
                return null;
            }

            return Date.valueOf(dateOfBirth);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isValidDateOfBirth(String value) {
        return parseDateOfBirth(value) != null;
    }

    private static LocalDate dateFromIdNumber(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null || !trimmed.matches("[0-9]{13}")) {
            return null;
        }

        try {
            int year = Integer.parseInt(trimmed.substring(0, 2));
            int month = Integer.parseInt(trimmed.substring(2, 4));
            int day = Integer.parseInt(trimmed.substring(4, 6));
            LocalDate today = LocalDate.now();
            int currentCentury = (today.getYear() / 100) * 100;
            LocalDate candidate = LocalDate.of(currentCentury + year, month, day);
            if (!candidate.isBefore(today)) {
                candidate = LocalDate.of(currentCentury - 100 + year, month, day);
            }
            if (!candidate.isBefore(today) || candidate.isBefore(today.minusYears(MAX_PATIENT_AGE_YEARS))) {
                return null;
            }
            return candidate;
        } catch (Exception e) {
            return null;
        }
    }
}
