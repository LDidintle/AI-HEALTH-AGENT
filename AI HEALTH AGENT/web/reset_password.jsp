<%@page import="za.ac.tut.util.CsrfUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%!
    private String value(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>
<%
    boolean otpRequested = "true".equals(String.valueOf(request.getAttribute("otpRequested")));
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reset Password</title>
    <link rel="stylesheet" href="auth.css">
</head>
<body>
<main class="form-container">
    <h2>Reset Password</h2>
    <p class="reset-text">Enter your account email, then use the OTP to set a new password.</p>

    <% if (request.getAttribute("error") != null) { %>
        <p class="message error"><%= value(request.getAttribute("error")) %></p>
    <% } %>
    <% if (request.getAttribute("message") != null) { %>
        <p class="message success"><%= value(request.getAttribute("message")) %></p>
    <% } %>
    <% if (request.getAttribute("success") != null) { %>
        <p class="message success"><%= value(request.getAttribute("success")) %></p>
    <% } %>
    <% if (request.getAttribute("demoOtp") != null) { %>
        <p class="message info">Demo OTP: <strong><%= value(request.getAttribute("demoOtp")) %></strong></p>
    <% } %>

    <form action="PasswordResetRequestServlet.do" method="post">
        <input type="hidden" name="<%= CsrfUtil.PARAMETER %>" value="<%= CsrfUtil.token(request) %>">

        <label for="email">Account Email</label>
        <input type="email" id="email" name="email" value="<%= value(request.getAttribute("email")) %>" required>
        <button type="submit">Send OTP</button>
    </form>

    <% if (otpRequested) { %>
        <form action="PasswordResetConfirmServlet.do" method="post" onsubmit="return validateResetPassword()">
            <input type="hidden" name="<%= CsrfUtil.PARAMETER %>" value="<%= CsrfUtil.token(request) %>">
            <input type="hidden" name="email" value="<%= value(request.getAttribute("email")) %>">

            <label for="otp">OTP Code</label>
            <input type="text" id="otp" name="otp" inputmode="numeric" maxlength="6" pattern="[0-9]{6}" required>

            <label for="password">New Password</label>
            <input type="password" id="password" name="password" required>

            <label for="confirmPassword">Confirm New Password</label>
            <input type="password" id="confirmPassword" name="confirmPassword" required>

            <p id="resetPasswordError" class="message error" style="display:none;"></p>
            <button type="submit">Change Password</button>
        </form>
    <% } %>

    <a class="secondary-link" href="user_sign.html">Back to Login</a>
</main>
<script src="reset_password.js"></script>
</body>
</html>
