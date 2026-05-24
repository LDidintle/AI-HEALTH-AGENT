import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import za.ac.tut.model.PasswordUtils;
import za.ac.tut.model.ReportCriteria;
import za.ac.tut.model.ReportResult;
import za.ac.tut.model.PatientSummary;
import za.ac.tut.util.PasswordResetService;
import za.ac.tut.util.PatientContextSettingsService;
import za.ac.tut.util.PatientAccountProcedureService;
import za.ac.tut.util.ReportService;
import za.ac.tut.util.PatientSummaryService;
import za.ac.tut.util.VitalAlertEvaluator;
import za.ac.tut.util.HealthRiskPredictionService;

public class BackendIntegrationChecks {

    public static void main(String[] args) throws Exception {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        try (Connection conn = DriverManager.getConnection("jdbc:derby:memory:smarthealthIntegration;create=true")) {
            createSchema(conn);
            verifiesPatientCrud(conn);
            verifiesPasswordReset(conn);
            verifiesPatientContextSettings(conn);
            verifiesSamsungSleepTemperaturePrediction(conn);
            verifiesSamsungSleepTemperatureStaffSummary(conn);
            verifiesEmergencyAlertPersistence(conn);
            verifiesReports(conn);
        }
        System.out.println("Backend integration checks passed.");
    }

