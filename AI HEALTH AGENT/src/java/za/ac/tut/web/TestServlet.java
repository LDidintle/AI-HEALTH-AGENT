package za.ac.tut.web;

import java.io.IOException;
import java.sql.*;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import za.ac.tut.model.PasswordUtils;
import za.ac.tut.util.Database;

public class TestServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        HttpSession session = request.getSession(false);

        if (session == null) {
            response.getWriter().println("Session expired!");
            return;
        }

        try {
            String title = (String) session.getAttribute("title");
            String name = (String) session.getAttribute("name");
            String surname = (String) session.getAttribute("surname");
            String email = (String) session.getAttribute("email");

            if (isBlank(name) || isBlank(surname) || isBlank(email)) {
                response.sendRedirect("welcome.html");
                return;
            }

            String pass = request.getParameter("password");
            String confirmPass = request.getParameter("confirmPassword");
            if (pass == null || !pass.equals(confirmPass)) {
                request.setAttribute("error", "Passwords do not match.");
                request.getRequestDispatcher("password.jsp").forward(request, response);
                return;
            }

            if (!isStrongPassword(pass)) {
                request.setAttribute("error", "Password must be 8+ characters with 2 numbers, 1 uppercase letter, and 1 special character.");
                request.getRequestDispatcher("password.jsp").forward(request, response);
                return;
            }

            PasswordUtils pu = new PasswordUtils();
            String hashedPass = pu.hashPassword(pass);

       
            String sqlUser = "INSERT INTO users "
                    + "(title, first_name, surname, email, is_verified) "
                    + "VALUES (?, ?, ?, ?, FALSE)";
            String sqlAuth = "INSERT INTO user_auth (user_id, password_hash) VALUES (?, ?)";

            
            try (Connection conn = Database.getConnection()) {
                conn.setAutoCommit(false);

                try (
                    PreparedStatement psUser = conn.prepareStatement(sqlUser, PreparedStatement.RETURN_GENERATED_KEYS);
                    PreparedStatement psAuth = conn.prepareStatement(sqlAuth)
                ) {
                    psUser.setString(1, title);
                    psUser.setString(2, name);
                    psUser.setString(3, surname);
                    psUser.setString(4, email);

                    int rowsInserted = psUser.executeUpdate();
                    if (rowsInserted == 0) {
                        throw new SQLException("Failed to insert user.");
                    }

                    int userId;
                    try (ResultSet rs = psUser.getGeneratedKeys()) {
                        if (rs.next()) {
                            userId = rs.getInt(1);
                        } else {
                            throw new SQLException("Failed to get user ID.");
                        }
                    }

                    psAuth.setInt(1, userId);
                    psAuth.setString(2, hashedPass);
                    psAuth.executeUpdate();

                    conn.commit();

                    session.invalidate();
                    RequestDispatcher disp = request.getRequestDispatcher("account_created.jsp");
                    disp.forward(request, response);
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                }
                
            }

        } catch (SQLException e) {
            e.printStackTrace();
            if ("23505".equals(e.getSQLState())) {
                request.setAttribute("error", "An account with this email already exists.");
            } else if ("42703".equals(e.getSQLState())) {
                request.setAttribute("error", "The database needs the latest user verification migration before accounts can be created.");
            } else {
                request.setAttribute("error", "Unable to create the account right now. Please try again later.");
            }
            request.getRequestDispatcher("error.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Unable to create the account right now. Please try again later.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }

    private boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        int digits = 0;
        boolean hasUppercase = false;
        boolean hasSpecial = false;

        for (char character : password.toCharArray()) {
            if (Character.isDigit(character)) {
                digits++;
            } else if (Character.isUpperCase(character)) {
                hasUppercase = true;
            } else if (!Character.isLetterOrDigit(character)) {
                hasSpecial = true;
            }
        }

        return digits >= 2 && hasUppercase && hasSpecial;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
