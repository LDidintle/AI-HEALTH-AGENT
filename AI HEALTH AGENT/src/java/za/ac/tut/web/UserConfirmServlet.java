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
import za.ac.tut.model.PasswordUtils;
import za.ac.tut.util.AuthUtil;
import za.ac.tut.util.Database;

public class UserConfirmServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = trimToNull(request.getParameter("username"));
        String password = trimToNull(request.getParameter("password"));

        if (email == null || password == null) {
            request.setAttribute("usernameError", "Email and password are required.");
            request.getRequestDispatcher("error_user.jsp").forward(request, response);
            return;
        }

        try {
            try (Connection conn = Database.getConnection()) {

                String sql = "SELECT u.id, u.email, u.is_verified, u.dob, u.gender, u.cell_number, "
                        + "u.id_number, u.emergency_contact_name, u.emergency_contact_number, "
                        + "u.blood_group, u.address, ua.password_hash "
                        + "FROM users u JOIN user_auth ua ON u.id = ua.user_id "
                        + "WHERE u.email = ?";

                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, email);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String dbPasswordHash = rs.getString("password_hash");

                    if (PasswordUtils.verifyPassword(password, dbPasswordHash)) {
                        // Password correct
                        HttpSession session = request.getSession();
                        AuthUtil.markPatient(session, rs.getString("email"), rs.getInt("id"));

                        if (rs.getBoolean("is_verified") && CompleteProfileServlet.isProfileIncomplete(rs)) {
                            response.sendRedirect("CompleteProfileServlet.do");
                            return;
                        }

                        response.sendRedirect("healthApp.html");
                    } else {
                        // Password wrong
                        request.setAttribute("passwordError", "Incorrect password!");
                        request.getRequestDispatcher("error_user.jsp").forward(request, response);
                    }

                } else {
                    // Username not found
                    request.setAttribute("usernameError", "Username not found!");
                    request.getRequestDispatcher("error_user.jsp").forward(request, response);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Unable to sign in right now. Please try again later.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
