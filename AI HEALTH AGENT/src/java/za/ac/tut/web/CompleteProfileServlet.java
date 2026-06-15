package za.ac.tut.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import za.ac.tut.util.Database;
import za.ac.tut.util.PatientValidation;

public class CompleteProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if ("true".equals(request.getParameter("skip"))) {
            response.sendRedirect("healthApp.html");
            return;
        }

        Integer userId = getSessionUserId(request);
        if (userId == null) {
            response.sendRedirect("user_sign.html");
            return;
        }

        loadProfile(request, userId);
        request.getRequestDispatcher("complete_profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer userId = getSessionUserId(request);
        if (userId == null) {
            response.sendRedirect("user_sign.html");
            return;
        }

        String cellNumber = request.getParameter("cell_number");
        String emergencyNumber = request.getParameter("emergency_contact_number");
        String idNumber = request.getParameter("id_number");

        if (!isBlank(cellNumber) && !PatientValidation.isValidSouthAfricanPhone(cellNumber)) {
            reject(request, response, userId, "Please enter a valid South African personal cell number.");
            return;
        }

        if (!isBlank(emergencyNumber) && !PatientValidation.isValidSouthAfricanPhone(emergencyNumber)) {
            reject(request, response, userId, "Please enter a valid South African emergency contact number.");
            return;
        }

        if (PatientValidation.samePhone(cellNumber, emergencyNumber)) {
            reject(request, response, userId, "Your personal number and emergency contact number must not be the same.");
            return;
        }

        if (!isBlank(idNumber) && !PatientValidation.isValidIdNumber(idNumber)) {
            reject(request, response, userId, "Please enter a valid 13 digit South African ID number with a real birth date.");
            return;
        }

        Date dob = PatientValidation.parseDateOfBirth(request.getParameter("dob"));
        if (!isBlank(request.getParameter("dob")) && dob == null) {
            reject(request, response, userId, "Date of birth must be a real past date.");
            return;
        }
        if (!isBlank(idNumber) && !isBlank(request.getParameter("dob"))
                && !PatientValidation.idNumberMatchesDateOfBirth(idNumber, request.getParameter("dob"))) {
            reject(request, response, userId, "South African ID number must match the selected date of birth.");
            return;
        }

        String sql = "UPDATE users SET dob=?, gender=?, marital_status=?, cell_number=?, id_number=?, "
                + "emergency_contact_name=?, emergency_contact_number=?, blood_group=?, known_allergies=?, "
                + "chronic_conditions=?, address=? WHERE id=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, dob);
            ps.setString(2, trimToNull(request.getParameter("gender")));
            ps.setString(3, trimToNull(request.getParameter("marital_status")));
            ps.setString(4, PatientValidation.normalizePhone(cellNumber));
            ps.setString(5, trimToNull(idNumber));
            ps.setString(6, trimToNull(request.getParameter("emergency_contact_name")));
            ps.setString(7, PatientValidation.normalizePhone(emergencyNumber));
            ps.setString(8, trimToNull(request.getParameter("blood_group")));
            ps.setString(9, trimToNull(request.getParameter("known_allergies")));
            ps.setString(10, trimToNull(request.getParameter("chronic_conditions")));
            ps.setString(11, trimToNull(request.getParameter("address")));
            ps.setInt(12, userId);
            ps.executeUpdate();

            response.sendRedirect("healthApp.html");
        } catch (Exception e) {
            throw new ServletException("Unable to save patient profile details.", e);
        }
    }

    static boolean isProfileIncomplete(ResultSet rs) throws java.sql.SQLException {
        return isBlank(rs.getString("dob"))
                || isBlank(rs.getString("gender"))
                || isBlank(rs.getString("cell_number"))
                || isBlank(rs.getString("id_number"))
                || isBlank(rs.getString("emergency_contact_name"))
                || isBlank(rs.getString("emergency_contact_number"))
                || isBlank(rs.getString("blood_group"))
                || isBlank(rs.getString("address"));
    }

    private void reject(HttpServletRequest request, HttpServletResponse response, int userId, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        loadProfile(request, userId);
        request.getRequestDispatcher("complete_profile.jsp").forward(request, response);
    }

    private void loadProfile(HttpServletRequest request, int userId) throws ServletException {
        String sql = "SELECT title, first_name, surname, email, dob, gender, marital_status, cell_number, "
                + "id_number, emergency_contact_name, emergency_contact_number, blood_group, known_allergies, "
                + "chronic_conditions, address, is_verified FROM users WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ServletException("Signed-in user was not found.");
                }

                request.setAttribute("title", rs.getString("title"));
                request.setAttribute("first_name", rs.getString("first_name"));
                request.setAttribute("surname", rs.getString("surname"));
                request.setAttribute("email", rs.getString("email"));
                request.setAttribute("dob", rs.getDate("dob"));
                request.setAttribute("gender", rs.getString("gender"));
                request.setAttribute("marital_status", rs.getString("marital_status"));
                request.setAttribute("cell_number", rs.getString("cell_number"));
                request.setAttribute("id_number", rs.getString("id_number"));
                request.setAttribute("emergency_contact_name", rs.getString("emergency_contact_name"));
                request.setAttribute("emergency_contact_number", rs.getString("emergency_contact_number"));
                request.setAttribute("blood_group", rs.getString("blood_group"));
                request.setAttribute("known_allergies", rs.getString("known_allergies"));
                request.setAttribute("chronic_conditions", rs.getString("chronic_conditions"));
                request.setAttribute("address", rs.getString("address"));
                request.setAttribute("is_verified", rs.getBoolean("is_verified"));
            }
        } catch (Exception e) {
            throw new ServletException("Unable to load patient profile details.", e);
        }
    }

    private Integer getSessionUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return null;
        }

        return Integer.valueOf(String.valueOf(session.getAttribute("userId")));
    }

    private static boolean isBlank(Object value) {
        return value == null || value.toString().trim().isEmpty();
    }

    private String trimToNull(String value) {
        return PatientValidation.trimToNull(value);
    }
}
