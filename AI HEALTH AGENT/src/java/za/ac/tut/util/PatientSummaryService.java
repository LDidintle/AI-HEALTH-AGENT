package za.ac.tut.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import za.ac.tut.model.PatientSummary;

public final class PatientSummaryService {

    private PatientSummaryService() {
    }

    public static PatientSummary loadSummary(Connection conn, int userId) throws Exception {
        PatientSummary summary = new PatientSummary();
        summary.setAveragePulse(queryAverage(conn, "SELECT AVG(bpm) FROM pulse_readings WHERE user_id = ?", userId));
        summary.setAverageTemperature(queryAverage(conn, "SELECT AVG(temperature) FROM temperature_readings WHERE user_id = ?", userId));
        summary.setAverageSystolic(queryAverage(conn, "SELECT AVG(systolic) FROM blood_pressure_readings WHERE user_id = ?", userId));
        summary.setAverageDiastolic(queryAverage(conn, "SELECT AVG(diastolic) FROM blood_pressure_readings WHERE user_id = ?", userId));
        summary.setReadingCount(queryCount(conn, userId));
        summary.setPrediction(predict(summary));
        return summary;
    }

    private static BigDecimal queryAverage(Connection conn, String sql, int userId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : null;
            }
        }
    }

    private static int queryCount(Connection conn, int userId) throws Exception {
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM pulse_readings WHERE user_id = ?) + "
                + "(SELECT COUNT(*) FROM temperature_readings WHERE user_id = ?) + "
                + "(SELECT COUNT(*) FROM blood_pressure_readings WHERE user_id = ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private static String predict(PatientSummary summary) {
        if (summary.getReadingCount() == 0) {
            return "No synced vitals yet. Ask the patient to sync from the mobile app before clinical review.";
        }

        if (greaterOrEqual(summary.getAverageSystolic(), 140) || greaterOrEqual(summary.getAverageDiastolic(), 90)) {
            return "Average blood pressure is high. Review repeated readings and consider follow-up.";
        }

        if (lessThan(summary.getAveragePulse(), 50)) {
            return "Average pulse is low. Check symptoms and review with a clinician if persistent.";
        }

        if (greaterThan(summary.getAveragePulse(), 100)) {
            return "Average pulse is high. Recheck resting readings and review possible causes.";
        }

        if (greaterThan(summary.getAverageTemperature(), new BigDecimal("37.5"))) {
            return "Average temperature is raised. Monitor fever symptoms and follow up if persistent.";
        }

        return "Average vitals are within the expected screening range.";
    }

    private static boolean greaterThan(BigDecimal value, int threshold) {
        return value != null && value.compareTo(BigDecimal.valueOf(threshold)) > 0;
    }

    private static boolean greaterThan(BigDecimal value, BigDecimal threshold) {
        return value != null && value.compareTo(threshold) > 0;
    }

    private static boolean greaterOrEqual(BigDecimal value, int threshold) {
        return value != null && value.compareTo(BigDecimal.valueOf(threshold)) >= 0;
    }

    private static boolean lessThan(BigDecimal value, int threshold) {
        return value != null && value.compareTo(BigDecimal.valueOf(threshold)) < 0;
    }
}
