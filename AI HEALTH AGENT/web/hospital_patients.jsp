<%@page import="java.util.List"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.sql.Timestamp"%>
<%@page import="za.ac.tut.model.HospitalAlertPatientRow"%>
<%@page import="za.ac.tut.util.CsrfUtil"%>
<%@page import="za.ac.tut.util.HospitalAlertStatusService"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    List<HospitalAlertPatientRow> patients = (List<HospitalAlertPatientRow>) request.getAttribute("patients");
    String csrfToken = CsrfUtil.token(request);
    String updateStatus = request.getParameter("status");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hospital Patient Portal</title>
    <link rel="stylesheet" href="page-ui.css">
</head>
<body class="app-page">
    <main class="shell wide">
        <p class="eyebrow">Hospital portal</p>
        <h1><%= request.getAttribute("hospitalName") == null ? "Patient Alerts" : request.getAttribute("hospitalName") %></h1>
        <p class="lead">Review patients assigned to this hospital from demo emergency alerts<%= request.getAttribute("hospitalServiceArea") == null ? "." : " in " + request.getAttribute("hospitalServiceArea") + "." %> Mark alert progress or remove attended alerts from this active list without deleting patient records.</p>

        <% if ("updated".equals(updateStatus)) { %>
            <p class="message success">Alert status updated.</p>
        <% } else if ("not_found".equals(updateStatus)) { %>
            <p class="message error">That alert was not found for this hospital.</p>
        <% } else if ("invalid".equals(updateStatus)) { %>
            <p class="message error">Choose a valid alert action.</p>
        <% } %>

        <div class="actions">
            <a class="btn primary" href="ReportsServlet.do?type=alerts">Emergency Report</a>
            <a class="btn secondary" href="SignOutServlet.do">Logout</a>
        </div>

        <section class="stage-guide" aria-label="Alert stage guide">
            <div>
                <span class="status-pill pending">Not solved</span>
                <p>New alert. Staff have not started reviewing it yet.</p>
            </div>
            <div>
                <span class="status-pill in-progress">Ongoing</span>
                <p>Staff are reviewing the patient and coordinating next steps.</p>
            </div>
            <div>
                <span class="status-pill verified">Resolved</span>
                <p>Alert has been attended. Remove it when it should leave the active queue.</p>
            </div>
        </section>

        <div class="toolbar">
            <div>
                <label for="patientSearch">Search alert patients</label>
                <input type="search" id="patientSearch" placeholder="Search by system ID, ID number, name, email, phone, condition, or blood group">
            </div>
            <button class="btn secondary" type="button" id="clearSearch">Clear</button>
        </div>

        <div class="result-count" id="resultCount"></div>

        <div class="table-wrap">
            <table class="data-table" id="patientsTable">
                <tr>
                    <th>Alert</th>
                    <th>Patient</th>
                    <th>Contact</th>
                    <th>Vitals</th>
                    <th>Screening Note</th>
                    <th>Details</th>
                </tr>
                <% if (patients != null && !patients.isEmpty()) {
                    for (HospitalAlertPatientRow row : patients) { %>
                    <tr data-patient-row>
                        <td class="alert-cell">
                            <div class="pill-stack">
                                <span class="status-pill <%= "CRITICAL".equals(row.getLatestAlertStatus()) ? "pending" : "verified" %>"><%= safe(row.getLatestAlertStatus()) %></span>
                                <span class="status-pill <%= assignmentClass(row.getAssignmentStatus()) %>"><%= displayAssignmentStatus(row.getAssignmentStatus()) %></span>
                            </div>
                            <div class="muted-line"><%= formatTimestamp(row.getLatestAlertCreatedAt()) %></div>
                            <% if (row.getLatestAlertId() != null) { %>
                                <form action="HospitalAlertStatusServlet.do" method="post" class="alert-status-form">
                                    <input type="hidden" name="<%= CsrfUtil.PARAMETER %>" value="<%= csrfToken %>">
                                    <input type="hidden" name="alertId" value="<%= row.getLatestAlertId() %>">
                                    <label for="alertStatus<%= row.getLatestAlertId() %>">Hospital status</label>
                                    <div class="status-control">
                                        <select id="alertStatus<%= row.getLatestAlertId() %>" name="statusAction">
                                            <option value="not_solved" <%= selectedAssignment(row.getAssignmentStatus(), HospitalAlertStatusService.ASSIGNED) %>>Not solved</option>
                                            <option value="ongoing" <%= selectedAssignment(row.getAssignmentStatus(), HospitalAlertStatusService.ONGOING) %>>Ongoing</option>
                                            <option value="resolved" <%= selectedAssignment(row.getAssignmentStatus(), HospitalAlertStatusService.RESOLVED) %>>Resolved</option>
                                        </select>
                                        <button class="btn primary compact" type="submit">Update</button>
                                    </div>
                                    <button class="btn danger compact" type="submit" name="action" value="remove" onclick="return confirm('Remove this attended alert from the active hospital list? Patient records will not be deleted.');">Remove alert</button>
                                </form>
                            <% } %>
                        </td>
                        <td class="patient-cell">
                            <strong><%= row.getUser().getFirstName() %> <%= row.getUser().getSurname() %></strong>
                            <span><%= row.getUser().getEmail() %></span>
                            <span>System ID <%= row.getUser().getId() %></span>
                            <span>ID <%= safe(row.getUser().getIdNumber()) %></span>
                        </td>
                        <td class="patient-cell">
                            <strong><%= safe(row.getUser().getCellNumber()) %></strong>
                            <span><%= safe(row.getUser().getEmergencyContactName()) %></span>
                            <span><%= safe(row.getUser().getEmergencyContactNumber()) %></span>
                            <span>Blood group <%= safe(row.getUser().getBloodGroup()) %></span>
                        </td>
                        <td class="vitals-list">
                            <span><strong>HR</strong> <%= formatDecimal(row.getSummary().getAveragePulse()) %> BPM</span>
                            <span><strong>Temp</strong> <%= formatDecimal(row.getSummary().getAverageTemperature()) %> °C</span>
                            <span><strong>BP</strong> <%= formatDecimal(row.getSummary().getAverageSystolic()) %>/<%= formatDecimal(row.getSummary().getAverageDiastolic()) %></span>
                        </td>
                        <td class="screening-note">
                            <div class="detail-card">
                                <span><%= escapeHtml(screeningPreview(row)) %></span>
                            </div>
                        </td>
                        <td class="details-cell">
                            <div class="details-stack">
                                <div class="detail-card">
                                    <strong class="detail-label"><%= detailHeading(row) %></strong>
                                    <span><%= escapeHtml(detailPreview(row)) %></span>
                                    <span class="detail-meta"><%= escapeHtml(detailMeta(row)) %></span>
                                </div>
                                <div class="row-actions">
                                    <a class="btn primary compact" href="HospitalPatientDetailsServlet.do?id=<%= row.getUser().getId() %>">View</a>
                                </div>
                            </div>
                        </td>
                    </tr>
                <%  }
                } else { %>
                    <tr><td colspan="6" class="empty">No active emergency-alert patients assigned to this hospital yet</td></tr>
                <% } %>
                <tr id="noSearchResults" class="hidden">
                    <td colspan="6" class="empty">No matching alert patients found</td>
                </tr>
            </table>
        </div>
    </main>

    <script>
        const searchInput = document.getElementById('patientSearch');
        const clearSearch = document.getElementById('clearSearch');
        const resultCount = document.getElementById('resultCount');
        const patientRows = Array.from(document.querySelectorAll('[data-patient-row]'));
        const noSearchResults = document.getElementById('noSearchResults');

        function updateSearch() {
            const query = searchInput.value.trim().toLowerCase();
            let visible = 0;
            patientRows.forEach(row => {
                const match = row.textContent.toLowerCase().includes(query);
                row.classList.toggle('hidden', !match);
                if (match) visible++;
            });
            noSearchResults.classList.toggle('hidden', !(visible === 0 && patientRows.length > 0));
            resultCount.textContent = visible + ' of ' + patientRows.length + ' patients shown';
        }

        searchInput.addEventListener('input', updateSearch);
        clearSearch.addEventListener('click', () => {
            searchInput.value = '';
            searchInput.focus();
            updateSearch();
        });
        updateSearch();
    </script>
