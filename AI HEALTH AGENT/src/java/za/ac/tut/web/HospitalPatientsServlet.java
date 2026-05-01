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
import za.ac.tut.model.PatientSummaryRow;
import za.ac.tut.model.User;
import za.ac.tut.util.Database;
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

        List<PatientSummaryRow> patients = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM users ORDER BY surname, first_name");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User user = PatientMapper.fromResultSet(rs);
                PatientSummaryRow row = new PatientSummaryRow();
                row.setUser(user);
                row.setSummary(PatientSummaryService.loadSummary(conn, user.getId()));
                patients.add(row);
            }

            request.setAttribute("patients", patients);
            request.getRequestDispatcher("hospital_patients.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Unable to load hospital patient list.", e);
        }
    }

    private boolean hasHospitalSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && "true".equals(String.valueOf(session.getAttribute("hospital")));
    }
}
