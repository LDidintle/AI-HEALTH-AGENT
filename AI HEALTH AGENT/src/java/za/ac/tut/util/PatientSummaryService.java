package za.ac.tut.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
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
        summary.setPrediction(formatPrediction(HealthRiskPredictionService.predictForUser(conn, userId)));
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

    private static String formatPrediction(HealthRiskPredictionService.PredictionResult prediction) {
        List<String> reasons = prediction.getReasons();
        String lead = reasons.isEmpty() ? prediction.getSummary() : reasons.get(0);
        return prediction.getRiskLevel().name() + " - " + lead + " " + prediction.getRecommendedAction();
    }
}
