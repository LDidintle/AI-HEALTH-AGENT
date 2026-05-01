<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hospital Portal Login</title>
    <link rel="stylesheet" href="auth.css">
</head>
<body>
    <main class="form-container">
        <h2>Hospital Portal</h2>
        <p class="lead">Read-only access for hospital staff to search patients and review summaries.</p>
        <% if (request.getAttribute("error") != null) { %>
            <div class="error"><%= request.getAttribute("error") %></div>
        <% } %>
        <form action="HospitalLoginServlet.do" method="post">
            <label for="username">Hospital Username:</label>
            <input type="text" id="username" name="username" required>

            <label for="password">Hospital Password:</label>
            <input type="password" id="password" name="password" required>

            <button type="submit">Login</button>
            <a class="secondary-link" href="index.html">Back to home</a>
        </form>
    </main>
</body>
</html>
