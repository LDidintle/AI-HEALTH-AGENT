<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Find Patient</title>
    <link rel="stylesheet" href="page-ui.css">
</head>
<body class="app-page">
    <main class="shell">
        <p class="eyebrow">Doctor / Staff workspace</p>
        <h1>Find Patient</h1>
        <p class="lead">Enter the patient's email address to view their saved account details.</p>

        <%
            String errorMessage = (String) request.getAttribute("errorMessage");
            String searchedEmail = (String) request.getAttribute("searchedEmail");
            if (searchedEmail == null) {
                searchedEmail = "";
            }
        %>

        <% if (errorMessage != null) { %>
            <div class="message error"><%= errorMessage %></div>
        <% } %>

        <form action="ReadUserServlet.do" method="post">
            <label for="email">Email</label>
            <input type="email" id="email" name="email" value="<%= searchedEmail %>" required>

            <div class="actions">
                <button class="btn primary" type="submit">Submit Email</button>
                <a class="btn secondary" href="admin_dashboard.jsp">Dashboard</a>
            </div>
        </form>
    </main>
</body>
</html>
