package za.ac.tut.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import za.ac.tut.model.HospitalAlertPatientRow;
import za.ac.tut.model.User;
import za.ac.tut.util.Database;
import za.ac.tut.util.HospitalAlertStatusService;
import za.ac.tut.util.PatientMapper;
import za.ac.tut.util.PatientSummaryService;

public class HospitalPatientsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!hasHospitalSession(request)) {
            response.sendRedirect("hospital_sign.jsp");
            return;
        }

        List<HospitalAlertPatientRow> patients = new ArrayList<>();
        HttpSession session = request.getSession(false);
        Integer hospitalId = session == null ? null : (Integer) session.getAttribute("hospitalId");
        boolean legacyHospital = session != null && "true".equals(String.valueOf(session.getAttribute("hospitalLegacy")));

        String sql = legacyHospital
                ? "SELECT * FROM users ORDER BY surname, first_name"
                : "SELECT DISTINCT u.* FROM users u "
                    + "JOIN emergency_alerts ea ON ea.user_id = u.id "
                    + "JOIN hospital_alert_assignments haa ON haa.alert_id = ea.alert_id "
                    + "WHERE haa.hospital_id = ? "
                    + "AND COALESCE(haa.status, 'ASSIGNED') <> '" + HospitalAlertStatusService.REMOVED + "' "
                    + "ORDER BY u.surname, u.first_name";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (!legacyHospital) {
                ps.setInt(1, hospitalId);
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    User user = PatientMapper.fromResultSet(rs);
                    HospitalAlertPatientRow row = new HospitalAlertPatientRow();
                    row.setUser(user);
                    row.setSummary(PatientSummaryService.loadSummary(conn, user.getId()));
                    if (!legacyHospital) {
                        loadLatestAlert(conn, hospitalId, row);
                    }
                    patients.add(row);
                }
            }

            request.setAttribute("patients", patients);
            request.setAttribute("hospitalName", session.getAttribute("hospitalName"));
            request.setAttribute("hospitalServiceArea", session.getAttribute("hospitalServiceArea"));
            request.getRequestDispatcher("hospital_patients.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Unable to load hospital patient list.", e);
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

    private void loadLatestAlert(Connection conn, int hospitalId, HospitalAlertPatientRow row) throws Exception {
        String sql = "SELECT ea.alert_id, ea.alert_status, ea.created_at, haa.status AS assignment_status "
                + "FROM emergency_alerts ea "
                + "JOIN hospital_alert_assignments haa ON haa.alert_id = ea.alert_id "
                + "WHERE haa.hospital_id = ? AND ea.user_id = ? "
                + "AND COALESCE(haa.status, 'ASSIGNED') <> ? "
                + "ORDER BY ea.created_at DESC, ea.alert_id DESC LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hospitalId);
            ps.setInt(2, row.getUser().getId());
            ps.setString(3, HospitalAlertStatusService.REMOVED);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    row.setLatestAlertId(rs.getInt("alert_id"));
                    row.setLatestAlertStatus(rs.getString("alert_status"));
                    row.setAssignmentStatus(rs.getString("assignment_status"));
                    row.setLatestAlertCreatedAt(rs.getTimestamp("created_at"));
                }
            }
        }
    }
}
