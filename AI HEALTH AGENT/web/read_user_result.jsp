<%@page import="za.ac.tut.model.User"%>
<%@page import="za.ac.tut.model.PatientSummary"%>
<%@page import="java.math.BigDecimal"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    User user = (User) request.getAttribute("user");
    PatientSummary summary = (PatientSummary) request.getAttribute("summary");
    boolean readonlyPortal = "true".equals(String.valueOf(request.getAttribute("readonlyPortal")));
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
        <p class="eyebrow"><%= readonlyPortal ? "Hospital portal" : "Doctor / Staff workspace" %></p>
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
                <div><dt>Personal Cell Number</dt><dd><%= user.getCellNumber() != null ? user.getCellNumber() : "" %></dd></div>
                <div><dt>ID Number</dt><dd><%= user.getIdNumber() != null ? user.getIdNumber() : "" %></dd></div>
                <div><dt>Emergency Contact</dt><dd><%= user.getEmergencyContactName() != null ? user.getEmergencyContactName() : "" %></dd></div>
                <div><dt>Emergency Contact Number</dt><dd><%= user.getEmergencyContactNumber() != null ? user.getEmergencyContactNumber() : "" %></dd></div>
                <div><dt>Blood Group</dt><dd><%= user.getBloodGroup() != null ? user.getBloodGroup() : "" %></dd></div>
                <div><dt>Known Allergies</dt><dd><%= user.getKnownAllergies() != null ? user.getKnownAllergies() : "" %></dd></div>
                <div><dt>Chronic Conditions</dt><dd><%= user.getChronicConditions() != null ? user.getChronicConditions() : "" %></dd></div>
                <div><dt>Address</dt><dd><%= user.getAddress() != null ? user.getAddress() : "" %></dd></div>
            </dl>

            <% if (summary != null) { %>
                <section class="summary-panel">
                    <h2>Doctor Summary</h2>
                    <dl class="readonly-list">
                        <div><dt>Average Heart Rate</dt><dd><%= formatDecimal(summary.getAveragePulse()) %> BPM</dd></div>
                        <div><dt>Average Temperature</dt><dd><%= formatDecimal(summary.getAverageTemperature()) %> °C</dd></div>
                        <div><dt>Average Blood Pressure</dt><dd><%= formatDecimal(summary.getAverageSystolic()) %>/<%= formatDecimal(summary.getAverageDiastolic()) %> mmHg</dd></div>
                        <div><dt>Readings Used</dt><dd><%= summary.getReadingCount() %></dd></div>
                        <div><dt>Prediction</dt><dd><%= summary.getPrediction() %></dd></div>
                    </dl>
                </section>
            <% } %>
        <% } %>

        <div class="actions">
            <% if (readonlyPortal) { %>
                <a class="btn primary" href="HospitalPatientsServlet.do">Back to Patient Search</a>
                <a class="btn secondary" href="index.html">Logout</a>
            <% } else { %>
                <a class="btn primary" href="read_user.jsp">Read Another Patient</a>
                <a class="btn secondary" href="admin_dashboard.jsp">Dashboard</a>
            <% } %>
        </div>
    </main>
</body>
</html>
<%!
    private String formatDecimal(BigDecimal value) {
        return value == null ? "No data" : value.setScale(1, java.math.RoundingMode.HALF_UP).toPlainString();
    }
%>
