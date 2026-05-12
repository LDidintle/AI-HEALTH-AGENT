package za.ac.tut.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import za.ac.tut.model.PasswordUtils;
import za.ac.tut.util.Database;
import za.ac.tut.util.JsonUtil;
import za.ac.tut.util.PatientAccountProcedureService;
import za.ac.tut.util.PatientValidation;

public class MobileRegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        String firstName = trimToNull(request.getParameter("firstName"));
        String surname = trimToNull(request.getParameter("surname"));
        String email = trimToNull(request.getParameter("email"));
        String dobText = trimToNull(request.getParameter("dob"));
        String password = trimToNull(request.getParameter("password"));

        if (firstName == null || surname == null || email == null || dobText == null || password == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"firstName, surname, email, dob and password are required.\"}");
            return;
        }

        Date dateOfBirth = PatientValidation.parseDateOfBirth(dobText);
        if (dateOfBirth == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"Date of birth must be a real past date.\"}");
            return;
        }

        if (password.length() < 6) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"Password must be at least 6 characters.\"}");
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

                try {
                    int userId = PatientAccountProcedureService.createPatientAccount(
                            conn, "Patient", firstName, surname, dateOfBirth, email,
                            PasswordUtils.hashPassword(password));

                    conn.commit();

                    String json = "{"
                            + "\"success\":true,"
                            + "\"message\":\"Account created. You can now sign in.\","
                            + "\"user\":{"
                            + "\"id\":" + userId + ","
                            + "\"email\":" + JsonUtil.quote(email) + ","
                            + "\"fullName\":" + JsonUtil.quote(firstName + " " + surname) + ","
                            + "\"isVerified\":false"
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
