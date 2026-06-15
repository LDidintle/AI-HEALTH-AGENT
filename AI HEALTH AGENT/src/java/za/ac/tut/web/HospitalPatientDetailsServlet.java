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
import za.ac.tut.model.User;
import za.ac.tut.util.Database;
import za.ac.tut.util.HospitalAlertStatusService;
import za.ac.tut.util.PatientMapper;
import za.ac.tut.util.PatientSummaryService;

public class HospitalPatientDetailsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!hasHospitalSession(request)) {
            response.sendRedirect("hospital_sign.jsp");
            return;
        }

        int id = parseId(request.getParameter("id"));
        if (id <= 0) {
            response.sendRedirect("HospitalPatientsServlet.do");
            return;
        }

        String sql = "SELECT * FROM users WHERE id = ?";
        HttpSession session = request.getSession(false);
        Integer hospitalId = session == null ? null : (Integer) session.getAttribute("hospitalId");
        boolean legacyHospital = session != null && "true".equals(String.valueOf(session.getAttribute("hospitalLegacy")));

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (!legacyHospital && !hasAssignedAlert(conn, hospitalId, id)) {
                response.sendRedirect("HospitalPatientsServlet.do");
                return;
            }

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = PatientMapper.fromResultSet(rs);
                    request.setAttribute("user", user);
                    request.setAttribute("summary", PatientSummaryService.loadSummary(conn, user.getId()));
                    request.setAttribute("readonlyPortal", "true");
                    request.getRequestDispatcher("read_user_result.jsp").forward(request, response);
                    return;
                }
            }

            response.sendRedirect("HospitalPatientsServlet.do");
        } catch (Exception e) {
            throw new ServletException("Unable to load hospital patient details.", e);
        }
    }

    private int parseId(String value) {
        if (value == null || value.trim().isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private boolean hasHospitalSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !"true".equals(String.valueOf(session.getAttribute("hospital")))) {
            return false;
        }
        return "true".equals(String.valueOf(session.getAttribute("hospitalLegacy")))
                || session.getAttribute("hospitalId") instanceof Integer;
    }

    private boolean hasAssignedAlert(Connection conn, Integer hospitalId, int userId) throws Exception {
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
}
