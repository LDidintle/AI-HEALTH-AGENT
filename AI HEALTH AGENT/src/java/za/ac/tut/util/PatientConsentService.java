package za.ac.tut.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Savepoint;

public final class PatientConsentService {

    public static final String HEALTH_SYNC = "HEALTH_SYNC";

    private PatientConsentService() {
    }

    public static void recordHealthSyncConsent(Connection conn, int userId, String version) {
        Savepoint savepoint = null;
        try {
            if (!conn.getAutoCommit()) {
                savepoint = conn.setSavepoint("optional_patient_consent");
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO patient_consents (user_id, consent_type, consent_version, accepted) VALUES (?, ?, ?, TRUE)")) {
                ps.setInt(1, userId);
                ps.setString(2, HEALTH_SYNC);
                ps.setString(3, version);
                ps.executeUpdate();
            }

            if (savepoint != null) {
                conn.releaseSavepoint(savepoint);
            }
        } catch (Exception ignored) {
            rollbackOptionalWrite(conn, savepoint);
            // Older demo databases may not have this table until the production migration is applied.
        }
    }

    public static boolean hasAcceptedHealthSyncConsent(Connection conn, int userId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT consent_id FROM patient_consents WHERE user_id = ? AND consent_type = ? AND accepted = TRUE "
                + "ORDER BY created_at DESC " + limitOne(conn))) {
            ps.setInt(1, userId);
            ps.setString(2, HEALTH_SYNC);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static String limitOne(Connection conn) throws Exception {
        String productName = conn.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("derby")
                ? "FETCH FIRST 1 ROW ONLY" : "LIMIT 1";
    }

    private static void rollbackOptionalWrite(Connection conn, Savepoint savepoint) {
        if (savepoint == null) {
            return;
        }
        try {
            conn.rollback(savepoint);
        } catch (Exception ignored) {
            // Preserve the original best-effort behavior.
        }
    }
}
