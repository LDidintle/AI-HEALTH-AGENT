<%@page import="za.ac.tut.model.ClinicalNote"%>
<%@page import="za.ac.tut.model.User"%>
<%@page import="za.ac.tut.model.PatientSummary"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.sql.Timestamp"%>
<%@page import="za.ac.tut.util.CsrfUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    User user = (User) request.getAttribute("user");
    PatientSummary summary = (PatientSummary) request.getAttribute("summary");
    ClinicalNote clinicalNote = (ClinicalNote) request.getAttribute("clinicalNote");
    boolean hospitalPortal = "true".equals(String.valueOf(request.getAttribute("hospitalPortal")));
    String noteStatus = request.getParameter("note");
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
        <p class="eyebrow"><%= hospitalPortal ? "Hospital portal" : "Doctor / Staff workspace" %></p>
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
                    <h2>Predicted Screening Summary</h2>
                    <dl class="readonly-list">
                        <div><dt>Average Heart Rate</dt><dd><%= formatDecimal(summary.getAveragePulse()) %> BPM</dd></div>
                        <div><dt>Average Temperature</dt><dd><%= formatDecimal(summary.getAverageTemperature()) %> °C</dd></div>
                        <div><dt>Average Blood Pressure</dt><dd><%= formatDecimal(summary.getAverageSystolic()) %>/<%= formatDecimal(summary.getAverageDiastolic()) %> mmHg</dd></div>
                        <div><dt>Readings Used</dt><dd><%= summary.getReadingCount() %></dd></div>
                        <div><dt>Screening Note</dt><dd><%= escapeHtml(summary.getPrediction()) %></dd></div>
                    </dl>
                </section>
            <% } %>

            <section class="summary-panel">
                <h2>Clinical Notes</h2>
                <% if ("saved".equals(noteStatus)) { %>
                    <p class="message success">Clinical notes saved.</p>
                <% } else if ("cleared".equals(noteStatus)) { %>
                    <p class="message success">Clinical notes cleared.</p>
                <% } else if ("invalid".equals(noteStatus)) { %>
                    <p class="message error">Clinical notes must be 5000 characters or fewer.</p>
                <% } else if ("forbidden".equals(noteStatus)) { %>
                    <p class="message error">You do not have access to update notes for this patient.</p>
                <% } %>
                <form action="UpdateClinicalNoteServlet.do" method="post" class="clinical-note-form">
                    <input type="hidden" name="<%= CsrfUtil.PARAMETER %>" value="<%= CsrfUtil.token(request) %>">
                    <input type="hidden" name="userId" value="<%= user.getId() %>">
                    <label for="noteText">Detailed clinician observations</label>
                    <textarea id="noteText" name="noteText" rows="10" maxlength="5000" placeholder="Record findings, follow-up plans, and important clinical context here."><%= clinicalNote == null ? "" : escapeHtml(clinicalNote.getNoteText()) %></textarea>
                    <p class="note-meta">
                        <%= clinicalNote == null || clinicalNote.getUpdatedAt() == null
                                ? "No clinical notes saved yet."
                                : "Last updated " + formatTimestamp(clinicalNote.getUpdatedAt()) + formatUpdater(clinicalNote) %>
                    </p>
                    <div class="actions">
                        <button class="btn primary" type="submit">Save Clinical Notes</button>
                    </div>
                </form>
            </section>
        <% } %>

        <div class="actions">
            <% if (hospitalPortal) { %>
                <a class="btn primary" href="HospitalPatientsServlet.do">Back to Patient Search</a>
                <a class="btn secondary" href="SignOutServlet.do">Logout</a>
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

    private String formatTimestamp(Timestamp value) {
        return value == null ? "" : value.toString();
    }

    private String formatUpdater(ClinicalNote note) {
        String role = note.getUpdatedByRole();
        String actor = note.getUpdatedByActor();
        if (role == null || role.trim().isEmpty()) {
            return ".";
        }
        if (actor == null || actor.trim().isEmpty()) {
            return " by " + escapeHtml(role) + ".";
        }
        return " by " + escapeHtml(role) + " (" + escapeHtml(actor) + ").";
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>
