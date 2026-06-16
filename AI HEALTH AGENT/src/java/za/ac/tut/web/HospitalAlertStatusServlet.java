package za.ac.tut.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import za.ac.tut.util.AuditEventService;
import za.ac.tut.util.AuthUtil;
import za.ac.tut.util.Database;
import za.ac.tut.util.HospitalAlertStatusService;

public class HospitalAlertStatusServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (!hasHospitalSession(session)) {
            response.sendRedirect("hospital_sign.jsp");
            return;
        }

        String status = HospitalAlertStatusService.statusForSubmission(
                request.getParameter("action"),
                request.getParameter("statusAction")
        );
        int alertId = parsePositiveInt(request.getParameter("alertId"));
        Integer hospitalId = (Integer) session.getAttribute("hospitalId");

        if (status == null || alertId <= 0 || hospitalId == null) {
            response.sendRedirect("HospitalPatientsServlet.do?status=invalid");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            int updated = updateAssignmentStatus(conn, alertId, hospitalId, status);
            AuditEventService.record(
                    conn,
                    null,
                    AuthUtil.ROLE_HOSPITAL,
                    "HOSPITAL_ALERT_STATUS_UPDATE",
                    "emergency_alert",
                    String.valueOf(alertId),
                    updated > 0 ? "SUCCESS" : "NOT_FOUND",
                    "hospitalId=" + hospitalId + " status=" + status,
                    request.getRemoteAddr()
            );
            response.sendRedirect("HospitalPatientsServlet.do?status=" + (updated > 0 ? "updated" : "not_found"));
        } catch (Exception e) {
            throw new ServletException("Unable to update hospital alert status.", e);
        }
    }

    private boolean hasHospitalSession(HttpSession session) {
        return session != null
                && "true".equals(String.valueOf(session.getAttribute("hospital")))
                && session.getAttribute("hospitalId") instanceof Integer;
    }

    private int parsePositiveInt(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    private int updateAssignmentStatus(Connection conn, int alertId, int hospitalId, String status) throws Exception {
        String sql = "UPDATE hospital_alert_assignments SET status = ? WHERE alert_id = ? AND hospital_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, alertId);
            ps.setInt(3, hospitalId);
            return ps.executeUpdate();
        }
    }
}
