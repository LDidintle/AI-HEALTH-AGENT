package za.ac.tut.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import za.ac.tut.model.ReportColumn;
import za.ac.tut.model.ReportCriteria;
import za.ac.tut.model.ReportResult;
import za.ac.tut.util.AuthUtil;
import za.ac.tut.util.Database;
import za.ac.tut.util.ReportService;

public class ReportsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        boolean admin = AuthUtil.isAdmin(request);
        boolean hospital = AuthUtil.isHospital(request);

        if (!admin && !hospital) {
            response.sendRedirect(request.getContextPath() + "/admin");
            return;
        }

        ReportCriteria criteria = buildCriteria(request, admin, hospital);

        try (Connection conn = Database.getConnection()) {
            ReportResult result = ReportService.loadReport(conn, criteria);

            if ("csv".equalsIgnoreCase(trim(request.getParameter("export")))) {
                writeCsv(response, result, criteria);
                return;
            }

            request.setAttribute("criteria", criteria);
            request.setAttribute("report", result);
            request.getRequestDispatcher("reports.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Unable to load report.", e);
        }
    }

    private ReportCriteria buildCriteria(HttpServletRequest request, boolean admin, boolean hospital) {
        LocalDate today = LocalDate.now();
        LocalDate start = parseDate(request.getParameter("startDate"), today.withDayOfMonth(1));
        LocalDate end = parseDate(request.getParameter("endDate"), today);
        if (end.isBefore(start)) {
            end = start;
        }

        HttpSession session = request.getSession(false);
        ReportCriteria criteria = new ReportCriteria();
        criteria.setAdmin(admin);
        criteria.setHospital(hospital && !admin);
        criteria.setLegacyHospital(session != null && "true".equals(String.valueOf(session.getAttribute("hospitalLegacy"))));
        criteria.setHospitalId(session != null && session.getAttribute("hospitalId") instanceof Integer
                ? (Integer) session.getAttribute("hospitalId")
                : null);
        criteria.setReportType(ReportService.normalizeReportType(request.getParameter("type"), criteria.isHospital()));
        criteria.setStartDate(start);
        criteria.setEndDate(end);
        criteria.setStatus(normalizeStatus(request.getParameter("status")));
        criteria.setPatientId(trim(request.getParameter("patientId")));
        criteria.setSearch(trim(request.getParameter("search")));
        return criteria;
    }

    private LocalDate parseDate(String value, LocalDate fallback) {
        String trimmed = trim(value);
        if (trimmed.isEmpty()) {
            return fallback;
        }
        try {
            return LocalDate.parse(trimmed);
        } catch (Exception e) {
            return fallback;
        }
    }

    private String normalizeStatus(String value) {
        return trim(value).toUpperCase();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private void writeCsv(HttpServletResponse response, ReportResult report, ReportCriteria criteria)
            throws IOException {
        String filename = "smarthealth-" + report.getReportType() + "-"
                + criteria.getStartDateText() + "-to-" + criteria.getEndDateText() + ".csv";
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (PrintWriter writer = response.getWriter()) {
            for (int i = 0; i < report.getColumns().size(); i++) {
                if (i > 0) {
                    writer.print(',');
                }
                writer.print(csv(report.getColumns().get(i).getLabel()));
            }
            writer.println();

            for (Map<String, String> row : report.getRows()) {
                for (int i = 0; i < report.getColumns().size(); i++) {
                    ReportColumn column = report.getColumns().get(i);
                    if (i > 0) {
                        writer.print(',');
                    }
                    writer.print(csv(row.get(column.getKey())));
                }
                writer.println();
            }
        }
    }

    private String csv(String value) {
        String text = value == null ? "" : value;
        return "\"" + text.replace("\"", "\"\"").replace("\r", " ").replace("\n", " ") + "\"";
    }
}
