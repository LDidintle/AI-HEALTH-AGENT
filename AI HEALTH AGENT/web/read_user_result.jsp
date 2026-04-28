<%@page import="za.ac.tut.model.User"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    User user = (User) request.getAttribute("user");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Patient Details</title>
    <link rel="stylesheet" href="page-ui.css">
</head>
<body class="app-page">
    <main class="shell">
        <p class="eyebrow">Doctor / Staff workspace</p>
        <h1>Patient Details</h1>

        <% if (user == null) { %>
            <p class="lead">No patient details are available.</p>
        <% } else { %>
            <dl class="readonly-list">
                <div><dt>ID</dt><dd><%= user.getId() %></dd></div>
                <div><dt>Title</dt><dd><%= user.getTitle() != null ? user.getTitle() : "" %></dd></div>
                <div><dt>First Name</dt><dd><%= user.getFirstName() != null ? user.getFirstName() : "" %></dd></div>
                <div><dt>Surname</dt><dd><%= user.getSurname() != null ? user.getSurname() : "" %></dd></div>
                <div><dt>Date of Birth</dt><dd><%= user.getDob() != null ? user.getDob() : "" %></dd></div>
                <div><dt>Gender</dt><dd><%= user.getGender() != null ? user.getGender() : "" %></dd></div>
                <div><dt>Marital Status</dt><dd><%= user.getMaritalStatus() != null ? user.getMaritalStatus() : "" %></dd></div>
                <div><dt>Email</dt><dd><%= user.getEmail() != null ? user.getEmail() : "" %></dd></div>
                <div><dt>Cell Number</dt><dd><%= user.getCellNumber() != null ? user.getCellNumber() : "" %></dd></div>
                <div><dt>Address</dt><dd><%= user.getAddress() != null ? user.getAddress() : "" %></dd></div>
            </dl>
        <% } %>

        <div class="actions">
            <a class="btn primary" href="read_user.jsp">Read Another Patient</a>
            <a class="btn secondary" href="admin_dashboard.jsp">Dashboard</a>
        </div>
    </main>
</body>
</html>
