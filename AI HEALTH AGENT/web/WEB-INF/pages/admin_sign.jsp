<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    boolean hasLoginError = "invalid".equals(request.getParameter("error"));
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Doctor / Staff Login</title>
    <link rel="stylesheet" href="<%= contextPath %>/auth.css">
</head>
<body>
    <main class="form-container">
        <h2>Doctor / Staff Login</h2>
        <form action="<%= contextPath %>/AdminServlet.do" method="post">
            <label for="username">Staff Username:</label>
            <input type="text" id="username" name="username" autocomplete="username" required>

            <label for="password">Staff Password:</label>
            <input type="password" id="password" name="password" autocomplete="current-password" required>

            <% if (hasLoginError) { %>
                <div id="errorMsg" class="error">Staff username or password is incorrect.</div>
            <% } %>

            <button type="submit">Login</button>
            <a class="secondary-link" href="<%= contextPath %>/index.html">Back to home</a>
        </form>
    </main>
</body>
</html>
