package za.ac.tut.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class PatientContextSettingsService {

    public static final String DEFAULT_SLEEP_START = "22:00";
    public static final String DEFAULT_SLEEP_END = "06:00";

    private PatientContextSettingsService() {
    }

    public static ContextSettings load(Connection conn, int userId) throws Exception {
        String sql = "SELECT sleep_start, sleep_end FROM patient_context_settings WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ContextSettings(rs.getString("sleep_start"), rs.getString("sleep_end"));
                }
            }
        }
        return new ContextSettings(DEFAULT_SLEEP_START, DEFAULT_SLEEP_END);
    }

    public static void save(Connection conn, int userId, String sleepStart, String sleepEnd) throws Exception {
        String normalizedStart = normalizeTime(sleepStart, "sleepStart");
        String normalizedEnd = normalizeTime(sleepEnd, "sleepEnd");

        String updateSql = "UPDATE patient_context_settings "
                + "SET sleep_start = ?, sleep_end = ?, updated_at = CURRENT_TIMESTAMP "
                + "WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setString(1, normalizedStart);
            ps.setString(2, normalizedEnd);
            ps.setInt(3, userId);
            if (ps.executeUpdate() > 0) {
                return;
            }
        }

        String insertSql = "INSERT INTO patient_context_settings "
                + "(user_id, sleep_start, sleep_end, updated_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setInt(1, userId);
            ps.setString(2, normalizedStart);
            ps.setString(3, normalizedEnd);
            ps.executeUpdate();
        }
    }

    public static String normalizeTime(String value, String name) {
        if (value == null || !value.matches("[0-9]{2}:[0-9]{2}")) {
            throw new IllegalArgumentException(name + " must use HH:mm.");
        }
        int hour = Integer.parseInt(value.substring(0, 2));
        int minute = Integer.parseInt(value.substring(3, 5));
        if (hour > 23 || minute > 59) {
            throw new IllegalArgumentException(name + " must be a real 24-hour time.");
        }
        return value;
    }

    public static final class ContextSettings {
        private final String sleepStart;
        private final String sleepEnd;

        public ContextSettings(String sleepStart, String sleepEnd) {
            this.sleepStart = sleepStart;
            this.sleepEnd = sleepEnd;
        }

        public String getSleepStart() {
            return sleepStart;
        }

        public String getSleepEnd() {
            return sleepEnd;
        }
    }
}
