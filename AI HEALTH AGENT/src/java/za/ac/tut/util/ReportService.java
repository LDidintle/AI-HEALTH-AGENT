package za.ac.tut.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import za.ac.tut.model.ReportCriteria;
import za.ac.tut.model.ReportResult;

public final class ReportService {

    public static final String REPORT_MANAGEMENT = "management";
    public static final String REPORT_VITALS = "vitals";
    public static final String REPORT_ALERTS = "alerts";
    public static final String REPORT_SYNC = "sync";

    private ReportService() {
    }

    public static ReportResult loadReport(Connection conn, ReportCriteria criteria) throws SQLException {
        String type = normalizeReportType(criteria.getReportType(), criteria.isHospital());
        criteria.setReportType(type);

        switch (type) {
            case REPORT_VITALS:
                return loadVitalsReport(conn, criteria);
            case REPORT_ALERTS:
                return loadAlertReport(conn, criteria);
            case REPORT_SYNC:
                return loadSyncReport(conn, criteria);
            case REPORT_MANAGEMENT:
            default:
                return loadManagementReport(conn, criteria);
        }
    }

    public static String normalizeReportType(String reportType, boolean hospitalUser) {
        if (hospitalUser) {
            return REPORT_ALERTS;
        }

        String type = clean(reportType);
        if (REPORT_VITALS.equals(type) || REPORT_ALERTS.equals(type) || REPORT_SYNC.equals(type)) {
            return type;
        }
        return REPORT_MANAGEMENT;
    }

    private static ReportResult loadManagementReport(Connection conn, ReportCriteria criteria) throws SQLException {
        Timestamp start = startTimestamp(criteria);
        Timestamp end = endExclusiveTimestamp(criteria);
        ReportResult result = baseResult(
                REPORT_MANAGEMENT,
                "Management Summary Report",
                "High-level operating totals for the selected reporting period.");
        result.addColumn("metric", "Metric");
        result.addColumn("value", "Value");
        result.addColumn("meaning", "Business Meaning");

        int totalPatients = queryInt(conn, "SELECT COUNT(*) FROM users");
        int newPatients = queryInt(conn, "SELECT COUNT(*) FROM users WHERE created_at >= ? AND created_at < ?", start, end);
        int verifiedPatients = queryInt(conn, "SELECT COUNT(*) FROM users WHERE is_verified = TRUE");
        int monitoredPatients = queryInt(conn,
                "SELECT COUNT(DISTINCT user_id) FROM ("
                + "SELECT user_id FROM pulse_readings WHERE recorded_at >= ? AND recorded_at < ? "
                + "UNION SELECT user_id FROM temperature_readings WHERE recorded_at >= ? AND recorded_at < ? "
                + "UNION SELECT user_id FROM blood_pressure_readings WHERE recorded_at >= ? AND recorded_at < ?"
                + ") monitored",
                start, end, start, end, start, end);
        int totalReadings = queryInt(conn,
                "SELECT "
                + "(SELECT COUNT(*) FROM pulse_readings WHERE recorded_at >= ? AND recorded_at < ?) + "
                + "(SELECT COUNT(*) FROM temperature_readings WHERE recorded_at >= ? AND recorded_at < ?) + "
                + "(SELECT COUNT(*) FROM blood_pressure_readings WHERE recorded_at >= ? AND recorded_at < ?)",
                start, end, start, end, start, end);
        int alertCount = queryInt(conn,
                "SELECT COUNT(*) FROM emergency_alerts WHERE created_at >= ? AND created_at < ?",
                start, end);
        int activeDevices = queryInt(conn,
                "SELECT COUNT(DISTINCT d.device_id) FROM devices d JOIN users u ON u.id = d.user_id WHERE d.active = TRUE");

        addMetric(result, "Registered patients", totalPatients, "Total patient accounts in the system.");
        addMetric(result, "New patients this period", newPatients, "Patients created between the selected dates.");
        addMetric(result, "Verified patients", verifiedPatients, "Patients marked as staff verified.");
        addMetric(result, "Patients with synced vitals", monitoredPatients, "Unique patients with heart, temperature, or blood pressure readings in range.");
        addMetric(result, "Total vital readings", totalReadings, "All vital measurements captured in range.");
        addMetric(result, "Emergency alerts", alertCount, "Alerts raised in the selected period.");
        addMetric(result, "Active registered devices", activeDevices, "Devices linked to patient accounts.");
        return result;
    }

