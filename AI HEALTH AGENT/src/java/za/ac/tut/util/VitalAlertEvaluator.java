package za.ac.tut.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Statement;

public final class VitalAlertEvaluator {

    private static final int COOLDOWN_MINUTES = 15;

    private VitalAlertEvaluator() {
    }

    public static void evaluateAndStore(
            Connection conn,
            int userId,
            Integer heartRate,
            BigDecimal temperature,
            Integer systolic,
            Integer diastolic) throws Exception {

        AlertDecision decision = assess(heartRate, temperature, systolic, diastolic);
        if (decision == null || hasRecentOpenAlert(conn, userId, decision.status)) {
            return;
        }

        String sql = "INSERT INTO emergency_alerts (user_id, bpm, alert_status, countdown_seconds) "
                + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            if (heartRate == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, heartRate);
            }
            ps.setString(3, decision.status);
            ps.setInt(4, decision.countdownSeconds);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    assignAlertToHospital(conn, rs.getInt(1), userId);
                }
            }
        }
    }

    private static void assignAlertToHospital(Connection conn, int alertId, int userId) throws Exception {
        Integer hospitalId = findHospitalForUser(conn, userId);
        if (hospitalId == null) {
            return;
        }

        String sql = "INSERT INTO hospital_alert_assignments (alert_id, hospital_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, alertId);
            ps.setInt(2, hospitalId);
            ps.executeUpdate();
        }
    }

    private static Integer findHospitalForUser(Connection conn, int userId) throws Exception {
        String patientAddress = null;
        try (PreparedStatement ps = conn.prepareStatement("SELECT address FROM users WHERE id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    patientAddress = rs.getString("address");
                }
            }
        }

        if (patientAddress == null || patientAddress.trim().isEmpty()) {
            return null;
        }

        String normalizedAddress = patientAddress.toLowerCase();
        String sql = "SELECT hospital_id, service_area FROM hospitals WHERE active = TRUE ORDER BY name";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String serviceArea = rs.getString("service_area");
                if (serviceArea != null && !serviceArea.trim().isEmpty()
                        && normalizedAddress.contains(serviceArea.trim().toLowerCase())) {
                    return rs.getInt("hospital_id");
                }
            }
        }

        return null;
    }

    public static AlertDecision assess(
            Integer heartRate,
            BigDecimal temperature,
            Integer systolic,
            Integer diastolic) {

        if (systolic != null && diastolic != null && (systolic >= 180 || diastolic >= 120)) {
            return new AlertDecision("CRITICAL", 30);
        }
        if (heartRate != null && (heartRate >= 130 || heartRate <= 40)) {
            return new AlertDecision("CRITICAL", 30);
        }
        if (temperature != null && temperature.compareTo(new BigDecimal("39.00")) >= 0) {
            return new AlertDecision("CRITICAL", 30);
        }
        if (heartRate != null && temperature != null
                && heartRate >= 110
                && temperature.compareTo(new BigDecimal("38.00")) >= 0) {
            return new AlertDecision("WARNING", 60);
        }
        if (systolic != null && diastolic != null && (systolic >= 140 || diastolic >= 90)) {
            return new AlertDecision("WARNING", 60);
        }
        if (heartRate != null && (heartRate >= 110 || heartRate <= 50)) {
            return new AlertDecision("WARNING", 60);
        }
        if (temperature != null && temperature.compareTo(new BigDecimal("38.00")) >= 0) {
            return new AlertDecision("WARNING", 60);
        }

        return null;
    }

    private static boolean hasRecentOpenAlert(Connection conn, int userId, String status) throws Exception {
        String sql = "SELECT alert_id FROM emergency_alerts "
                + "WHERE user_id = ? AND alert_status = ? "
                + "AND created_at >= ? "
                + limitOne(conn);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, status);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis() - COOLDOWN_MINUTES * 60L * 1000L));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static String limitOne(Connection conn) throws Exception {
        String productName = conn.getMetaData().getDatabaseProductName();
        if (productName != null && productName.toLowerCase().contains("derby")) {
            return "FETCH FIRST 1 ROW ONLY";
        }
        return "LIMIT 1";
    }

    public static class AlertDecision {
        private final String status;
        private final int countdownSeconds;

        private AlertDecision(String status, int countdownSeconds) {
            this.status = status;
            this.countdownSeconds = countdownSeconds;
        }

        public String getStatus() {
            return status;
        }

        public int getCountdownSeconds() {
            return countdownSeconds;
        }
    }
}
