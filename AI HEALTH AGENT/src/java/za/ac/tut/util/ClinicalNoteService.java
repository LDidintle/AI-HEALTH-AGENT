package za.ac.tut.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import za.ac.tut.model.ClinicalNote;

public final class ClinicalNoteService {

    private static final int MAX_NOTE_LENGTH = 5000;

    private ClinicalNoteService() {
    }

    public static ClinicalNote load(Connection conn, int userId) throws Exception {
        ClinicalNote note = new ClinicalNote();
        String sql = "SELECT note_text, updated_by_role, updated_by_actor, created_at, updated_at "
                + "FROM clinical_notes WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    note.setNoteText(rs.getString("note_text"));
                    note.setUpdatedByRole(rs.getString("updated_by_role"));
                    note.setUpdatedByActor(rs.getString("updated_by_actor"));
                    note.setCreatedAt(rs.getTimestamp("created_at"));
                    note.setUpdatedAt(rs.getTimestamp("updated_at"));
                }
            }
        }
        return note;
    }

    public static boolean save(Connection conn, int userId, String noteText, String updatedByRole,
            String updatedByActor) throws Exception {
        if (userId <= 0) {
            throw new IllegalArgumentException("A valid patient id is required.");
        }

        String normalized = normalize(noteText);
        if (normalized == null) {
            delete(conn, userId);
            return false;
        }

        int updated = update(conn, userId, normalized, clean(updatedByRole), clean(updatedByActor));
        if (updated == 0) {
            insert(conn, userId, normalized, clean(updatedByRole), clean(updatedByActor));
        }
        return true;
    }

    private static int update(Connection conn, int userId, String noteText, String updatedByRole,
            String updatedByActor) throws Exception {
        String sql = "UPDATE clinical_notes SET note_text = ?, updated_by_role = ?, updated_by_actor = ?, "
                + "updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, noteText);
            ps.setString(2, updatedByRole);
            ps.setString(3, updatedByActor);
            ps.setInt(4, userId);
            return ps.executeUpdate();
        }
    }

    private static void insert(Connection conn, int userId, String noteText, String updatedByRole,
            String updatedByActor) throws Exception {
        String sql = "INSERT INTO clinical_notes (user_id, note_text, updated_by_role, updated_by_actor) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, noteText);
            ps.setString(3, updatedByRole);
            ps.setString(4, updatedByActor);
            ps.executeUpdate();
        }
    }

    private static void delete(Connection conn, int userId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM clinical_notes WHERE user_id = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replace("\r\n", "\n").replace('\r', '\n').trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > MAX_NOTE_LENGTH) {
            throw new IllegalArgumentException("Clinical notes must be 5000 characters or fewer.");
        }
        return normalized;
    }

    private static String clean(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
