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
import za.ac.tut.util.JsonUtil;

public class MobileHealthSyncServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        String email = resolveEmail(request);
        if (email == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"email is required when no session is active.\"}");
            return;
        }

        try {
            try (Connection conn = Database.getConnection()) {

                Integer userId = findUserIdByEmail(conn, email);
                if (userId == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    writeJson(response, "{\"success\":false,\"message\":\"User was not found.\"}");
                    return;
                }

                String json = "{"
                        + "\"success\":true,"
                        + "\"email\":" + JsonUtil.quote(email) + ","
                        + "\"heartRate\":" + latestHeartRateJson(conn, userId) + ","
                        + "\"temperature\":" + latestTemperatureJson(conn, userId) + ","
                        + "\"bloodPressure\":" + latestBloodPressureJson(conn, userId)
                        + "}";

                writeJson(response, json);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(response, "{\"success\":false,\"message\":\"Unable to read synchronized data.\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        String email = resolveEmail(request);
        String source = trimToNull(request.getParameter("source"));
        String heartRateValue = trimToNull(request.getParameter("heartRate"));
        String temperatureValue = trimToNull(request.getParameter("temperature"));
        String systolicValue = trimToNull(request.getParameter("systolic"));
        String diastolicValue = trimToNull(request.getParameter("diastolic"));
        String recordedAt = trimToNull(request.getParameter("recordedAt"));
        String externalRecordId = trimToNull(request.getParameter("externalRecordId"));
        String deviceType = trimToNull(request.getParameter("deviceType"));
        String deviceManufacturer = trimToNull(request.getParameter("deviceManufacturer"));
        String deviceModel = trimToNull(request.getParameter("deviceModel"));

        if (email == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"email is required when no session is active.\"}");
            return;
        }

        if (heartRateValue == null && temperatureValue == null
                && (systolicValue == null || diastolicValue == null)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"Provide heartRate, temperature, or systolic and diastolic.\"}");
            return;
        }

        try {
            Integer heartRate = heartRateValue == null ? null : Integer.valueOf(heartRateValue);
            BigDecimal temperature = temperatureValue == null ? null : new BigDecimal(temperatureValue);
            Integer systolic = systolicValue == null ? null : Integer.valueOf(systolicValue);
            Integer diastolic = diastolicValue == null ? null : Integer.valueOf(diastolicValue);
            Timestamp recordedTimestamp = parseRecordedAt(recordedAt);

            try (Connection conn = Database.getConnection()) {

                Integer userId = findUserIdByEmail(conn, email);
                if (userId == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    writeJson(response, "{\"success\":false,\"message\":\"User was not found.\"}");
                    return;
                }

                conn.setAutoCommit(false);

                Integer deviceId = upsertDevice(conn, userId, source, deviceType, deviceManufacturer, deviceModel);

                if (heartRate != null) {
                    insertHeartRate(conn, userId, deviceId, heartRate, recordedTimestamp, source, externalRecordId);
                }
                if (temperature != null) {
                    insertTemperature(conn, userId, deviceId, temperature, recordedTimestamp, source, externalRecordId);
                }
                if (systolic != null && diastolic != null) {
                    insertBloodPressure(conn, userId, deviceId, systolic, diastolic, recordedTimestamp, source, externalRecordId);
                }

                insertSyncLog(conn, userId, deviceId, source, externalRecordId, recordedTimestamp);
                conn.commit();

                String json = "{"
                        + "\"success\":true,"
                        + "\"message\":\"Health readings synchronized successfully.\","
                        + "\"email\":" + JsonUtil.quote(email) + ","
                        + "\"source\":" + JsonUtil.quote(source == null ? "HEALTH_CONNECT" : source)
                        + "}";

                writeJson(response, json);
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"Numeric health values are invalid.\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(response, "{\"success\":false,\"message\":\"Unable to synchronize health readings.\"}");
        }
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

    private Integer upsertDevice(Connection conn, int userId, String source, String deviceType,
            String deviceManufacturer, String deviceModel) throws Exception {
        if (deviceType == null && deviceManufacturer == null && deviceModel == null) {
            return null;
        }

        String normalizedSource = source == null ? "HEALTH_CONNECT" : source;
        String normalizedType = deviceType == null ? "UNKNOWN" : deviceType;

        String selectSql = "SELECT device_id FROM devices "
                + "WHERE user_id = ? AND platform = ? AND device_type = ? "
                + "AND COALESCE(manufacturer, '') = COALESCE(?, '') "
                + "AND COALESCE(device_model, '') = COALESCE(?, '')";

        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setInt(1, userId);
            ps.setString(2, normalizedSource);
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
            ps.setString(5, normalizedSource);
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

    private void insertHeartRate(Connection conn, int userId, Integer deviceId, int heartRate,
            Timestamp recordedAt, String source, String externalRecordId) throws Exception {
        String sql = "INSERT INTO pulse_readings "
                + "(user_id, device_id, bpm, status, recorded_at, measured_at, synced_at, source, external_record_id) "
                + "VALUES (?, ?, ?, ?, COALESCE(?, CURRENT_TIMESTAMP), COALESCE(?, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            setInteger(ps, 2, deviceId);
            ps.setInt(3, heartRate);
            ps.setString(4, classifyHeartRate(heartRate));
            setTimestamp(ps, 5, recordedAt);
            setTimestamp(ps, 6, recordedAt);
            ps.setString(7, source == null ? "HEALTH_CONNECT" : source);
            ps.setString(8, externalRecordId);
            ps.executeUpdate();
        }
    }

    private void insertTemperature(Connection conn, int userId, Integer deviceId, BigDecimal temperature,
            Timestamp recordedAt, String source, String externalRecordId) throws Exception {
        String sql = "INSERT INTO temperature_readings "
                + "(user_id, device_id, temperature, status, recorded_at, measured_at, synced_at, source, external_record_id) "
                + "VALUES (?, ?, ?, ?, COALESCE(?, CURRENT_TIMESTAMP), COALESCE(?, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            setInteger(ps, 2, deviceId);
            ps.setBigDecimal(3, temperature);
            ps.setString(4, classifyTemperature(temperature));
            setTimestamp(ps, 5, recordedAt);
            setTimestamp(ps, 6, recordedAt);
            ps.setString(7, source == null ? "HEALTH_CONNECT" : source);
            ps.setString(8, externalRecordId);
            ps.executeUpdate();
        }
    }

    private void insertBloodPressure(Connection conn, int userId, Integer deviceId, int systolic,
            int diastolic, Timestamp recordedAt, String source, String externalRecordId) throws Exception {
        String sql = "INSERT INTO blood_pressure_readings "
                + "(user_id, device_id, systolic, diastolic, status, recorded_at, measured_at, synced_at, source, external_record_id) "
                + "VALUES (?, ?, ?, ?, ?, COALESCE(?, CURRENT_TIMESTAMP), COALESCE(?, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            setInteger(ps, 2, deviceId);
            ps.setInt(3, systolic);
            ps.setInt(4, diastolic);
            ps.setString(5, classifyBloodPressure(systolic, diastolic));
            setTimestamp(ps, 6, recordedAt);
            setTimestamp(ps, 7, recordedAt);
            ps.setString(8, source == null ? "HEALTH_CONNECT" : source);
            ps.setString(9, externalRecordId);
            ps.executeUpdate();
        }
    }

    private void insertSyncLog(Connection conn, int userId, Integer deviceId, String source,
            String externalRecordId, Timestamp recordedAt) throws Exception {
        String sql = "INSERT INTO device_sync_events (user_id, device_id, source_platform, external_record_id, synced_for, sync_status) "
                + "VALUES (?, ?, ?, ?, COALESCE(?, CURRENT_TIMESTAMP), ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            setInteger(ps, 2, deviceId);
            ps.setString(3, source == null ? "HEALTH_CONNECT" : source);
            ps.setString(4, externalRecordId);
            setTimestamp(ps, 5, recordedAt);
            ps.setString(6, "SYNCED");
            ps.executeUpdate();
        }
    }

    private String latestHeartRateJson(Connection conn, int userId) throws Exception {
        String sql = "SELECT bpm, status, recorded_at, source FROM pulse_readings "
                + "WHERE user_id = ? ORDER BY recorded_at DESC, pulse_id DESC LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return "{"
                            + "\"value\":" + rs.getInt("bpm") + ","
                            + "\"status\":" + JsonUtil.quote(rs.getString("status")) + ","
                            + "\"recordedAt\":" + JsonUtil.quote(String.valueOf(rs.getTimestamp("recorded_at"))) + ","
                            + "\"source\":" + JsonUtil.quote(rs.getString("source"))
                            + "}";
                }
            }
        }

        return "null";
    }

    private String latestTemperatureJson(Connection conn, int userId) throws Exception {
        String sql = "SELECT temperature, status, recorded_at, source FROM temperature_readings "
                + "WHERE user_id = ? ORDER BY recorded_at DESC, temp_id DESC LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return "{"
                            + "\"value\":" + rs.getBigDecimal("temperature").toPlainString() + ","
                            + "\"status\":" + JsonUtil.quote(rs.getString("status")) + ","
                            + "\"recordedAt\":" + JsonUtil.quote(String.valueOf(rs.getTimestamp("recorded_at"))) + ","
                            + "\"source\":" + JsonUtil.quote(rs.getString("source"))
                            + "}";
                }
            }
        }

        return "null";
    }

    private String latestBloodPressureJson(Connection conn, int userId) throws Exception {
        String sql = "SELECT systolic, diastolic, status, recorded_at, source FROM blood_pressure_readings "
                + "WHERE user_id = ? ORDER BY recorded_at DESC, bp_id DESC LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return "{"
                            + "\"systolic\":" + rs.getInt("systolic") + ","
                            + "\"diastolic\":" + rs.getInt("diastolic") + ","
                            + "\"status\":" + JsonUtil.quote(rs.getString("status")) + ","
                            + "\"recordedAt\":" + JsonUtil.quote(String.valueOf(rs.getTimestamp("recorded_at"))) + ","
                            + "\"source\":" + JsonUtil.quote(rs.getString("source"))
                            + "}";
                }
            }
        }

        return "null";
    }

    private String classifyHeartRate(int heartRate) {
        if (heartRate < 50) {
            return "LOW";
        }
        if (heartRate > 100) {
            return "HIGH";
        }
        return "NORMAL";
    }

    private String classifyTemperature(BigDecimal temperature) {
        if (temperature.compareTo(new BigDecimal("36.00")) < 0) {
            return "LOW";
        }
        if (temperature.compareTo(new BigDecimal("37.50")) > 0) {
            return "HIGH";
        }
        return "NORMAL";
    }

    private String classifyBloodPressure(int systolic, int diastolic) {
        if (systolic >= 140 || diastolic >= 90) {
            return "HIGH";
        }
        if (systolic < 90 || diastolic < 60) {
            return "LOW";
        }
        return "NORMAL";
    }

    private Timestamp parseRecordedAt(String recordedAt) {
        if (recordedAt == null) {
            return null;
        }

        String normalized = recordedAt.trim().replace("T", " ");
        if (normalized.endsWith("Z")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        if (normalized.length() == 16) {
            normalized = normalized + ":00";
        }

        return Timestamp.valueOf(normalized);
    }

    private void setTimestamp(PreparedStatement ps, int index, Timestamp timestamp) throws Exception {
        if (timestamp == null) {
            ps.setNull(index, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(index, timestamp);
        }
    }

    private void setInteger(PreparedStatement ps, int index, Integer value) throws Exception {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String resolveEmail(HttpServletRequest request) {
        String email = trimToNull(request.getParameter("email"));
        if (email != null) {
            return email;
        }

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
}
