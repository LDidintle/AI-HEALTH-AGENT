package za.ac.tut.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;

public final class AuditEventService {

    private AuditEventService() {
    }

    public static void record(Connection conn, Integer actorUserId, String actorRole, String action,
            String targetType, String targetId, String outcome, String detail, String ipAddress) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO audit_events (actor_user_id, actor_role, action, target_type, target_id, outcome, detail, ip_address) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            if (actorUserId == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, actorUserId);
            }
            ps.setString(2, clean(actorRole, 30));
            ps.setString(3, clean(action, 80));
            ps.setString(4, clean(targetType, 80));
            ps.setString(5, clean(targetId, 80));
            ps.setString(6, clean(outcome, 30));
            ps.setString(7, clean(detail, 500));
            ps.setString(8, clean(ipAddress, 80));
            ps.executeUpdate();
        } catch (Exception ignored) {
            // Audit must never break the user-facing health workflow.
        }
    }

    private static String clean(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String cleaned = value.replace('\n', ' ').replace('\r', ' ').trim();
        return cleaned.length() <= maxLength ? cleaned : cleaned.substring(0, maxLength);
    }
}
