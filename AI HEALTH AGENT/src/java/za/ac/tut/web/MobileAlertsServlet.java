package za.ac.tut.web;

import java.io.IOException;
import java.io.PrintWriter;
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
import za.ac.tut.util.JsonUtil;

public class MobileAlertsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        Integer userId = resolveUserId(request);
        String email = resolveEmail(request);

        try (Connection conn = Database.getConnection()) {
            if (userId == null && email != null) {
                userId = findUserIdByEmail(conn, email);
            }

            if (userId == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJson(response, "{\"success\":false,\"message\":\"Sign in or provide an email address to check alerts.\"}");
                return;
            }

            writeJson(response, buildLatestAlertJson(conn, userId));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(response, "{\"success\":false,\"message\":\"Unable to check emergency alerts.\"}");
        }
    }

    private Integer findUserIdByEmail(Connection conn, String email) throws Exception {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("id") : null;
            }
        }
    }

    private String buildLatestAlertJson(Connection conn, int userId) throws Exception {
        String sql = "SELECT ea.alert_id, ea.bpm, ea.alert_status, ea.countdown_seconds, ea.created_at, "
                + "h.name AS hospital_name, h.service_area, haa.status AS assignment_status "
                + "FROM emergency_alerts ea "
                + "LEFT JOIN hospital_alert_assignments haa ON haa.alert_id = ea.alert_id "
                + "LEFT JOIN hospitals h ON h.hospital_id = haa.hospital_id "
                + "WHERE ea.user_id = ? AND ea.created_at >= ? "
                + "ORDER BY ea.created_at DESC, ea.alert_id DESC LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis() - 24L * 60L * 60L * 1000L));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return "{\"success\":true,\"hasAlert\":false,\"alert\":null}";
                }

                Timestamp createdAt = rs.getTimestamp("created_at");
                String status = rs.getString("alert_status");
                String hospitalName = rs.getString("hospital_name");
                String message = hospitalName == null || hospitalName.trim().isEmpty()
                        ? "Emergency alert recorded. Staff should review this patient now."
                        : "Emergency alert sent to " + hospitalName + ".";

                return "{"
                        + "\"success\":true,"
                        + "\"hasAlert\":true,"
                        + "\"alert\":{"
                        + "\"id\":" + rs.getInt("alert_id") + ","
                        + "\"status\":" + JsonUtil.quote(status) + ","
                        + "\"bpm\":" + (rs.getObject("bpm") == null ? "null" : rs.getInt("bpm")) + ","
                        + "\"countdownSeconds\":" + rs.getInt("countdown_seconds") + ","
                        + "\"createdAt\":" + JsonUtil.quote(createdAt == null ? null : createdAt.toInstant().toString()) + ","
                        + "\"hospitalName\":" + JsonUtil.quote(hospitalName) + ","
                        + "\"hospitalServiceArea\":" + JsonUtil.quote(rs.getString("service_area")) + ","
                        + "\"assignmentStatus\":" + JsonUtil.quote(rs.getString("assignment_status")) + ","
                        + "\"message\":" + JsonUtil.quote(message)
                        + "}"
                        + "}";
            }
        }
    }

    private Integer resolveUserId(HttpServletRequest request) {
        String userIdParam = trimToNull(request.getParameter("userId"));
        if (userIdParam != null) {
            try {
                return Integer.valueOf(userIdParam);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return null;
        }
        return Integer.valueOf(String.valueOf(session.getAttribute("userId")));
    }

    private String resolveEmail(HttpServletRequest request) {
        String email = trimToNull(request.getParameter("email"));
        if (email != null) {
            return email;
        }

        HttpSession session = request.getSession(false);
        Object sessionEmail = session == null ? null : session.getAttribute("user");
        return sessionEmail == null ? null : String.valueOf(sessionEmail);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void writeJson(HttpServletResponse response, String json) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.write(json);
        }
    }
}
