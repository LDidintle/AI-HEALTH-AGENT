package za.ac.tut.model;

import java.sql.Timestamp;

public class ClinicalNote {

    private String noteText;
    private String updatedByRole;
    private String updatedByActor;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public String getUpdatedByRole() {
        return updatedByRole;
    }

    public void setUpdatedByRole(String updatedByRole) {
        this.updatedByRole = updatedByRole;
    }

    public String getUpdatedByActor() {
        return updatedByActor;
    }

    public void setUpdatedByActor(String updatedByActor) {
        this.updatedByActor = updatedByActor;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
