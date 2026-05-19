package za.ac.tut.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import za.ac.tut.util.Database;
import za.ac.tut.util.VitalAlertEvaluator;

public class MobileHealthSectionSyncServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        String email = resolveEmail(request);
        if (email == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(response, "{\"success\":false,\"message\":\"Sign in before synchronizing health sections.\"}");
            return;
        }

        try {
            SectionPayload payload = parsePayload(request);
            if (!payload.hasAnyReading()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJson(response, "{\"success\":false,\"message\":\"Provide at least one section reading.\"}");
                return;
            }

            try (Connection conn = Database.getConnection()) {
                Integer userId = findUserIdByEmail(conn, email);
                if (userId == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    writeJson(response, "{\"success\":false,\"message\":\"User was not found.\"}");
                    return;
                }

                conn.setAutoCommit(false);
                Integer deviceId = upsertDevice(
                        conn,
                        userId,
                        payload.source,
                        payload.deviceType,
                        payload.deviceManufacturer,
                        payload.deviceModel
                );

                insertSection(conn, userId, deviceId, payload);

                if (payload.heartRateLatest != null) {
                    insertHeartRate(conn, userId, deviceId, payload);
                }
                if (payload.temperatureLatest != null) {
                    insertTemperature(conn, userId, deviceId, payload);
                }
                if (payload.systolicLatest != null && payload.diastolicLatest != null) {
                    insertBloodPressure(conn, userId, deviceId, payload);
                }

                VitalAlertEvaluator.evaluateAndStore(
                        conn,
                        userId,
                        payload.heartRateLatest,
                        payload.temperatureLatest,
                        payload.systolicLatest,
                        payload.diastolicLatest
                );
                insertSyncLog(conn, userId, deviceId, payload);
                conn.commit();
            }

            writeJson(response, "{\"success\":true,\"message\":\"Health section synchronized successfully.\"}");
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"Health section values are invalid.\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(response, "{\"success\":false,\"message\":\"Unable to synchronize health section.\"}");
        }
    }

    private SectionPayload parsePayload(HttpServletRequest request) {
        String windowStart = requireParameter(request, "windowStart");
        String windowEnd = requireParameter(request, "windowEnd");

        SectionPayload payload = new SectionPayload();
        payload.windowStart = parseTimestamp(windowStart);
        payload.windowEnd = parseTimestamp(windowEnd);
        payload.source = defaultString(trimToNull(request.getParameter("source")), "HEALTH_CONNECT_SECTION");
        payload.heartRateLatest = parseInteger(request, "heartRateLatest");
        payload.heartRateMin = parseInteger(request, "heartRateMin");
        payload.heartRateMax = parseInteger(request, "heartRateMax");
        payload.heartRateAverage = parseDecimal(request, "heartRateAverage");
        payload.heartRateCount = parseCount(request, "heartRateCount");
        payload.temperatureLatest = parseDecimal(request, "temperatureLatest");
        payload.temperatureMin = parseDecimal(request, "temperatureMin");
        payload.temperatureMax = parseDecimal(request, "temperatureMax");
        payload.temperatureAverage = parseDecimal(request, "temperatureAverage");
        payload.temperatureCount = parseCount(request, "temperatureCount");
        payload.systolicLatest = parseInteger(request, "systolicLatest");
        payload.diastolicLatest = parseInteger(request, "diastolicLatest");
        payload.bloodPressureCount = parseCount(request, "bloodPressureCount");
        payload.deviceType = trimToNull(request.getParameter("deviceType"));
        payload.deviceManufacturer = trimToNull(request.getParameter("deviceManufacturer"));
        payload.deviceModel = trimToNull(request.getParameter("deviceModel"));
        return payload;
    }

    private void insertSection(Connection conn, int userId, Integer deviceId, SectionPayload payload) throws Exception {
        String sql = "INSERT INTO health_sync_sections "
                + "(user_id, device_id, source, window_start, window_end, "
                + "heart_rate_latest, heart_rate_min, heart_rate_max, heart_rate_average, heart_rate_count, "
                + "temperature_latest, temperature_min, temperature_max, temperature_average, temperature_count, "
                + "systolic_latest, diastolic_latest, blood_pressure_count, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            setInteger(ps, 2, deviceId);
            ps.setString(3, payload.source);
            ps.setTimestamp(4, payload.windowStart);
            ps.setTimestamp(5, payload.windowEnd);
            setInteger(ps, 6, payload.heartRateLatest);
            setInteger(ps, 7, payload.heartRateMin);
            setInteger(ps, 8, payload.heartRateMax);
            setDecimal(ps, 9, payload.heartRateAverage);
            ps.setInt(10, payload.heartRateCount);
            setDecimal(ps, 11, payload.temperatureLatest);
            setDecimal(ps, 12, payload.temperatureMin);
            setDecimal(ps, 13, payload.temperatureMax);
            setDecimal(ps, 14, payload.temperatureAverage);
            ps.setInt(15, payload.temperatureCount);
            setInteger(ps, 16, payload.systolicLatest);
            setInteger(ps, 17, payload.diastolicLatest);
            ps.setInt(18, payload.bloodPressureCount);
            ps.executeUpdate();
        }
    }

    private void insertHeartRate(Connection conn, int userId, Integer deviceId, SectionPayload payload) throws Exception {
        String sql = "INSERT INTO pulse_readings "
                + "(user_id, device_id, bpm, status, recorded_at, measured_at, synced_at, source, external_record_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            setInteger(ps, 2, deviceId);
            ps.setInt(3, payload.heartRateLatest);
            ps.setString(4, classifyHeartRate(payload.heartRateLatest));
            ps.setTimestamp(5, payload.windowEnd);
            ps.setTimestamp(6, payload.windowEnd);
            ps.setString(7, payload.source);
            ps.setString(8, sectionRecordId("heart", payload.windowEnd));
            ps.executeUpdate();
        }
    }

    private void insertTemperature(Connection conn, int userId, Integer deviceId, SectionPayload payload) throws Exception {
        String sql = "INSERT INTO temperature_readings "
                + "(user_id, device_id, temperature, status, recorded_at, measured_at, synced_at, source, external_record_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            setInteger(ps, 2, deviceId);
            ps.setBigDecimal(3, payload.temperatureLatest);
            ps.setString(4, classifyTemperature(payload.temperatureLatest));
            ps.setTimestamp(5, payload.windowEnd);
            ps.setTimestamp(6, payload.windowEnd);
            ps.setString(7, payload.source);
            ps.setString(8, sectionRecordId("temp", payload.windowEnd));
            ps.executeUpdate();
        }
    }

    private void insertBloodPressure(Connection conn, int userId, Integer deviceId, SectionPayload payload) throws Exception {
        String sql = "INSERT INTO blood_pressure_readings "
                + "(user_id, device_id, systolic, diastolic, status, recorded_at, measured_at, synced_at, source, external_record_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            setInteger(ps, 2, deviceId);
            ps.setInt(3, payload.systolicLatest);
            ps.setInt(4, payload.diastolicLatest);
            ps.setString(5, classifyBloodPressure(payload.systolicLatest, payload.diastolicLatest));
            ps.setTimestamp(6, payload.windowEnd);
            ps.setTimestamp(7, payload.windowEnd);
            ps.setString(8, payload.source);
            ps.setString(9, sectionRecordId("bp", payload.windowEnd));
            ps.executeUpdate();
        }
    }

    private void insertSyncLog(Connection conn, int userId, Integer deviceId, SectionPayload payload) throws Exception {
        String sql = "INSERT INTO device_sync_events "
                + "(user_id, device_id, source_platform, external_record_id, synced_for, sync_status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            setInteger(ps, 2, deviceId);
            ps.setString(3, payload.source);
            ps.setString(4, sectionRecordId("section", payload.windowEnd));
            ps.setTimestamp(5, payload.windowEnd);
            ps.setString(6, "SYNCED");
            ps.executeUpdate();
        }
    }

    private Integer upsertDevice(Connection conn, int userId, String source, String deviceType,
            String deviceManufacturer, String deviceModel) throws Exception {
        if (deviceType == null && deviceManufacturer == null && deviceModel == null) {
            return null;
        }

        String normalizedType = deviceType == null ? "UNKNOWN" : deviceType;
        String selectSql = "SELECT device_id FROM devices "
                + "WHERE user_id = ? AND platform = ? AND device_type = ? "
                + "AND COALESCE(manufacturer, '') = COALESCE(?, '') "
                + "AND COALESCE(device_model, '') = COALESCE(?, '')";

        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setInt(1, userId);
            ps.setString(2, source);
            ps.setString(3, normalizedType);
            ps.setString(4, deviceManufacturer);
            ps.setString(5, deviceModel);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("device_id");
                }
            }
        }

        String insertSql = "INSERT INTO devices (user_id, device_type, manufacturer, device_model, platform, active) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(insertSql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, normalizedType);
            ps.setString(3, deviceManufacturer);
            ps.setString(4, deviceModel);
            ps.setString(5, source);
            ps.setBoolean(6, true);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return null;
    }

    private Integer findUserIdByEmail(Connection conn, String email) throws Exception {
        String sql = "SELECT id FROM users WHERE email = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        return null;
    }

    private String classifyHeartRate(int heartRate) {
        if (heartRate < 50) return "LOW";
        if (heartRate > 100) return "HIGH";
        return "NORMAL";
    }

    private String classifyTemperature(BigDecimal temperature) {
        if (temperature.compareTo(new BigDecimal("36.00")) < 0) return "LOW";
        if (temperature.compareTo(new BigDecimal("37.50")) > 0) return "HIGH";
        return "NORMAL";
    }

    private String classifyBloodPressure(int systolic, int diastolic) {
        if (systolic >= 140 || diastolic >= 90) return "HIGH";
        if (systolic < 90 || diastolic < 60) return "LOW";
        return "NORMAL";
    }

    private String sectionRecordId(String prefix, Timestamp timestamp) {
        return prefix + "-section-" + timestamp.getTime();
    }

    private Integer parseInteger(HttpServletRequest request, String name) {
        String value = trimToNull(request.getParameter(name));
        return value == null ? null : Integer.valueOf(value);
    }

    private int parseCount(HttpServletRequest request, String name) {
        String value = trimToNull(request.getParameter(name));
        return value == null ? 0 : Integer.parseInt(value);
    }

    private BigDecimal parseDecimal(HttpServletRequest request, String name) {
        String value = trimToNull(request.getParameter(name));
        return value == null ? null : new BigDecimal(value);
    }

    private Timestamp parseTimestamp(String value) {
        String normalized = value.trim().replace("T", " ");
        if (normalized.endsWith("Z")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.length() == 16) {
            normalized = normalized + ":00";
        }
        return Timestamp.valueOf(normalized);
    }

    private void setInteger(PreparedStatement ps, int index, Integer value) throws Exception {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private void setDecimal(PreparedStatement ps, int index, BigDecimal value) throws Exception {
        if (value == null) {
            ps.setNull(index, Types.DECIMAL);
        } else {
            ps.setBigDecimal(index, value);
        }
    }

    private String requireParameter(HttpServletRequest request, String name) {
        String value = trimToNull(request.getParameter(name));
        if (value == null) {
            throw new IllegalArgumentException(name + " is required.");
        }
        return value;
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String defaultString(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String resolveEmail(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object sessionEmail = session.getAttribute("user");
        return sessionEmail == null ? null : String.valueOf(sessionEmail);
    }

    private void writeJson(HttpServletResponse response, String json) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.write(json);
        }
    }

    private static class SectionPayload {
        private Timestamp windowStart;
        private Timestamp windowEnd;
        private String source;
        private Integer heartRateLatest;
        private Integer heartRateMin;
        private Integer heartRateMax;
        private BigDecimal heartRateAverage;
        private int heartRateCount;
        private BigDecimal temperatureLatest;
        private BigDecimal temperatureMin;
        private BigDecimal temperatureMax;
        private BigDecimal temperatureAverage;
        private int temperatureCount;
        private Integer systolicLatest;
        private Integer diastolicLatest;
        private int bloodPressureCount;
        private String deviceType;
        private String deviceManufacturer;
        private String deviceModel;

        private boolean hasAnyReading() {
            return heartRateLatest != null || temperatureLatest != null
                    || (systolicLatest != null && diastolicLatest != null);
        }
    }
}
