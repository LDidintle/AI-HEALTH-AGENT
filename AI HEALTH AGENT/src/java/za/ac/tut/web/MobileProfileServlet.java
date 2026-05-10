package za.ac.tut.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
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
import za.ac.tut.util.JsonUtil;
import za.ac.tut.util.PatientValidation;

public class MobileProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(response, "{\"success\":false,\"message\":\"No active user session.\"}");
            return;
        }

        String email = String.valueOf(session.getAttribute("user"));

        try {
            try (Connection conn = Database.getConnection()) {

                String sql = "SELECT id, first_name, surname, title, dob, gender, marital_status, cell_number, "
                        + "id_number, emergency_contact_name, emergency_contact_number, blood_group, "
                        + "known_allergies, chronic_conditions, address, is_verified "
                        + "FROM users WHERE email = ?";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, email);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            writeJson(response, "{\"success\":false,\"message\":\"User was not found.\"}");
                            return;
                        }

                        String json = "{"
                                + "\"success\":true,"
                                + "\"user\":{"
                                + "\"id\":" + rs.getInt("id") + ","
                                + "\"email\":" + JsonUtil.quote(email) + ","
                                + "\"title\":" + JsonUtil.quote(rs.getString("title")) + ","
                                + "\"firstName\":" + JsonUtil.quote(rs.getString("first_name")) + ","
                                + "\"surname\":" + JsonUtil.quote(rs.getString("surname")) + ","
                                + "\"dob\":" + JsonUtil.quote(dateToString(rs.getDate("dob"))) + ","
                                + "\"gender\":" + JsonUtil.quote(rs.getString("gender")) + ","
                                + "\"maritalStatus\":" + JsonUtil.quote(rs.getString("marital_status")) + ","
                                + "\"cellNumber\":" + JsonUtil.quote(rs.getString("cell_number")) + ","
                                + "\"idNumber\":" + JsonUtil.quote(rs.getString("id_number")) + ","
                                + "\"emergencyContactName\":" + JsonUtil.quote(rs.getString("emergency_contact_name")) + ","
                                + "\"emergencyContactNumber\":" + JsonUtil.quote(rs.getString("emergency_contact_number")) + ","
                                + "\"bloodGroup\":" + JsonUtil.quote(rs.getString("blood_group")) + ","
                                + "\"knownAllergies\":" + JsonUtil.quote(rs.getString("known_allergies")) + ","
                                + "\"chronicConditions\":" + JsonUtil.quote(rs.getString("chronic_conditions")) + ","
                                + "\"address\":" + JsonUtil.quote(rs.getString("address")) + ","
                                + "\"isVerified\":" + rs.getBoolean("is_verified")
                                + "}"
                                + "}";

                        writeJson(response, json);
                    }
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(response, "{\"success\":false,\"message\":\"Unable to load profile.\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(response, "{\"success\":false,\"message\":\"No active user session.\"}");
            return;
        }

        String title = valueOrDefault(trimToNull(request.getParameter("title")), "Patient");
        String firstName = trimToNull(request.getParameter("firstName"));
        String surname = trimToNull(request.getParameter("surname"));
        String dobText = trimToNull(request.getParameter("dob"));
        String gender = valueOrDefault(trimToNull(request.getParameter("gender")), "Not specified");
        String maritalStatus = valueOrDefault(trimToNull(request.getParameter("maritalStatus")), "");
        String cellNumber = trimToNull(request.getParameter("cellNumber"));
        String idNumber = trimToNull(request.getParameter("idNumber"));
        String emergencyContactName = trimToNull(request.getParameter("emergencyContactName"));
        String emergencyContactNumber = trimToNull(request.getParameter("emergencyContactNumber"));
        String bloodGroup = valueOrDefault(trimToNull(request.getParameter("bloodGroup")), "");
        String knownAllergies = trimToNull(request.getParameter("knownAllergies"));
        String chronicConditions = trimToNull(request.getParameter("chronicConditions"));
        String address = trimToNull(request.getParameter("address"));

        if (firstName == null || surname == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"firstName and surname are required.\"}");
            return;
        }

        Date dob = parseDob(dobText);
        if (dobText != null && dob == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"Date of birth must be a valid past date.\"}");
            return;
        }

        if (cellNumber != null && !PatientValidation.isValidSouthAfricanPhone(cellNumber)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"Please enter a valid South African personal cell number.\"}");
            return;
        }

        if (emergencyContactNumber != null && !PatientValidation.isValidSouthAfricanPhone(emergencyContactNumber)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"Please enter a valid South African emergency contact number.\"}");
            return;
        }

        if (PatientValidation.samePhone(cellNumber, emergencyContactNumber)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"Your personal number and emergency contact number must not be the same.\"}");
            return;
        }

        if (idNumber != null && !PatientValidation.isValidIdNumber(idNumber)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"South African ID number must be 13 digits.\"}");
            return;
        }

        try {
            try (Connection conn = Database.getConnection()) {
                String sql = "UPDATE users SET title = ?, first_name = ?, surname = ?, dob = ?, gender = ?, "
                        + "marital_status = ?, cell_number = ?, id_number = ?, emergency_contact_name = ?, "
                        + "emergency_contact_number = ?, blood_group = ?, known_allergies = ?, "
                        + "chronic_conditions = ?, address = ? WHERE id = ?";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, title);
                    ps.setString(2, firstName);
                    ps.setString(3, surname);
                    ps.setDate(4, dob);
                    ps.setString(5, gender);
                    ps.setString(6, maritalStatus);
                    ps.setString(7, PatientValidation.normalizePhone(cellNumber));
                    ps.setString(8, idNumber);
                    ps.setString(9, emergencyContactName);
                    ps.setString(10, PatientValidation.normalizePhone(emergencyContactNumber));
                    ps.setString(11, bloodGroup);
                    ps.setString(12, knownAllergies);
                    ps.setString(13, chronicConditions);
                    ps.setString(14, address);
                    ps.setInt(15, Integer.parseInt(String.valueOf(session.getAttribute("userId"))));
                    ps.executeUpdate();
                }

                writeJson(response, "{\"success\":true,\"message\":\"Profile updated.\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(response, "{\"success\":false,\"message\":\"Unable to update profile.\"}");
        }
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null ? fallback : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String dateToString(Date date) {
        return date == null ? null : date.toString();
    }

    private Date parseDob(String value) {
        if (value == null) {
            return null;
        }

        try {
            LocalDate dob = LocalDate.parse(value);
            if (dob.isAfter(LocalDate.now())) {
                return null;
            }
            return Date.valueOf(dob);
        } catch (Exception e) {
            return null;
        }
    }

    private void writeJson(HttpServletResponse response, String json) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.write(json);
        }
    }
}
