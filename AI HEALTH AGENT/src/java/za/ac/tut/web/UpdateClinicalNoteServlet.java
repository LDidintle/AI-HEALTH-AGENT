package za.ac.tut.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import za.ac.tut.util.AuditEventService;
import za.ac.tut.util.AuthUtil;
import za.ac.tut.util.ClinicalNoteService;
import za.ac.tut.util.Database;
import za.ac.tut.util.HospitalAlertStatusService;

public class UpdateClinicalNoteServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        boolean admin = AuthUtil.isAdmin(session);
        boolean hospital = AuthUtil.isHospital(session);
        int userId = parseId(request.getParameter("userId"));

        if ((!admin && !hospital) || userId <= 0) {
            response.sendRedirect(admin ? "ReadUserServlet.do" : "HospitalPatientsServlet.do?note=invalid");
            return;
        }

        String noteText = request.getParameter("noteText");
        try (Connection conn = Database.getConnection()) {
            if (hospital && !hasHospitalAccess(conn, session, userId)) {
                response.sendRedirect("HospitalPatientsServlet.do?note=forbidden");
                return;
            }

            boolean saved = ClinicalNoteService.save(
                    conn,
                    userId,
                    noteText,
                    AuthUtil.currentRole(session),
                    actorReference(session)
            );
            AuditEventService.record(
                    conn,
                    null,
                    AuthUtil.currentRole(session),
                    "CLINICAL_NOTE_UPDATE",
                    "user",
                    String.valueOf(userId),
                    saved ? "SAVED" : "CLEARED",
                    "clinical note updated",
                    request.getRemoteAddr()
            );
            response.sendRedirect(returnPath(session, userId, saved ? "saved" : "cleared"));
        } catch (IllegalArgumentException e) {
            response.sendRedirect(returnPath(session, userId, "invalid"));
        } catch (Exception e) {
            throw new ServletException("Unable to save clinical note.", e);
        }
    }

    private String returnPath(HttpSession session, int userId, String status) {
        if (AuthUtil.isHospital(session)) {
            return "HospitalPatientDetailsServlet.do?id=" + userId + "&note=" + status;
        }
        return "ReadUserServlet.do?id=" + userId + "&note=" + status;
    }

    private boolean hasHospitalAccess(Connection conn, HttpSession session, int userId) throws Exception {
        if ("true".equals(String.valueOf(session.getAttribute("hospitalLegacy")))) {
            return true;
        }
        Integer hospitalId = session == null ? null : (Integer) session.getAttribute("hospitalId");
        if (hospitalId == null) {
            return false;
        }
        String sql = "SELECT 1 FROM emergency_alerts ea "
                + "JOIN hospital_alert_assignments haa ON haa.alert_id = ea.alert_id "
                + "WHERE haa.hospital_id = ? AND ea.user_id = ? "
                + "AND COALESCE(haa.status, 'ASSIGNED') <> ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hospitalId);
            ps.setInt(2, userId);
            ps.setString(3, HospitalAlertStatusService.REMOVED);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private String actorReference(HttpSession session) {
        if (session == null) {
            return null;
        }
        if (AuthUtil.isHospital(session)) {
            Object hospitalId = session.getAttribute("hospitalId");
            return hospitalId == null ? "legacy-hospital" : String.valueOf(hospitalId);
        }
        return "admin";
    }

    private int parseId(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : -1;
        } catch (Exception e) {
            return -1;
        }
    }
}
