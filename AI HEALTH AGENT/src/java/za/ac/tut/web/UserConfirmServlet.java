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
import za.ac.tut.util.Database;

public class UserConfirmServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("username");
        String password = request.getParameter("password");

        String hashedEnteredPassword = PasswordUtils.hashPassword(password);

        try {
            try (Connection conn = Database.getConnection()) {

                String sql = "SELECT user_id, password_hash FROM user_auth WHERE user_id = " +
                             "(SELECT id FROM users WHERE email = ?)";

                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, email);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String dbPasswordHash = rs.getString("password_hash").trim(); // Trim spaces

                    if (hashedEnteredPassword.equals(dbPasswordHash)) {
                        // Password correct
                        HttpSession session = request.getSession();
                        session.setAttribute("user", email);
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
            request.setAttribute("error", "Database error: " + e.getMessage());
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }
}
