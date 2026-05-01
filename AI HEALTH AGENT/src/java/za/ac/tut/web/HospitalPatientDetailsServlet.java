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
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
        return session != null && "true".equals(String.valueOf(session.getAttribute("hospital")));
    }
}
