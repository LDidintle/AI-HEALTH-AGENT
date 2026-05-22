package za.ac.tut.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import za.ac.tut.util.Database;
import za.ac.tut.util.HealthRiskPredictionService;
import za.ac.tut.util.JsonUtil;

public class ReadingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(response, "{\"success\":false,\"message\":\"No active user session.\"}");
            return;
        }

        String email = String.valueOf(session.getAttribute("user"));

        try {
            try (Connection conn = Database.getConnection()) {

                Integer userId = findUserIdByEmail(conn, email);
                if (userId == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    writeJson(response, "{\"success\":false,\"message\":\"User was not found.\"}");
                    return;
                }

                Integer latestHeartRate = getLatestHeartRate(conn, userId);
                String latestTemperature = getLatestTemperature(conn, userId);
                String latestBloodPressure = getLatestBloodPressure(conn, userId);
                Timestamp latestSyncedAt = getLatestSyncedAt(conn, userId);
                String latestSectionJson = safeJsonObject(() -> getLatestSectionJson(conn, userId), "null");
                String trendPointsJson = safeJsonObject(() -> getTrendPointsJson(conn, userId), "[]");
                String activeAlertJson = safeJsonObject(() -> getActiveAlertJson(conn, userId), "null");
                String predictionJson = HealthRiskPredictionService.toJson(
                        HealthRiskPredictionService.predictForUser(conn, userId));

                String json = "{"
                        + "\"success\":true,"
                        + "\"email\":" + JsonUtil.quote(email) + ","
                        + "\"heartRate\":" + (latestHeartRate == null ? "null" : latestHeartRate) + ","
                        + "\"temperature\":" + (latestTemperature == null ? "null" : latestTemperature) + ","
                        + "\"bloodPressure\":" + (latestBloodPressure == null ? "null" : JsonUtil.quote(latestBloodPressure)) + ","
                        + "\"latestSyncedAt\":" + (latestSyncedAt == null ? "null" : JsonUtil.quote(latestSyncedAt.toInstant().toString())) + ","
                        + "\"latestSection\":" + latestSectionJson + ","
                        + "\"trendPoints\":" + trendPointsJson + ","
                        + "\"activeAlert\":" + activeAlertJson + ","
                        + "\"prediction\":" + predictionJson
                        + "}";

                writeJson(response, json);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(response, "{\"success\":false,\"message\":\"Unable to load readings.\"}");
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

    private Integer getLatestHeartRate(Connection conn, int userId) throws Exception {
        String sql = "SELECT bpm FROM pulse_readings WHERE user_id = ? ORDER BY synced_at DESC, recorded_at DESC, pulse_id DESC LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("bpm");
                }
            }
        }

        return null;
    }

    private String getLatestTemperature(Connection conn, int userId) throws Exception {
        String sql = "SELECT temperature FROM temperature_readings WHERE user_id = ? ORDER BY synced_at DESC, recorded_at DESC, temp_id DESC LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("temperature").toPlainString();
                }
            }
        }

        return null;
    }

    private String getLatestBloodPressure(Connection conn, int userId) throws Exception {
        String sql = "SELECT systolic, diastolic FROM blood_pressure_readings "
                + "WHERE user_id = ? ORDER BY synced_at DESC, recorded_at DESC, bp_id DESC LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("systolic") + "/" + rs.getInt("diastolic");
                }
            }
        }

        return null;
    }

    private String getLatestSectionJson(Connection conn, int userId) throws Exception {
        String sql = "SELECT h.source, h.window_start, h.window_end, h.heart_rate_latest, "
                + "h.heart_rate_min, h.heart_rate_max, h.heart_rate_average, h.heart_rate_count, "
                + "h.temperature_count, h.blood_pressure_count, h.created_at, "
                + "d.device_type, d.manufacturer, d.device_model "
                + "FROM health_sync_sections h "
                + "LEFT JOIN devices d ON h.device_id = d.device_id "
                + "WHERE h.user_id = ? "
                + "ORDER BY h.window_end DESC, h.section_id DESC LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return "{"
                            + "\"source\":" + JsonUtil.quote(rs.getString("source")) + ","
                            + "\"windowStart\":" + quoteTimestamp(rs.getTimestamp("window_start")) + ","
                            + "\"windowEnd\":" + quoteTimestamp(rs.getTimestamp("window_end")) + ","
                            + "\"heartRateLatest\":" + nullableInteger(rs, "heart_rate_latest") + ","
                            + "\"heartRateMin\":" + nullableInteger(rs, "heart_rate_min") + ","
                            + "\"heartRateMax\":" + nullableInteger(rs, "heart_rate_max") + ","
                            + "\"heartRateAverage\":" + nullableDecimal(rs, "heart_rate_average") + ","
                            + "\"heartRateCount\":" + rs.getInt("heart_rate_count") + ","
                            + "\"temperatureCount\":" + rs.getInt("temperature_count") + ","
                            + "\"bloodPressureCount\":" + rs.getInt("blood_pressure_count") + ","
                            + "\"createdAt\":" + quoteTimestamp(rs.getTimestamp("created_at")) + ","
                            + "\"deviceType\":" + JsonUtil.quote(rs.getString("device_type")) + ","
                            + "\"manufacturer\":" + JsonUtil.quote(rs.getString("manufacturer")) + ","
                            + "\"deviceModel\":" + JsonUtil.quote(rs.getString("device_model"))
                            + "}";
                }
            }
        }

        return "null";
    }

    private String getTrendPointsJson(Connection conn, int userId) throws Exception {
        String sql = "SELECT heart_rate_latest, systolic_latest, diastolic_latest, "
                + "temperature_latest, window_end "
                + "FROM ("
                + "SELECT heart_rate_latest, systolic_latest, diastolic_latest, temperature_latest, window_end, section_id "
                + "FROM health_sync_sections WHERE user_id = ? "
                + "ORDER BY window_end DESC, section_id DESC LIMIT 12"
                + ") latest_sections ORDER BY window_end ASC";

        StringBuilder json = new StringBuilder("[");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                boolean first = true;
                while (rs.next()) {
                    if (!first) {
                        json.append(",");
                    }
                    first = false;

                    json.append("{")
                            .append("\"heartRate\":").append(nullableInteger(rs, "heart_rate_latest")).append(",")
                            .append("\"systolic\":").append(nullableInteger(rs, "systolic_latest")).append(",")
                            .append("\"diastolic\":").append(nullableInteger(rs, "diastolic_latest")).append(",")
                            .append("\"temperature\":").append(nullableDecimal(rs, "temperature_latest")).append(",")
                            .append("\"recordedAt\":").append(quoteTimestamp(rs.getTimestamp("window_end")))
                            .append("}");
                }
            }
        }
        json.append("]");
        return json.toString();
    }

    private Timestamp getLatestSyncedAt(Connection conn, int userId) throws Exception {
        String sql = "SELECT MAX(synced_at) AS latest_synced_at FROM ("
                + "SELECT synced_at FROM pulse_readings WHERE user_id = ? "
                + "UNION ALL SELECT synced_at FROM temperature_readings WHERE user_id = ? "
                + "UNION ALL SELECT synced_at FROM blood_pressure_readings WHERE user_id = ?"
                + ") synced_readings";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp("latest_synced_at");
                }
            }
        }

        return null;
    }

    private String getActiveAlertJson(Connection conn, int userId) throws Exception {
        String sql = "SELECT alert_id, bpm, alert_status, countdown_seconds, created_at "
                + "FROM emergency_alerts "
                + "WHERE user_id = ? AND created_at >= ? "
                + "ORDER BY created_at DESC, alert_id DESC LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis() - 60L * 60L * 1000L));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    return "{"
                            + "\"id\":" + rs.getInt("alert_id") + ","
                            + "\"bpm\":" + (rs.getObject("bpm") == null ? "null" : rs.getInt("bpm")) + ","
                            + "\"status\":" + JsonUtil.quote(rs.getString("alert_status")) + ","
                            + "\"countdownSeconds\":" + rs.getInt("countdown_seconds") + ","
                            + "\"createdAt\":" + JsonUtil.quote(createdAt == null ? null : createdAt.toInstant().toString())
                            + "}";
                }
            }
        }

        return "null";
    }

    private String nullableInteger(ResultSet rs, String column) throws Exception {
        Object value = rs.getObject(column);
        return value == null ? "null" : String.valueOf(rs.getInt(column));
    }

    private String nullableDecimal(ResultSet rs, String column) throws Exception {
        BigDecimal value = rs.getBigDecimal(column);
        return value == null ? "null" : value.toPlainString();
    }

    private String quoteTimestamp(Timestamp timestamp) {
        return timestamp == null ? "null" : JsonUtil.quote(timestamp.toInstant().toString());
    }

    private String safeJsonObject(JsonSupplier supplier, String fallback) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return fallback;
        }
    }

    private void writeJson(HttpServletResponse response, String json) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.write(json);
        }
    }

    private interface JsonSupplier {
        String get() throws Exception;
    }
}
