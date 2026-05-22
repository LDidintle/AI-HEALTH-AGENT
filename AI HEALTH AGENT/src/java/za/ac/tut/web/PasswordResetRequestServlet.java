package za.ac.tut.web;

import java.io.IOException;
import java.sql.Connection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import za.ac.tut.util.Database;
import za.ac.tut.util.MailService;
import za.ac.tut.util.PasswordResetService;
import za.ac.tut.util.RateLimitService;
import za.ac.tut.util.ResetOtpVisibility;

public class PasswordResetRequestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("reset_password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = trimToNull(request.getParameter("email"));
        if (!RateLimitService.allow(RateLimitService.key("password-reset", request.getRemoteAddr(), email),
                5, 15L * 60L * 1000L)) {
            request.setAttribute("error", "Too many password reset attempts. Try again later.");
            request.getRequestDispatcher("reset_password.jsp").forward(request, response);
            return;
        }

        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            request.setAttribute("error", "Please enter a valid account email address.");
            request.getRequestDispatcher("reset_password.jsp").forward(request, response);
            return;
        }

        try (Connection conn = Database.getConnection()) {
            PasswordResetService.ensureSchema(conn);
            PasswordResetService.ResetRequestResult result = PasswordResetService.createOtp(conn, email);

            request.setAttribute("email", email);
            request.setAttribute("otpRequested", "true");
            request.setAttribute("message", "If that email exists, an OTP has been sent. Enter it below to set a new password.");

            if (result.isUserFound()) {
                boolean sent = MailService.sendPasswordResetOtp(email, result.getOtp());
                if (!sent && ResetOtpVisibility.isDemoOtpVisible()) {
                    request.setAttribute("demoOtp", result.getOtp());
                }
            }

            request.getRequestDispatcher("reset_password.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Unable to request password reset OTP.", e);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
