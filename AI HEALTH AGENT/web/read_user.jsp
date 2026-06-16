<%@page import="za.ac.tut.util.CsrfUtil"%>
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
        <p class="lead">Enter the patient's numeric system ID or email address to view saved details, the predicted screening note, and editable clinical notes.</p>

        <%
            String errorMessage = (String) request.getAttribute("errorMessage");
            String searchedEmail = (String) request.getAttribute("searchedEmail");
            String searchedId = (String) request.getAttribute("searchedId");
            if (searchedEmail == null) {
                searchedEmail = "";
            }
            if (searchedId == null) {
                searchedId = "";
            }
        %>

        <% if (errorMessage != null) { %>
            <div class="message error"><%= errorMessage %></div>
        <% } %>

        <form action="ReadUserServlet.do" method="post">
            <input type="hidden" name="<%= CsrfUtil.PARAMETER %>" value="<%= CsrfUtil.token(request) %>">

            <label for="patient_id">Patient System ID</label>
            <input type="number" id="patient_id" name="patient_id" value="<%= searchedId %>" inputmode="numeric">

            <label for="email">Email</label>
            <input type="email" id="email" name="email" value="<%= searchedEmail %>">

            <div class="actions">
                <button class="btn primary" type="submit">Find Patient</button>
                <a class="btn secondary" href="admin_dashboard.jsp">Dashboard</a>
            </div>
        </form>
    </main>
</body>
</html>
