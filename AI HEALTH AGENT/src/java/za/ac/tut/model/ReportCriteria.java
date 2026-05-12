package za.ac.tut.model;

import java.time.LocalDate;

public class ReportCriteria {

    private String reportType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String patientId;
    private String search;
    private boolean admin;
    private boolean hospital;
    private boolean legacyHospital;
    private Integer hospitalId;

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isHospital() {
        return hospital;
    }

    public void setHospital(boolean hospital) {
        this.hospital = hospital;
    }

    public boolean isLegacyHospital() {
        return legacyHospital;
    }

    public void setLegacyHospital(boolean legacyHospital) {
        this.legacyHospital = legacyHospital;
    }

    public Integer getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(Integer hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getStartDateText() {
        return startDate == null ? "" : startDate.toString();
    }

    public String getEndDateText() {
        return endDate == null ? "" : endDate.toString();
    }
}
