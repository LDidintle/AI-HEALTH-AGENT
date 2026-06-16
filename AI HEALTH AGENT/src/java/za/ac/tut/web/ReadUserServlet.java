package za.ac.tut.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import za.ac.tut.model.ClinicalNote;
import za.ac.tut.model.User;
import za.ac.tut.util.ClinicalNoteService;
import za.ac.tut.util.Database;
import za.ac.tut.util.PatientMapper;
import za.ac.tut.util.PatientSummaryService;

public class ReadUserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = parseId(request.getParameter("id"));
        if (id > 0) {
            try {
                if (showPatientDetails(request, response, id)) {
                    return;
                }
            } catch (Exception e) {
                throw new ServletException("Unable to read user details.", e);
            }
            request.setAttribute("errorMessage", "No patient was found with those details.");
        }
        request.getRequestDispatcher("read_user.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String patientId = request.getParameter("patient_id");

        if ((email == null || email.trim().isEmpty()) && (patientId == null || patientId.trim().isEmpty())) {
            request.setAttribute("errorMessage", "Please enter a patient ID or email address.");
            request.getRequestDispatcher("read_user.jsp").forward(request, response);
            return;
        }

        try {
            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE email = ? OR id = ?")) {

                ps.setString(1, email == null ? "" : email.trim());
                ps.setInt(2, parseId(patientId));

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        response.sendRedirect("ReadUserServlet.do?id=" + rs.getInt("id"));
                        return;
                    }
                }
            }

            request.setAttribute("errorMessage", "No patient was found with those details.");
            request.setAttribute("searchedEmail", email == null ? "" : email.trim());
            request.setAttribute("searchedId", patientId == null ? "" : patientId.trim());
            request.getRequestDispatcher("read_user.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Unable to read user details.", e);
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

    private boolean showPatientDetails(HttpServletRequest request, HttpServletResponse response, int userId)
            throws Exception {
        String sql = "SELECT id, title, first_name, surname, dob, gender, marital_status, "
                + "email, cell_number, id_number, emergency_contact_name, emergency_contact_number, "
                + "blood_group, known_allergies, chronic_conditions, address, is_verified FROM users "
                + "WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                User user = PatientMapper.fromResultSet(rs);
                ClinicalNote clinicalNote = ClinicalNoteService.load(conn, userId);
                request.setAttribute("user", user);
                request.setAttribute("summary", PatientSummaryService.loadSummary(conn, userId));
                request.setAttribute("clinicalNote", clinicalNote);
                request.getRequestDispatcher("read_user_result.jsp").forward(request, response);
                return true;
            }
        }
    }
}
