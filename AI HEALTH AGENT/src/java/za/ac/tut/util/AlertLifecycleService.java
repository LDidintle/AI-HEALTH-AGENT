package za.ac.tut.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class AlertLifecycleService {

    public static final String CREATED = "CREATED";
    public static final String ACKNOWLEDGED = "ACKNOWLEDGED";
    public static final String RESOLVED = "RESOLVED";
    public static final String CANCELLED = "CANCELLED";

    private static final Set<String> STATUSES = new HashSet<>(Arrays.asList(
            CREATED, ACKNOWLEDGED, RESOLVED, CANCELLED
    ));

    private AlertLifecycleService() {
    }

    public static boolean isValidStatus(String status) {
        return status != null && STATUSES.contains(status.trim().toUpperCase());
    }

    public static boolean canTransition(String fromStatus, String toStatus) {
        String from = normalize(fromStatus);
        String to = normalize(toStatus);
        if (!isValidStatus(to)) {
            return false;
        }
        if (from == null) {
            return CREATED.equals(to);
        }
        if (CREATED.equals(from)) {
            return ACKNOWLEDGED.equals(to) || RESOLVED.equals(to) || CANCELLED.equals(to);
        }
        if (ACKNOWLEDGED.equals(from)) {
            return RESOLVED.equals(to) || CANCELLED.equals(to);
        }
        return false;
    }

    public static void transition(Connection conn, int alertId, String toStatus, String actorRole,
            String actorId, String note) throws Exception {
        String normalizedTo = normalize(toStatus);
        String fromStatus = currentStatus(conn, alertId);
        if (!canTransition(fromStatus, normalizedTo)) {
            throw new IllegalArgumentException("Invalid alert lifecycle transition.");
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE emergency_alerts SET lifecycle_status = ? WHERE alert_id = ?")) {
            ps.setString(1, normalizedTo);
            ps.setInt(2, alertId);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO alert_events (alert_id, from_status, to_status, actor_role, actor_id, note) "
                + "VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setInt(1, alertId);
            if (fromStatus == null) {
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(2, fromStatus);
            }
            ps.setString(3, normalizedTo);
            ps.setString(4, actorRole);
            ps.setString(5, actorId);
            ps.setString(6, note);
            ps.executeUpdate();
        }
    }

    private static String currentStatus(Connection conn, int alertId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT lifecycle_status FROM emergency_alerts WHERE alert_id = ?")) {
            ps.setInt(1, alertId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return normalize(rs.getString("lifecycle_status"));
                }
            }
        }
        return null;
    }

    private static String normalize(String status) {
        return status == null || status.trim().isEmpty() ? null : status.trim().toUpperCase();
    }
}
