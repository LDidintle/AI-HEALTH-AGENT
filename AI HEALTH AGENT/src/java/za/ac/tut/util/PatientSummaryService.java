package za.ac.tut.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import za.ac.tut.model.PatientSummary;

public final class PatientSummaryService {

    private static final BigDecimal RAISED_TEMPERATURE = new BigDecimal("37.5");

    private PatientSummaryService() {
    }

    public static PatientSummary loadSummary(Connection conn, int userId) throws Exception {
        PatientSummary summary = new PatientSummary();
        summary.setAveragePulse(queryAverage(conn, "SELECT AVG(bpm) FROM pulse_readings WHERE user_id = ?", userId));
        summary.setAverageTemperature(queryAverage(conn, "SELECT AVG(temperature) FROM temperature_readings WHERE user_id = ?", userId));
        summary.setAverageSystolic(queryAverage(conn, "SELECT AVG(systolic) FROM blood_pressure_readings WHERE user_id = ?", userId));
        summary.setAverageDiastolic(queryAverage(conn, "SELECT AVG(diastolic) FROM blood_pressure_readings WHERE user_id = ?", userId));
        summary.setReadingCount(queryCount(conn, userId));
        BigDecimal averageScorableTemperature = queryAverage(conn,
                "SELECT AVG(temperature) FROM temperature_readings WHERE user_id = ? "
                + "AND UPPER(COALESCE(source, '')) <> 'SAMSUNG_HEALTH_DATA'", userId);
        HealthRiskPredictionService.PredictionResult prediction =
                HealthRiskPredictionService.predictForUser(conn, userId);
        summary.setPrediction(formatPrediction(summary, prediction, averageScorableTemperature));
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
        String sql = "SELECT COUNT(*) FROM ("
                + "SELECT pulse_id FROM pulse_readings WHERE user_id = ? "
                + "UNION ALL SELECT temp_id FROM temperature_readings WHERE user_id = ? "
                + "UNION ALL SELECT bp_id FROM blood_pressure_readings WHERE user_id = ?"
                + ") patient_readings";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private static String formatPrediction(PatientSummary summary,
            HealthRiskPredictionService.PredictionResult prediction,
            BigDecimal averageScorableTemperature) {
        if (prediction.getDataQuality() == HealthRiskPredictionService.DataQuality.MISSING_VITALS
                && hasLegacyVitals(summary)) {
            return fallbackPrediction(summary, averageScorableTemperature);
        }

        List<String> reasons = prediction.getReasons();
        String lead = reasons.isEmpty() ? prediction.getSummary() : reasons.get(0);
        return prediction.getRiskLevel().name() + " - " + lead + " " + prediction.getRecommendedAction();
    }

    private static boolean hasLegacyVitals(PatientSummary summary) {
        return summary.getAveragePulse() != null
                || summary.getAverageTemperature() != null
                || summary.getAverageSystolic() != null
                || summary.getAverageDiastolic() != null;
    }

    private static String fallbackPrediction(PatientSummary summary, BigDecimal averageScorableTemperature) {
        if (summary.getReadingCount() == 0) {
            return "Awaiting readings - No synced vitals are available yet. "
                    + "Sync Samsung Health or Health Connect readings before using this screening score.";
        }

        if (greaterOrEqual(summary.getAverageSystolic(), 140) || greaterOrEqual(summary.getAverageDiastolic(), 90)) {
            return "HIGH - Average blood pressure is high. Review repeated readings and consider follow-up.";
        }

        if (lessThan(summary.getAveragePulse(), 50)) {
            return "MEDIUM - Average pulse is low. Check symptoms and review with a clinician if persistent.";
        }

        if (greaterThan(summary.getAveragePulse(), 100)) {
            return "MEDIUM - Average pulse is high. Recheck resting readings and review possible causes.";
        }

        if (greaterThan(averageScorableTemperature, RAISED_TEMPERATURE)) {
            return "MEDIUM - Average temperature is raised. Monitor fever symptoms and follow up if persistent.";
        }

        return "LOW - Average vitals are within the expected screening range. "
                + "Keep monitoring trends. This score does not diagnose or rule out illness.";
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