    private static void createSchema(Connection conn) throws Exception {
        try (Statement statement = conn.createStatement()) {
            statement.execute("CREATE TABLE users ("
                    + "id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
                    + "title VARCHAR(20), first_name VARCHAR(80) NOT NULL, surname VARCHAR(80) NOT NULL, "
                    + "dob DATE, email VARCHAR(160) NOT NULL UNIQUE, address CLOB, "
                    + "emergency_contact_name VARCHAR(120), emergency_contact_number VARCHAR(40), "
                    + "is_verified BOOLEAN DEFAULT FALSE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            statement.execute("CREATE TABLE user_auth ("
                    + "auth_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
                    + "user_id INTEGER NOT NULL, password_hash VARCHAR(255) NOT NULL, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            statement.execute("CREATE TABLE password_reset_otps ("
                    + "reset_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
                    + "user_id INTEGER NOT NULL, otp_hash VARCHAR(255) NOT NULL, expires_at TIMESTAMP NOT NULL, "
                    + "used_at TIMESTAMP, attempt_count INTEGER DEFAULT 0, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            statement.execute("CREATE TABLE patient_context_settings ("
                    + "user_id INTEGER NOT NULL PRIMARY KEY, "
                    + "sleep_start VARCHAR(5) NOT NULL, sleep_end VARCHAR(5) NOT NULL, "
                    + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            statement.execute("CREATE TABLE devices ("
                    + "device_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, user_id INTEGER NOT NULL, "
                    + "device_type VARCHAR(40), manufacturer VARCHAR(80), device_model VARCHAR(80), "
                    + "platform VARCHAR(80), active BOOLEAN DEFAULT TRUE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            statement.execute("CREATE TABLE pulse_readings ("
                    + "pulse_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, user_id INTEGER NOT NULL, "
                    + "bpm INTEGER, status VARCHAR(20), source VARCHAR(40), recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            statement.execute("CREATE TABLE temperature_readings ("
                    + "temp_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, user_id INTEGER NOT NULL, "
                    + "temperature DECIMAL(5,2), status VARCHAR(20), source VARCHAR(40), recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            statement.execute("CREATE TABLE blood_pressure_readings ("
                    + "bp_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, user_id INTEGER NOT NULL, "
                    + "systolic INTEGER, diastolic INTEGER, status VARCHAR(20), source VARCHAR(40), recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            statement.execute("CREATE TABLE device_sync_events ("
                    + "sync_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, user_id INTEGER NOT NULL, "
                    + "source_platform VARCHAR(80), sync_status VARCHAR(30), synced_for TIMESTAMP DEFAULT CURRENT_TIMESTAMP, device_id INTEGER)");
            statement.execute("CREATE TABLE health_sync_sections ("
                    + "section_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, user_id INTEGER NOT NULL, "
                    + "device_id INTEGER, source VARCHAR(80), window_start TIMESTAMP DEFAULT CURRENT_TIMESTAMP, window_end TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "heart_rate_latest INTEGER, heart_rate_min INTEGER, heart_rate_max INTEGER, heart_rate_average DECIMAL(6,2), heart_rate_count INTEGER DEFAULT 0, "
                    + "temperature_latest DECIMAL(5,2), temperature_min DECIMAL(5,2), temperature_max DECIMAL(5,2), temperature_average DECIMAL(5,2), temperature_count INTEGER DEFAULT 0, "
                    + "systolic_latest INTEGER, diastolic_latest INTEGER, blood_pressure_count INTEGER DEFAULT 0, "
                    + "device_type VARCHAR(40), device_manufacturer VARCHAR(80), device_model VARCHAR(80), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            statement.execute("CREATE TABLE emergency_alerts ("
                    + "alert_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, user_id INTEGER NOT NULL, bpm INTEGER, "
                    + "alert_status VARCHAR(30), countdown_seconds INTEGER, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            statement.execute("CREATE TABLE hospitals ("
                    + "hospital_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, name VARCHAR(100), email VARCHAR(160), "
                    + "phone VARCHAR(40), service_area VARCHAR(100), address CLOB, active BOOLEAN DEFAULT TRUE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            statement.execute("CREATE TABLE hospital_alert_assignments ("
                    + "assignment_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, alert_id INTEGER NOT NULL, hospital_id INTEGER NOT NULL, "
                    + "assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, status VARCHAR(30) DEFAULT 'ASSIGNED')");
            statement.execute("CREATE TABLE ambulance_notifications ("
                    + "notification_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, alert_id INTEGER NOT NULL, "
                    + "sent_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, response_status VARCHAR(40))");
        }
    }

    private static void verifiesPatientCrud(Connection conn) throws Exception {
        int userId = PatientAccountProcedureService.createPatientAccount(
                conn, "Mr", "Integration", "Patient", Date.valueOf("2000-01-01"),
                "integration@example.com", PasswordUtils.hashPassword("Strong22!"));
        assertTrue(userId > 0, "patient create should return generated id");
        assertEquals(1, count(conn, "SELECT COUNT(*) FROM users WHERE id = " + userId), "patient row should exist");
        assertEquals(1, count(conn, "SELECT COUNT(*) FROM user_auth WHERE user_id = " + userId), "auth row should exist");
        PatientAccountProcedureService.deletePatientAccount(conn, userId);
        assertEquals(0, count(conn, "SELECT COUNT(*) FROM users WHERE id = " + userId), "patient delete should remove user");
    }

    private static void verifiesPasswordReset(Connection conn) throws Exception {
        int userId = PatientAccountProcedureService.createPatientAccount(
                conn, "Ms", "Reset", "Patient", Date.valueOf("2001-02-03"),
                "reset@example.com", PasswordUtils.hashPassword("OldPass22!"));
        PasswordResetService.ResetRequestResult request = PasswordResetService.createOtp(conn, "reset@example.com");
        assertTrue(request.isUserFound(), "reset request should find user");
        assertTrue(request.getOtp().matches("[0-9]{6}"), "OTP should be six digits");
        PasswordResetService.ResetPasswordResult wrong = PasswordResetService.resetPassword(
                conn, "reset@example.com", "000000", PasswordUtils.hashPassword("NewPass22!"));
        assertFalse(wrong.isSuccess(), "wrong OTP should fail");
        PasswordResetService.ResetPasswordResult right = PasswordResetService.resetPassword(
                conn, "reset@example.com", request.getOtp(), PasswordUtils.hashPassword("NewPass22!"));
        assertTrue(right.isSuccess(), "correct OTP should reset password");
        assertEquals(1, count(conn, "SELECT COUNT(*) FROM password_reset_otps WHERE user_id = " + userId + " AND used_at IS NOT NULL"), "OTP should be marked used");
    }

    private static void verifiesPatientContextSettings(Connection conn) throws Exception {
        int userId = PatientAccountProcedureService.createPatientAccount(
                conn, "Ms", "Context", "Patient", Date.valueOf("2002-04-05"),
                "context@example.com", PasswordUtils.hashPassword("Context22!"));
        PatientContextSettingsService.ContextSettings defaults = PatientContextSettingsService.load(conn, userId);
        assertEquals("22:00", defaults.getSleepStart(), "default sleep start should be stable");
        assertEquals("06:00", defaults.getSleepEnd(), "default wake time should be stable");

        PatientContextSettingsService.save(conn, userId, "00:00", "08:00");
        PatientContextSettingsService.ContextSettings saved = PatientContextSettingsService.load(conn, userId);
        assertEquals("00:00", saved.getSleepStart(), "saved sleep start should load");
        assertEquals("08:00", saved.getSleepEnd(), "saved wake time should load");

        PatientContextSettingsService.save(conn, userId, "23:30", "07:15");
        assertEquals(1, count(conn, "SELECT COUNT(*) FROM patient_context_settings WHERE user_id = " + userId), "context settings should upsert one row");

        boolean rejected = false;
        try {
            PatientContextSettingsService.save(conn, userId, "24:00", "08:00");
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        assertTrue(rejected, "invalid sleep time should be rejected");
    }

    private static void verifiesSamsungSleepTemperaturePrediction(Connection conn) throws Exception {
        int userId = PatientAccountProcedureService.createPatientAccount(
                conn, "Ms", "Temperature", "Patient", Date.valueOf("2001-05-06"),
                "temperature@example.com", PasswordUtils.hashPassword("Temperature22!"));
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate("INSERT INTO health_sync_sections "
                    + "(user_id, source, heart_rate_latest, temperature_latest, systolic_latest, diastolic_latest) VALUES ("
                    + userId + ", 'SAMSUNG_HEALTH_DATA', 82, 34.10, 99, 58)");
        }
        HealthRiskPredictionService.PredictionResult prediction =
                HealthRiskPredictionService.predictForUser(conn, userId);
        assertFalse(prediction.getReasons().contains("Temperature is dangerously low."),
                "stored Samsung sleep temperature must not be classified as dangerous core temperature");
        assertEquals(HealthRiskPredictionService.DataQuality.LIMITED_DATA, prediction.getDataQuality(),
                "stored Samsung sleep temperature must keep screening data quality limited");
    }

    private static void verifiesEmergencyAlertPersistence(Connection conn) throws Exception {
        int userId = PatientAccountProcedureService.createPatientAccount(
                conn, "Dr", "Alert", "Patient", Date.valueOf("1999-03-04"),
                "alert@example.com", PasswordUtils.hashPassword("Alert22!"));
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate("UPDATE users SET address = '10 Clinic Road, Pretoria Central' WHERE id = " + userId);
            statement.executeUpdate("INSERT INTO hospitals (name, email, service_area, active) VALUES "
                    + "('Pretoria Hospital', 'hospital@example.com', 'Pretoria', TRUE)");
        }
        VitalAlertEvaluator.evaluateAndStore(conn, userId, 132, null, null, null);
        assertEquals(1, count(conn, "SELECT COUNT(*) FROM emergency_alerts WHERE user_id = " + userId + " AND alert_status = 'CRITICAL'"), "critical alert should persist");
        assertEquals(1, count(conn, "SELECT COUNT(*) FROM hospital_alert_assignments"), "alert should be assigned to matching hospital");
    }

    private static void verifiesSamsungSleepTemperatureStaffSummary(Connection conn) throws Exception {
        int userId = PatientAccountProcedureService.createPatientAccount(
                conn, "Mr", "Staff", "Trend", Date.valueOf("1998-07-08"),
                "staff-temperature@example.com", PasswordUtils.hashPassword("StaffTrend22!"));
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate("INSERT INTO pulse_readings (user_id, bpm, source) VALUES ("
                    + userId + ", 76, 'SAMSUNG_HEALTH_DATA')");
            statement.executeUpdate("INSERT INTO blood_pressure_readings (user_id, systolic, diastolic, source) VALUES ("
                    + userId + ", 118, 76, 'SAMSUNG_HEALTH_DATA')");
            statement.executeUpdate("INSERT INTO temperature_readings (user_id, temperature, source) VALUES ("
                    + userId + ", 39.20, 'SAMSUNG_HEALTH_DATA')");
        }

        PatientSummary summary = PatientSummaryService.loadSummary(conn, userId);
        assertFalse(summary.getPrediction().contains("temperature is raised"),
                "staff patient summary must not interpret Samsung sleep-temperature trend as fever");

        ReportCriteria criteria = new ReportCriteria();
        criteria.setReportType(ReportService.REPORT_VITALS);
        criteria.setStartDate(LocalDate.now().minusDays(1));
        criteria.setEndDate(LocalDate.now().plusDays(1));
        criteria.setSearch("staff-temperature@example.com");
        ReportResult vitals = ReportService.loadReport(conn, criteria);
        assertEquals(1, vitals.getRows().size(), "temperature trend test patient should appear in vitals report");
        assertTrue(vitals.getRows().get(0).get("risk").startsWith("LOW"),
                "vitals report screening note must not score Samsung sleep-temperature trend as fever");
    }

    private static void verifiesReports(Connection conn) throws Exception {
        ReportCriteria criteria = new ReportCriteria();
        criteria.setStartDate(LocalDate.now().minusDays(1));
        criteria.setEndDate(LocalDate.now().plusDays(1));
        criteria.setReportType(ReportService.REPORT_ALERTS);
        ReportResult alerts = ReportService.loadReport(conn, criteria);
        assertEquals(ReportService.REPORT_ALERTS, alerts.getReportType(), "alerts report should load");
        assertTrue(!alerts.getColumns().isEmpty(), "alerts report should include columns");
    }

    private static int count(Connection conn, String sql) throws Exception {
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " expected=" + expected + " actual=" + actual);
        }
    }
}
