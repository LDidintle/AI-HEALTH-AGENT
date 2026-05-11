package za.ac.tut.model;

import java.sql.Timestamp;

public class HospitalAlertPatientRow extends PatientSummaryRow {
    private String latestAlertStatus;
    private Timestamp latestAlertCreatedAt;

    public String getLatestAlertStatus() {
        return latestAlertStatus;
    }

    public void setLatestAlertStatus(String latestAlertStatus) {
        this.latestAlertStatus = latestAlertStatus;
    }

    public Timestamp getLatestAlertCreatedAt() {
        return latestAlertCreatedAt;
    }

    public void setLatestAlertCreatedAt(Timestamp latestAlertCreatedAt) {
        this.latestAlertCreatedAt = latestAlertCreatedAt;
    }
}
