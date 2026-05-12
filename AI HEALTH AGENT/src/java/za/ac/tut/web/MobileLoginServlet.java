package za.ac.tut.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import za.ac.tut.model.PasswordUtils;
import za.ac.tut.util.AuthUtil;
import za.ac.tut.util.Database;
import za.ac.tut.util.JsonUtil;

public class MobileLoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        String email = trimToNull(request.getParameter("email"));
        String password = trimToNull(request.getParameter("password"));

        if (email == null || password == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"email and password are required.\"}");
            return;
        }

        try {
            try (Connection conn = Database.getConnection()) {

                String sql = "SELECT u.id, u.first_name, u.surname, u.email, u.is_verified, ua.password_hash "
                        + "FROM users u JOIN user_auth ua ON u.id = ua.user_id "
                        + "WHERE u.email = ?";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, email);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            writeJson(response, "{\"success\":false,\"message\":\"Invalid email or password.\"}");
                            return;
                        }

                        String storedHash = rs.getString("password_hash");

                        if (!PasswordUtils.verifyPassword(password, storedHash)) {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            writeJson(response, "{\"success\":false,\"message\":\"Invalid email or password.\"}");
                            return;
                        }

                        HttpSession session = request.getSession(true);
                        AuthUtil.markPatient(session, rs.getString("email"), rs.getInt("id"));

                        String fullName = rs.getString("first_name") + " " + rs.getString("surname");
                        String json = "{"
                                + "\"success\":true,"
                                + "\"message\":\"Login successful.\","
                                + "\"user\":{"
                                + "\"id\":" + rs.getInt("id") + ","
                                + "\"email\":" + JsonUtil.quote(rs.getString("email")) + ","
                                + "\"fullName\":" + JsonUtil.quote(fullName) + ","
                                + "\"isVerified\":" + rs.getBoolean("is_verified")
                                + "}"
                                + "}";

                        writeJson(response, json);
                    }
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(response, "{\"success\":false,\"message\":\"Unable to complete login.\"}");
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