</body>
</html>
<%!
    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String screeningPreview(HospitalAlertPatientRow row) {
        if (row == null || row.getSummary() == null || !hasText(row.getSummary().getPrediction())) {
            return "Awaiting screening note.";
        }
        if (row.getSummary().getReadingCount() <= 0) {
            return "Awaiting synced vitals before screening. Open the patient record to review patient context.";
        }
        return truncate(row.getSummary().getPrediction(), 180);
    }

    private String detailHeading(HospitalAlertPatientRow row) {
        return hasText(noteText(row)) ? "Clinical note" : "Patient context";
    }

    private String detailPreview(HospitalAlertPatientRow row) {
        String noteText = noteText(row);
        if (hasText(noteText)) {
            return truncate(noteText, 160);
        }

        String conditions = clean(row.getUser().getChronicConditions());
        String allergies = clean(row.getUser().getKnownAllergies());

        if (conditions != null && allergies != null) {
            return "Conditions: " + conditions + ". Allergies: " + allergies + ".";
        }
        if (conditions != null) {
            return "Conditions: " + conditions + ".";
        }
        if (allergies != null) {
            return "Allergies: " + allergies + ".";
        }
        return "No clinical notes saved yet. Open the patient record to add findings and review history.";
    }

    private String detailMeta(HospitalAlertPatientRow row) {
        String noteText = noteText(row);
        if (hasText(noteText)) {
            String updatedAt = compactTimestamp(row.getClinicalNote().getUpdatedAt());
            String role = clean(row.getClinicalNote().getUpdatedByRole());
            if (updatedAt.isEmpty() && role == null) {
                return "Saved clinical note";
            }
            if (updatedAt.isEmpty()) {
                return "Updated by " + role;
            }
            if (role == null) {
                return "Updated " + updatedAt;
            }
            return "Updated " + updatedAt + " by " + role;
        }
        return "View full patient details and add clinical notes.";
    }

    private String formatDecimal(BigDecimal value) {
        return value == null ? "No data" : value.setScale(1, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private String formatTimestamp(Timestamp value) {
        return value == null ? "No alert date" : value.toString();
    }

    private String compactTimestamp(Timestamp value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        return text.length() > 16 ? text.substring(0, 16).replace('T', ' ') : text;
    }

    private String displayAssignmentStatus(String value) {
        return HospitalAlertStatusService.displayStatus(value);
    }

    private String assignmentClass(String value) {
        String status = value == null ? "" : value.trim();
        if (HospitalAlertStatusService.RESOLVED.equalsIgnoreCase(status)) {
            return "verified";
        }
        if (HospitalAlertStatusService.ONGOING.equalsIgnoreCase(status)) {
            return "in-progress";
        }
        return "pending";
    }

    private String selectedAssignment(String current, String option) {
        String status = current == null || current.trim().isEmpty()
                ? HospitalAlertStatusService.ASSIGNED
                : current.trim();
        return option.equalsIgnoreCase(status) ? "selected" : "";
    }

    private String noteText(HospitalAlertPatientRow row) {
        return row.getClinicalNote() == null ? null : row.getClinicalNote().getNoteText();
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean hasText(String value) {
        return clean(value) != null;
    }

    private String truncate(String value, int maxLength) {
        String cleaned = clean(value);
        if (cleaned == null || cleaned.length() <= maxLength) {
            return cleaned == null ? "" : cleaned;
        }
        return cleaned.substring(0, Math.max(0, maxLength - 1)).trim() + "…";
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
