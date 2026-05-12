package za.ac.tut.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReportResult {

    private String reportType;
    private String title;
    private String description;
    private final List<ReportColumn> columns = new ArrayList<>();
    private final List<Map<String, String>> rows = new ArrayList<>();

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ReportColumn> getColumns() {
        return columns;
    }

    public List<Map<String, String>> getRows() {
        return rows;
    }

    public void addColumn(String key, String label) {
        columns.add(new ReportColumn(key, label));
    }

    public void addRow(Map<String, String> row) {
        rows.add(row);
    }
}
