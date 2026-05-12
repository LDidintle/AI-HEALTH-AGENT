<%@page import="java.util.Map"%>
<%@page import="za.ac.tut.model.ReportColumn"%>
<%@page import="za.ac.tut.model.ReportCriteria"%>
<%@page import="za.ac.tut.model.ReportResult"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    ReportCriteria criteria = (ReportCriteria) request.getAttribute("criteria");
    ReportResult report = (ReportResult) request.getAttribute("report");
    if (criteria == null || report == null) {
        response.sendRedirect("ReportsServlet.do");
        return;
    }
    String type = report == null ? "management" : report.getReportType();
    boolean management = "management".equals(type);
    boolean alerts = "alerts".equals(type);
    boolean hospital = criteria != null && criteria.isHospital();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= h(report == null ? "Reports" : report.getTitle()) %></title>
    <link rel="stylesheet" href="page-ui.css">
</head>
<body class="app-page">
    <main class="shell wide">
        <p class="eyebrow"><%= hospital ? "Hospital reports" : "Doctor / Staff reports" %></p>
        <h1><%= h(report == null ? "Reports" : report.getTitle()) %></h1>
        <p class="lead"><%= h(report == null ? "" : report.getDescription()) %></p>

        <% if (!hospital) { %>
            <nav class="report-tabs" aria-label="Report pages">
                <a class="<%= active(type, "management") %>" href="ReportsServlet.do?type=management">Management Summary</a>
                <a class="<%= active(type, "vitals") %>" href="ReportsServlet.do?type=vitals">Patient Vitals</a>
                <a class="<%= active(type, "alerts") %>" href="ReportsServlet.do?type=alerts">Emergency Alerts</a>
                <a class="<%= active(type, "sync") %>" href="ReportsServlet.do?type=sync">Mobile Sync</a>
            </nav>
        <% } %>

        <form class="report-filter" action="ReportsServlet.do" method="get">
            <input type="hidden" name="type" value="<%= h(type) %>">
            <div class="filter-grid">
                <div>
                    <label for="startDate">Start date</label>
                    <input type="date" id="startDate" name="startDate" value="<%= h(criteria == null ? "" : criteria.getStartDateText()) %>">
                </div>
                <div>
                    <label for="endDate">End date</label>
                    <input type="date" id="endDate" name="endDate" value="<%= h(criteria == null ? "" : criteria.getEndDateText()) %>">
                </div>

                <% if (!management) { %>
                    <div>
                        <label for="patientId">Patient system ID</label>
                        <input type="number" id="patientId" name="patientId" min="1" value="<%= h(criteria == null ? "" : criteria.getPatientId()) %>" placeholder="All patients">
                    </div>
                    <div>
                        <label for="search">Search</label>
                        <input type="search" id="search" name="search" value="<%= h(criteria == null ? "" : criteria.getSearch()) %>" placeholder="Name, email, hospital, source">
                    </div>
                <% } %>

                <% if (alerts) { %>
                    <div>
                        <label for="status">Alert status</label>
                        <select id="status" name="status">
                            <option value="" <%= selected(criteria, "") %>>All statuses</option>
                            <option value="WARNING" <%= selected(criteria, "WARNING") %>>Warning</option>
                            <option value="CRITICAL" <%= selected(criteria, "CRITICAL") %>>Critical</option>
                            <option value="STARTED" <%= selected(criteria, "STARTED") %>>Started</option>
                        </select>
                    </div>
                <% } %>
            </div>

            <div class="actions">
                <button class="btn primary" type="submit">Run Report</button>
                <button class="btn secondary" type="submit" name="export" value="csv">Export CSV</button>
                <% if (hospital) { %>
                    <a class="btn secondary" href="HospitalPatientsServlet.do">Back to Portal</a>
                <% } else { %>
                    <a class="btn secondary" href="admin_dashboard.jsp">Dashboard</a>
                <% } %>
            </div>
        </form>

        <p class="report-meta">
            Showing <%= report == null ? 0 : report.getRows().size() %> row(s)
            from <%= h(criteria == null ? "" : criteria.getStartDateText()) %>
            to <%= h(criteria == null ? "" : criteria.getEndDateText()) %>.
            <% if (hospital) { %>This report is filtered to the signed-in hospital account.<% } %>
        </p>

        <div class="table-wrap">
            <table class="data-table">
                <tr>
                    <% if (report != null) {
                        for (ReportColumn column : report.getColumns()) { %>
                            <th><%= h(column.getLabel()) %></th>
                    <%  }
                    } %>
                </tr>

                <% if (report != null && !report.getRows().isEmpty()) {
                    for (Map<String, String> row : report.getRows()) { %>
                        <tr>
                            <% for (ReportColumn column : report.getColumns()) { %>
                                <td><%= cell(row.get(column.getKey())) %></td>
                            <% } %>
                        </tr>
                <%  }
                } else { %>
                    <tr>
                        <td class="empty" colspan="<%= report == null ? 1 : report.getColumns().size() %>">No report data found for the selected filters.</td>
                    </tr>
                <% } %>
            </table>
        </div>
    </main>
</body>
</html>
<%!
    private String h(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String cell(String value) {
        return h(value).replace("\n", "<br>");
    }

    private String active(String current, String expected) {
        return expected.equals(current) ? "active" : "";
    }

    private String selected(ReportCriteria criteria, String expected) {
        String actual = criteria == null || criteria.getStatus() == null ? "" : criteria.getStatus();
        return expected.equals(actual) ? "selected" : "";
    }
%>