    private static ReportResult loadVitalsReport(Connection conn, ReportCriteria criteria) throws SQLException {
        Timestamp start = startTimestamp(criteria);
        Timestamp end = endExclusiveTimestamp(criteria);
        int patientId = parseId(criteria.getPatientId());
        String search = clean(criteria.getSearch());
        String pattern = likePattern(search);

        ReportResult result = baseResult(
                REPORT_VITALS,
                "Patient Vitals Report",
                "Average heart rate, temperature, and blood pressure by patient for the selected period.");
        result.addColumn("patient", "Patient");
        result.addColumn("email", "Email");
        result.addColumn("pulse", "Avg Heart Rate");
        result.addColumn("temperature", "Avg Temperature");
        result.addColumn("bloodPressure", "Avg Blood Pressure");
        result.addColumn("readings", "Readings Used");
        result.addColumn("latest", "Latest Reading");
        result.addColumn("risk", "Screening Note");

        String sql = "SELECT u.id, u.first_name, u.surname, u.email, "
                + "COALESCE(pr.pulse_count, 0) AS pulse_count, pr.avg_pulse, pr.latest_pulse, "
                + "COALESCE(tr.temp_count, 0) AS temp_count, tr.avg_temperature, tr.avg_scorable_temperature, tr.latest_temperature, "
                + "COALESCE(bp.bp_count, 0) AS bp_count, bp.avg_systolic, bp.avg_diastolic, bp.latest_bp "
                + "FROM users u "
                + "LEFT JOIN (SELECT user_id, COUNT(*) AS pulse_count, AVG(bpm) AS avg_pulse, MAX(recorded_at) AS latest_pulse "
                + "FROM pulse_readings WHERE recorded_at >= ? AND recorded_at < ? GROUP BY user_id) pr ON pr.user_id = u.id "
                + "LEFT JOIN (SELECT user_id, COUNT(*) AS temp_count, AVG(temperature) AS avg_temperature, "
                + "AVG(CASE WHEN UPPER(COALESCE(source, '')) <> 'SAMSUNG_HEALTH_DATA' THEN temperature ELSE NULL END) AS avg_scorable_temperature, "
                + "MAX(recorded_at) AS latest_temperature "
                + "FROM temperature_readings WHERE recorded_at >= ? AND recorded_at < ? GROUP BY user_id) tr ON tr.user_id = u.id "
                + "LEFT JOIN (SELECT user_id, COUNT(*) AS bp_count, AVG(systolic) AS avg_systolic, AVG(diastolic) AS avg_diastolic, MAX(recorded_at) AS latest_bp "
                + "FROM blood_pressure_readings WHERE recorded_at >= ? AND recorded_at < ? GROUP BY user_id) bp ON bp.user_id = u.id "
                + "WHERE (? <= 0 OR u.id = ?) "
                + "AND (? = '' OR LOWER(u.first_name) LIKE ? OR LOWER(u.surname) LIKE ? OR LOWER(u.email) LIKE ?) "
                + "ORDER BY u.surname, u.first_name";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            index = setRange(ps, index, start, end);
            index = setRange(ps, index, start, end);
            index = setRange(ps, index, start, end);
            ps.setInt(index++, patientId);
            ps.setInt(index++, patientId);
            ps.setString(index++, search);
            ps.setString(index++, pattern);
            ps.setString(index++, pattern);
            ps.setString(index, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int readingCount = rs.getInt("pulse_count") + rs.getInt("temp_count") + rs.getInt("bp_count");
                    BigDecimal avgPulse = getBigDecimal(rs, "avg_pulse");
                    BigDecimal avgTemperature = getBigDecimal(rs, "avg_temperature");
                    BigDecimal avgScorableTemperature = getBigDecimal(rs, "avg_scorable_temperature");
                    BigDecimal avgSystolic = getBigDecimal(rs, "avg_systolic");
                    BigDecimal avgDiastolic = getBigDecimal(rs, "avg_diastolic");

                    Map<String, String> row = row();
                    row.put("patient", rs.getInt("id") + " - " + value(rs.getString("first_name")) + " " + value(rs.getString("surname")));
                    row.put("email", value(rs.getString("email")));
                    row.put("pulse", formatDecimal(avgPulse, " BPM"));
                    row.put("temperature", formatDecimal(avgTemperature, " C"));
                    row.put("bloodPressure", formatBloodPressure(avgSystolic, avgDiastolic));
                    row.put("readings", String.valueOf(readingCount));
                    row.put("latest", latestTimestamp(
                            rs.getTimestamp("latest_pulse"),
                            rs.getTimestamp("latest_temperature"),
                            rs.getTimestamp("latest_bp")));
                    HealthRiskPredictionService.PredictionResult prediction = HealthRiskPredictionService.predict(
                            new HealthRiskPredictionService.VitalSnapshot(
                                    avgPulse == null ? null : avgPulse.setScale(0, java.math.RoundingMode.HALF_UP).intValue(),
                                    avgScorableTemperature,
                                    avgSystolic == null ? null : avgSystolic.setScale(0, java.math.RoundingMode.HALF_UP).intValue(),
                                    avgDiastolic == null ? null : avgDiastolic.setScale(0, java.math.RoundingMode.HALF_UP).intValue()
                            ),
                            0
                    );
                    row.put("risk", prediction.getRiskLevel().name() + " - " + prediction.getSummary());
                    result.addRow(row);
                }
            }
        }
        return result;
    }

    private static ReportResult loadAlertReport(Connection conn, ReportCriteria criteria) throws SQLException {
        Timestamp start = startTimestamp(criteria);
        Timestamp end = endExclusiveTimestamp(criteria);
        String status = clean(criteria.getStatus()).toUpperCase();
        String search = clean(criteria.getSearch());
        String pattern = likePattern(search);
        int hospitalId = criteria.isHospital() && !criteria.isLegacyHospital() && criteria.getHospitalId() != null
                ? criteria.getHospitalId()
                : -1;

        ReportResult result = baseResult(
                REPORT_ALERTS,
                criteria.isHospital() ? "Hospital Emergency Alert Report" : "Emergency Alert Report",
                criteria.isHospital()
                        ? "Emergency alerts assigned to the signed-in hospital."
                        : "Emergency alerts, patient details, hospital assignment, and ambulance response status.");
        result.addColumn("alert", "Alert");
        result.addColumn("patient", "Patient");
        result.addColumn("status", "Status");
        result.addColumn("bpm", "BPM");
        result.addColumn("hospital", "Assigned Hospital");
        result.addColumn("ambulance", "Ambulance Status");
        result.addColumn("created", "Created");
        result.addColumn("contact", "Emergency Contact");

        String sql = "SELECT ea.alert_id, ea.bpm, ea.alert_status, ea.countdown_seconds, ea.created_at, "
                + "u.id AS user_id, u.first_name, u.surname, u.email, u.emergency_contact_name, u.emergency_contact_number, "
                + "h.name AS hospital_name, h.service_area, haa.status AS assignment_status, an.response_status "
                + "FROM emergency_alerts ea "
                + "JOIN users u ON u.id = ea.user_id "
                + "LEFT JOIN hospital_alert_assignments haa ON haa.alert_id = ea.alert_id "
                + "LEFT JOIN hospitals h ON h.hospital_id = haa.hospital_id "
                + "LEFT JOIN ambulance_notifications an ON an.alert_id = ea.alert_id "
                + "WHERE ea.created_at >= ? AND ea.created_at < ? "
                + "AND (? = '' OR ea.alert_status = ?) "
                + "AND (? <= 0 OR haa.hospital_id = ?) "
                + "AND (? = '' OR LOWER(u.first_name) LIKE ? OR LOWER(u.surname) LIKE ? OR LOWER(u.email) LIKE ? OR LOWER(COALESCE(h.name, '')) LIKE ?) "
                + "ORDER BY ea.created_at DESC, ea.alert_id DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            index = setRange(ps, index, start, end);
            ps.setString(index++, status);
            ps.setString(index++, status);
            ps.setInt(index++, hospitalId);
            ps.setInt(index++, hospitalId);
            ps.setString(index++, search);
            ps.setString(index++, pattern);
            ps.setString(index++, pattern);
            ps.setString(index++, pattern);
            ps.setString(index, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> row = row();
                    row.put("alert", "#" + rs.getInt("alert_id"));
                    row.put("patient", rs.getInt("user_id") + " - " + value(rs.getString("first_name")) + " " + value(rs.getString("surname")) + "\n" + value(rs.getString("email")));
                    row.put("status", value(rs.getString("alert_status")));
                    row.put("bpm", value(rs.getString("bpm")));
                    row.put("hospital", value(rs.getString("hospital_name")) + hospitalArea(rs.getString("service_area"), rs.getString("assignment_status")));
                    row.put("ambulance", valueOrDefault(rs.getString("response_status"), "Not sent"));
                    row.put("created", formatTimestamp(rs.getTimestamp("created_at")));
                    row.put("contact", value(rs.getString("emergency_contact_name")) + "\n" + value(rs.getString("emergency_contact_number")));
                    result.addRow(row);
                }
            }
        }
        return result;
    }

    private static ReportResult loadSyncReport(Connection conn, ReportCriteria criteria) throws SQLException {
        Timestamp start = startTimestamp(criteria);
        Timestamp end = endExclusiveTimestamp(criteria);
        int patientId = parseId(criteria.getPatientId());
        String search = clean(criteria.getSearch());
        String pattern = likePattern(search);

        ReportResult result = baseResult(
                REPORT_SYNC,
                "Mobile Sync Activity Report",
                "Mobile and watch sync activity by patient and device for the selected period.");
        result.addColumn("patient", "Patient");
        result.addColumn("device", "Device");
        result.addColumn("platform", "Platform / Source");
        result.addColumn("syncEvents", "Sync Events");
        result.addColumn("sections", "Section Summaries");
        result.addColumn("samples", "Samples");
        result.addColumn("latest", "Latest Sync");

        String sql = "SELECT u.id, u.first_name, u.surname, u.email, "
                + "d.device_type, d.manufacturer, d.device_model, d.platform, "
                + "COUNT(DISTINCT dse.sync_id) AS sync_events, MAX(dse.synced_for) AS latest_sync, "
                + "COUNT(DISTINCT hss.section_id) AS section_count, "
                + "COALESCE(SUM(hss.heart_rate_count), 0) AS heart_samples, "
                + "COALESCE(SUM(hss.temperature_count), 0) AS temperature_samples, "
                + "COALESCE(SUM(hss.blood_pressure_count), 0) AS bp_samples, "
                + "MAX(hss.window_end) AS latest_section, MAX(hss.source) AS latest_source "
                + "FROM users u "
                + "LEFT JOIN devices d ON d.user_id = u.id "
                + "LEFT JOIN device_sync_events dse ON dse.user_id = u.id "
                + "AND (d.device_id IS NULL OR dse.device_id = d.device_id) "
                + "AND dse.synced_for >= ? AND dse.synced_for < ? "
                + "LEFT JOIN health_sync_sections hss ON hss.user_id = u.id "
                + "AND (d.device_id IS NULL OR hss.device_id = d.device_id) "
                + "AND hss.window_end >= ? AND hss.window_end < ? "
                + "WHERE (? <= 0 OR u.id = ?) "
                + "AND (? = '' OR LOWER(u.first_name) LIKE ? OR LOWER(u.surname) LIKE ? OR LOWER(u.email) LIKE ? "
                + "OR LOWER(COALESCE(d.platform, '')) LIKE ? OR LOWER(COALESCE(hss.source, '')) LIKE ?) "
                + "GROUP BY u.id, u.first_name, u.surname, u.email, d.device_id, d.device_type, d.manufacturer, d.device_model, d.platform "
                + "ORDER BY MAX(dse.synced_for) DESC, MAX(hss.window_end) DESC, u.surname, u.first_name";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            index = setRange(ps, index, start, end);
            index = setRange(ps, index, start, end);
            ps.setInt(index++, patientId);
            ps.setInt(index++, patientId);
            ps.setString(index++, search);
            ps.setString(index++, pattern);
            ps.setString(index++, pattern);
            ps.setString(index++, pattern);
            ps.setString(index++, pattern);
            ps.setString(index, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long samples = rs.getLong("heart_samples") + rs.getLong("temperature_samples") + rs.getLong("bp_samples");
                    Map<String, String> row = row();
                    row.put("patient", rs.getInt("id") + " - " + value(rs.getString("first_name")) + " " + value(rs.getString("surname")) + "\n" + value(rs.getString("email")));
                    row.put("device", formatDevice(rs.getString("manufacturer"), rs.getString("device_model"), rs.getString("device_type")));
                    row.put("platform", valueOrDefault(rs.getString("latest_source"), valueOrDefault(rs.getString("platform"), "No sync source")));
                    row.put("syncEvents", String.valueOf(rs.getInt("sync_events")));
                    row.put("sections", String.valueOf(rs.getInt("section_count")));
                    row.put("samples", String.valueOf(samples));
                    row.put("latest", latestTimestamp(rs.getTimestamp("latest_sync"), rs.getTimestamp("latest_section")));
                    result.addRow(row);
                }
            }
        }
        return result;
    }

    private static ReportResult baseResult(String type, String title, String description) {
        ReportResult result = new ReportResult();
        result.setReportType(type);
        result.setTitle(title);
        result.setDescription(description);
        return result;
    }

    private static void addMetric(ReportResult result, String metric, int value, String meaning) {
        Map<String, String> row = row();
        row.put("metric", metric);
        row.put("value", String.valueOf(value));
        row.put("meaning", meaning);
        result.addRow(row);
    }

    private static int queryInt(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private static void setParams(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object value = params[i];
            if (value instanceof Timestamp) {
                ps.setTimestamp(i + 1, (Timestamp) value);
            } else if (value instanceof Integer) {
                ps.setInt(i + 1, (Integer) value);
            } else {
                ps.setString(i + 1, value == null ? "" : String.valueOf(value));
            }
        }
    }

    private static int setRange(PreparedStatement ps, int index, Timestamp start, Timestamp end) throws SQLException {
        ps.setTimestamp(index++, start);
        ps.setTimestamp(index++, end);
        return index;
    }

    private static Timestamp startTimestamp(ReportCriteria criteria) {
        LocalDate start = criteria.getStartDate();
        return Timestamp.valueOf(start.atStartOfDay());
    }

    private static Timestamp endExclusiveTimestamp(ReportCriteria criteria) {
        LocalDate end = criteria.getEndDate();
        return Timestamp.valueOf(end.plusDays(1).atStartOfDay());
    }

    private static Map<String, String> row() {
        return new LinkedHashMap<>();
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private static String likePattern(String value) {
        return "%" + clean(value) + "%";
    }

    private static int parseId(String value) {
        if (value == null || value.trim().isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static BigDecimal getBigDecimal(ResultSet rs, String column) throws SQLException {
        BigDecimal value = rs.getBigDecimal(column);
        return rs.wasNull() ? null : value;
    }

    private static String formatDecimal(BigDecimal value, String suffix) {
        if (value == null) {
            return "No data";
        }
        return value.setScale(1, java.math.RoundingMode.HALF_UP).toPlainString() + suffix;
    }

    private static String formatBloodPressure(BigDecimal systolic, BigDecimal diastolic) {
        if (systolic == null || diastolic == null) {
            return "No data";
        }
        return systolic.setScale(1, java.math.RoundingMode.HALF_UP).toPlainString()
                + "/"
                + diastolic.setScale(1, java.math.RoundingMode.HALF_UP).toPlainString()
                + " mmHg";
    }

    private static String latestTimestamp(Timestamp... values) {
        Timestamp latest = null;
        for (Timestamp value : values) {
            if (value != null && (latest == null || value.after(latest))) {
                latest = value;
            }
        }
        return formatTimestamp(latest);
    }

    private static String formatTimestamp(Timestamp value) {
        return value == null ? "No data" : value.toString();
    }

    private static String value(String value) {
        return value == null ? "" : value;
    }

    private static String valueOrDefault(String value, String fallback) {
        String cleaned = value(value).trim();
        return cleaned.isEmpty() ? fallback : cleaned;
    }

    private static String hospitalArea(String serviceArea, String assignmentStatus) {
        String area = value(serviceArea).trim();
        String status = value(assignmentStatus).trim();
        if (area.isEmpty() && status.isEmpty()) {
            return "";
        }
        if (status.isEmpty()) {
            return "\n" + area;
        }
        if (area.isEmpty()) {
            return "\n" + status;
        }
        return "\n" + area + " - " + status;
    }

    private static String formatDevice(String manufacturer, String model, String type) {
        StringBuilder builder = new StringBuilder();
        appendPart(builder, manufacturer);
        appendPart(builder, model);
        appendPart(builder, type);
        return builder.length() == 0 ? "No registered device" : builder.toString();
    }

    private static void appendPart(StringBuilder builder, String value) {
        String cleaned = value(value).trim();
        if (cleaned.isEmpty()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(' ');
        }
        builder.append(cleaned);
    }

}
