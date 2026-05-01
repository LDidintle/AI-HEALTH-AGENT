<%@page import="java.util.List"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="za.ac.tut.model.PatientSummaryRow"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    List<PatientSummaryRow> patients = (List<PatientSummaryRow>) request.getAttribute("patients");
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
        <h1>Patient Search</h1>
        <p class="lead">Search registered patients and review read-only doctor summaries. This portal cannot add, edit, or remove patients.</p>

        <div class="actions">
            <a class="btn secondary" href="index.html">Logout</a>
        </div>

        <div class="toolbar">
            <div>
                <label for="patientSearch">Search patients</label>
                <input type="search" id="patientSearch" placeholder="Search by system ID, ID number, name, email, phone, condition, or blood group">
            </div>
            <button class="btn secondary" type="button" id="clearSearch">Clear</button>
        </div>

        <div class="result-count" id="resultCount"></div>

        <div class="table-wrap">
            <table class="data-table" id="patientsTable">
                <tr>
                    <th>System ID</th>
                    <th>Patient</th>
                    <th>ID Number</th>
                    <th>Personal Number</th>
                    <th>Emergency Contact</th>
                    <th>Blood Group</th>
                    <th>Average Vitals</th>
                    <th>Prediction</th>
                    <th>Details</th>
                </tr>
                <% if (patients != null && !patients.isEmpty()) {
                    for (PatientSummaryRow row : patients) { %>
                    <tr data-patient-row>
                        <td><%= row.getUser().getId() %></td>
                        <td><%= row.getUser().getFirstName() %> <%= row.getUser().getSurname() %><br><%= row.getUser().getEmail() %></td>
                        <td><%= safe(row.getUser().getIdNumber()) %></td>
                        <td><%= safe(row.getUser().getCellNumber()) %></td>
                        <td><%= safe(row.getUser().getEmergencyContactName()) %><br><%= safe(row.getUser().getEmergencyContactNumber()) %></td>
                        <td><%= safe(row.getUser().getBloodGroup()) %></td>
                        <td>
                            HR: <%= formatDecimal(row.getSummary().getAveragePulse()) %> BPM<br>
                            Temp: <%= formatDecimal(row.getSummary().getAverageTemperature()) %> °C<br>
                            BP: <%= formatDecimal(row.getSummary().getAverageSystolic()) %>/<%= formatDecimal(row.getSummary().getAverageDiastolic()) %>
                        </td>
                        <td><%= row.getSummary().getPrediction() %></td>
                        <td><a class="btn primary" href="HospitalPatientDetailsServlet.do?id=<%= row.getUser().getId() %>">View</a></td>
                    </tr>
                <%  }
                } else { %>
                    <tr><td colspan="9" class="empty">No patients found</td></tr>
                <% } %>
                <tr id="noSearchResults" class="hidden">
                    <td colspan="9" class="empty">No matching patients found</td>
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

    private String formatDecimal(BigDecimal value) {
        return value == null ? "No data" : value.setScale(1, java.math.RoundingMode.HALF_UP).toPlainString();
    }
%>
