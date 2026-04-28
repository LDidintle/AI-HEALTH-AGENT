package za.ac.tut.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import za.ac.tut.util.Database;
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

                String json = "{"
                        + "\"success\":true,"
                        + "\"email\":" + JsonUtil.quote(email) + ","
                        + "\"heartRate\":" + (latestHeartRate == null ? "null" : latestHeartRate) + ","
                        + "\"temperature\":" + (latestTemperature == null ? "null" : latestTemperature) + ","
                        + "\"bloodPressure\":" + (latestBloodPressure == null ? "null" : JsonUtil.quote(latestBloodPressure))
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
        String sql = "SELECT bpm FROM pulse_readings WHERE user_id = ? ORDER BY recorded_at DESC, pulse_id DESC LIMIT 1";

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
        String sql = "SELECT temperature FROM temperature_readings WHERE user_id = ? ORDER BY recorded_at DESC, temp_id DESC LIMIT 1";

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
                + "WHERE user_id = ? ORDER BY recorded_at DESC, bp_id DESC LIMIT 1";

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

    private void writeJson(HttpServletResponse response, String json) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.write(json);
        }
    }
}
