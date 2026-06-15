package za.ac.tut.model;

import java.sql.Timestamp;

public class HospitalAlertPatientRow extends PatientSummaryRow {
    private Integer latestAlertId;
    private String latestAlertStatus;
    private String assignmentStatus;
    private Timestamp latestAlertCreatedAt;

    public Integer getLatestAlertId() {
        return latestAlertId;
    }

    public void setLatestAlertId(Integer latestAlertId) {
        this.latestAlertId = latestAlertId;
    }

    public String getLatestAlertStatus() {
        return latestAlertStatus;
    }

    public void setLatestAlertStatus(String latestAlertStatus) {
        this.latestAlertStatus = latestAlertStatus;
    }

    public String getAssignmentStatus() {
        return assignmentStatus;
    }

    public void setAssignmentStatus(String assignmentStatus) {
        this.assignmentStatus = assignmentStatus;
    }

    public Timestamp getLatestAlertCreatedAt() {
        return latestAlertCreatedAt;
    }

    public void setLatestAlertCreatedAt(Timestamp latestAlertCreatedAt) {
        this.latestAlertCreatedAt = latestAlertCreatedAt;
    }
}
