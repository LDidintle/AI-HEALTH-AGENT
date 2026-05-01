package za.ac.tut.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import za.ac.tut.model.PasswordUtils;
import za.ac.tut.util.Database;
import za.ac.tut.util.JsonUtil;
import za.ac.tut.util.PatientValidation;

public class MobileRegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        String title = valueOrDefault(trimToNull(request.getParameter("title")), "Patient");
        String firstName = trimToNull(request.getParameter("firstName"));
        String surname = trimToNull(request.getParameter("surname"));
        String dobValue = trimToNull(request.getParameter("dob"));
        String gender = valueOrDefault(trimToNull(request.getParameter("gender")), "Not specified");
        String maritalStatus = valueOrDefault(trimToNull(request.getParameter("maritalStatus")), "Not specified");
        String email = trimToNull(request.getParameter("email"));
        String cellNumber = valueOrDefault(trimToNull(request.getParameter("cellNumber")), "");
        String idNumber = trimToNull(request.getParameter("idNumber"));
        String emergencyContactName = trimToNull(request.getParameter("emergencyContactName"));
        String emergencyContactNumber = trimToNull(request.getParameter("emergencyContactNumber"));
        String bloodGroup = trimToNull(request.getParameter("bloodGroup"));
        String knownAllergies = trimToNull(request.getParameter("knownAllergies"));
        String chronicConditions = trimToNull(request.getParameter("chronicConditions"));
        String address = valueOrDefault(trimToNull(request.getParameter("address")), "");
        String password = trimToNull(request.getParameter("password"));

        if (firstName == null || surname == null || dobValue == null || email == null || password == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"firstName, surname, dob, email and password are required.\"}");
            return;
        }

        if (!cellNumber.isEmpty() && !PatientValidation.isValidSouthAfricanPhone(cellNumber)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"Personal cell number is invalid.\"}");
            return;
        }

        if (idNumber != null && !PatientValidation.isValidIdNumber(idNumber)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"ID number must be 13 digits.\"}");
            return;
        }

        if (emergencyContactNumber != null && !PatientValidation.isValidSouthAfricanPhone(emergencyContactNumber)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"Emergency contact number is invalid.\"}");
            return;
        }

        if (PatientValidation.samePhone(cellNumber, emergencyContactNumber)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"Personal number and emergency contact number must not be the same.\"}");
            return;
        }

        if (password.length() < 6) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"Password must be at least 6 characters.\"}");
            return;
        }

        Date dob;
        try {
            dob = parseDate(dobValue);
        } catch (ParseException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"Date of birth must use YYYY-MM-DD format.\"}");
            return;
        }

        try {
            try (Connection conn = Database.getConnection()) {

                if (emailExists(conn, email)) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    writeJson(response, "{\"success\":false,\"message\":\"An account with this email already exists.\"}");
                    return;
                }

                conn.setAutoCommit(false);

                String userSql = "INSERT INTO users "
                        + "(title, first_name, surname, dob, gender, marital_status, email, cell_number, "
                        + "id_number, emergency_contact_name, emergency_contact_number, blood_group, "
                        + "known_allergies, chronic_conditions, address) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                String authSql = "INSERT INTO user_auth (user_id, password_hash) VALUES (?, ?)";

                try (
                        PreparedStatement userStatement = conn.prepareStatement(userSql, PreparedStatement.RETURN_GENERATED_KEYS);
                        PreparedStatement authStatement = conn.prepareStatement(authSql)) {

                    userStatement.setString(1, title);
                    userStatement.setString(2, firstName);
                    userStatement.setString(3, surname);
                    userStatement.setDate(4, dob);
                    userStatement.setString(5, gender);
                    userStatement.setString(6, maritalStatus);
                    userStatement.setString(7, email);
                    userStatement.setString(8, PatientValidation.normalizePhone(cellNumber));
                    userStatement.setString(9, idNumber);
                    userStatement.setString(10, emergencyContactName);
                    userStatement.setString(11, PatientValidation.normalizePhone(emergencyContactNumber));
                    userStatement.setString(12, bloodGroup);
                    userStatement.setString(13, knownAllergies);
                    userStatement.setString(14, chronicConditions);
                    userStatement.setString(15, address);

                    userStatement.executeUpdate();

                    int userId;
                    try (ResultSet generatedKeys = userStatement.getGeneratedKeys()) {
                        if (!generatedKeys.next()) {
                            throw new IllegalStateException("User ID was not generated.");
                        }
                        userId = generatedKeys.getInt(1);
                    }

                    authStatement.setInt(1, userId);
                    authStatement.setString(2, PasswordUtils.hashPassword(password));
                    authStatement.executeUpdate();

                    conn.commit();

                    String json = "{"
                            + "\"success\":true,"
                            + "\"message\":\"Account created. You can now sign in.\","
                            + "\"user\":{"
                            + "\"id\":" + userId + ","
                            + "\"email\":" + JsonUtil.quote(email) + ","
                            + "\"fullName\":" + JsonUtil.quote(firstName + " " + surname)
                            + "}"
                            + "}";
                    writeJson(response, json);
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            writeJson(response, "{\"success\":false,\"message\":\"An account with this email already exists.\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(response, "{\"success\":false,\"message\":\"Unable to create account right now.\"}");
        }
    }

    private boolean emailExists(Connection conn, String email) throws Exception {
        String sql = "SELECT id FROM users WHERE email = ? LIMIT 1";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private Date parseDate(String value) throws ParseException {
        String pattern = value.contains("/") ? "dd/MM/yyyy" : "yyyy-MM-dd";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setLenient(false);
        return new Date(format.parse(value).getTime());
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

    private void writeJson(HttpServletResponse response, String json) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.write(json);
        }
    }
}
