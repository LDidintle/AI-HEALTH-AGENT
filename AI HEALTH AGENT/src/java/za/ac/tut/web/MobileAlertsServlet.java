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
import za.ac.tut.util.AlertLifecycleService;
import za.ac.tut.util.AuditEventService;
import za.ac.tut.util.RateLimitService;
import za.ac.tut.util.VitalAlertEvaluator;

public class MobileAlertsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        Integer userId = resolveUserId(request);

        try (Connection conn = Database.getConnection()) {
            if (userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                writeJson(response, "{\"success\":false,\"message\":\"Sign in before checking alerts.\"}");
                return;
            }

            writeJson(response, buildLatestAlertJson(conn, userId));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(response, "{\"success\":false,\"message\":\"Unable to check emergency alerts.\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        Integer userId = resolveUserId(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(response, "{\"success\":false,\"message\":\"Sign in before creating demo alerts.\"}");
            return;
        }

        if (!RateLimitService.allow(RateLimitService.key("demo-alert", request.getRemoteAddr(), String.valueOf(userId)),
                3, 15L * 60L * 1000L)) {
            response.setStatus(429);
            writeJson(response, "{\"success\":false,\"message\":\"Too many demo alerts. Please wait before trying again.\"}");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            VitalAlertEvaluator.evaluateAndStore(conn, userId, 132, null, null, null);
            AuditEventService.record(conn, userId, "PATIENT", "DEMO_ALERT_CREATED", "USER", String.valueOf(userId), "SUCCESS", "mobile emergency help demo", request.getRemoteAddr());
            writeJson(response, "{\"success\":true,\"message\":\"Demo emergency alert recorded for hospital/staff review. This is not real emergency dispatch.\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(response, "{\"success\":false,\"message\":\"Unable to create demo emergency alert.\"}");
        }
    }

    private String buildLatestAlertJson(Connection conn, int userId) throws Exception {
        try {
            return buildLatestAlertJson(conn, userId, true);
        } catch (Exception e) {
            return buildLatestAlertJson(conn, userId, false);
        }
    }

    private String buildLatestAlertJson(Connection conn, int userId, boolean includeLifecycleStatus) throws Exception {
        String sql = "SELECT ea.alert_id, ea.bpm, ea.alert_status, ea.countdown_seconds, ea.created_at, "
                + (includeLifecycleStatus
                ? "COALESCE(ea.lifecycle_status, 'CREATED') AS lifecycle_status, "
                : "'CREATED' AS lifecycle_status, ")
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
                        ? "SmartHealth demo alert recorded. Staff should review this patient now. This is not real emergency dispatch."
                        : "SmartHealth demo alert assigned to " + hospitalName + ". This is not real emergency dispatch.";

                return "{"
                        + "\"success\":true,"
                        + "\"hasAlert\":true,"
                        + "\"alert\":{"
                        + "\"id\":" + rs.getInt("alert_id") + ","
                        + "\"status\":" + JsonUtil.quote(status) + ","
                        + "\"lifecycleStatus\":" + JsonUtil.quote(rs.getString("lifecycle_status")) + ","
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
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return null;
        }
        return Integer.valueOf(String.valueOf(session.getAttribute("userId")));
    }

    private void writeJson(HttpServletResponse response, String json) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.write(json);
        }
    }
}
