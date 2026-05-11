package za.ac.tut.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLIntegrityConstraintViolationException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import za.ac.tut.model.PasswordUtils;
import za.ac.tut.util.Database;

public class HospitalRegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = trimToNull(request.getParameter("name"));
        String email = trimToNull(request.getParameter("email"));
        String phone = trimToNull(request.getParameter("phone"));
        String serviceArea = trimToNull(request.getParameter("serviceArea"));
        String address = trimToNull(request.getParameter("address"));
        String password = trimToNull(request.getParameter("password"));

        if (name == null || email == null || serviceArea == null || password == null) {
            reject(request, response, "Hospital name, email, service area, and password are required.");
            return;
        }

        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            reject(request, response, "Please enter a valid hospital email address.");
            return;
        }

        if (password.length() < 8) {
            reject(request, response, "Hospital password must be at least 8 characters.");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            int hospitalId = insertHospital(conn, name, email, phone, serviceArea, address);
            insertHospitalAuth(conn, hospitalId, password);

            conn.commit();
            request.setAttribute("message", "Hospital registered successfully. You can sign in now.");
            request.getRequestDispatcher("hospital_sign.jsp").forward(request, response);
        } catch (SQLIntegrityConstraintViolationException e) {
            reject(request, response, "A hospital with that email is already registered.");
        } catch (Exception e) {
            throw new ServletException("Unable to register hospital.", e);
        }
    }

    private int insertHospital(Connection conn, String name, String email, String phone,
            String serviceArea, String address) throws Exception {
        String sql = "INSERT INTO hospitals (name, email, phone, service_area, address, active) "
                + "VALUES (?, ?, ?, ?, ?, TRUE)";

        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, serviceArea);
            ps.setString(5, address);
            ps.executeUpdate();

            try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new IllegalStateException("Hospital registration did not return an ID.");
    }

    private void insertHospitalAuth(Connection conn, int hospitalId, String password) throws Exception {
        String sql = "INSERT INTO hospital_auth (hospital_id, password_hash) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hospitalId);
            ps.setString(2, PasswordUtils.hashPassword(password));
            ps.executeUpdate();
        }
    }

    private void reject(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("registerError", message);
        request.getRequestDispatcher("hospital_sign.jsp").forward(request, response);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
