package za.ac.tut.model;

public class PatientSummaryRow {

    private User user;
    private PatientSummary summary;

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public PatientSummary getSummary() { return summary; }
    public void setSummary(PatientSummary summary) { this.summary = summary; }
}
