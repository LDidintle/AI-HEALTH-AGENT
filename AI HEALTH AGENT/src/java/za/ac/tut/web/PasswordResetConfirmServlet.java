package za.ac.tut.web;

import java.io.IOException;
import java.sql.Connection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import za.ac.tut.model.PasswordUtils;
import za.ac.tut.util.Database;
import za.ac.tut.util.PasswordResetService;

public class PasswordResetConfirmServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = trimToNull(request.getParameter("email"));
        String otp = trimToNull(request.getParameter("otp"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        request.setAttribute("email", email == null ? "" : email);
        request.setAttribute("otpRequested", "true");

        if (email == null || otp == null) {
            reject(request, response, "Email and OTP are required.");
            return;
        }

        if (!otp.matches("[0-9]{6}")) {
            reject(request, response, "OTP must be the 6 digit code sent to your email.");
            return;
        }

        if (password == null || !password.equals(confirmPassword)) {
            reject(request, response, "Passwords do not match.");
            return;
        }

        if (!isStrongPassword(password)) {
            reject(request, response, "Password must be 8+ characters with 2 numbers, 1 uppercase letter, and 1 special character.");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            PasswordResetService.ensureSchema(conn);
            conn.setAutoCommit(false);
            try {
                PasswordResetService.ResetPasswordResult result = PasswordResetService.resetPassword(
                        conn,
                        email,
                        otp,
                        PasswordUtils.hashPassword(password)
                );
                conn.commit();

                if (result.isSuccess()) {
                    request.setAttribute("success", result.getMessage());
                    request.setAttribute("otpRequested", null);
                } else {
                    request.setAttribute("error", result.getMessage());
                }
                request.getRequestDispatcher("reset_password.jsp").forward(request, response);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new ServletException("Unable to reset password.", e);
        }
    }

    private void reject(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        request.getRequestDispatcher("reset_password.jsp").forward(request, response);
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

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
