package za.ac.tut.util;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public final class PatientAccountProcedureService {

    private PatientAccountProcedureService() {
    }

    public static int createPatientAccount(Connection conn, String title, String firstName, String surname,
            Date dateOfBirth, String email, String passwordHash) throws SQLException {
        try {
            return callCreatePatientProcedure(conn, title, firstName, surname, dateOfBirth, email, passwordHash);
        } catch (SQLException procedureError) {
            if (!isMissingProcedureError(procedureError)) {
                throw procedureError;
            }
            return insertPatientDirectly(conn, title, firstName, surname, dateOfBirth, email, passwordHash);
        }
    }

    public static void deletePatientAccount(Connection conn, int userId) throws SQLException {
        try {
            callDeletePatientProcedure(conn, userId);
        } catch (SQLException procedureError) {
            if (!isMissingProcedureError(procedureError)) {
                throw procedureError;
            }
            deletePatientDirectly(conn, userId);
        }
    }

    private static int callCreatePatientProcedure(Connection conn, String title, String firstName, String surname,
            Date dateOfBirth, String email, String passwordHash) throws SQLException {
        if (isPostgreSql()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT sp_register_patient_account(?, ?, ?, ?, ?, ?)")) {
                ps.setString(1, title);
                ps.setString(2, firstName);
                ps.setString(3, surname);
                ps.setDate(4, dateOfBirth);
                ps.setString(5, email);
                ps.setString(6, passwordHash);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
                throw new SQLException("Patient registration routine did not return a user ID.");
            }
        }

        try (CallableStatement cs = conn.prepareCall("{ call sp_register_patient_account(?, ?, ?, ?, ?, ?, ?) }")) {
            cs.setString(1, title);
            cs.setString(2, firstName);
            cs.setString(3, surname);
            cs.setDate(4, dateOfBirth);
            cs.setString(5, email);
            cs.setString(6, passwordHash);
            cs.registerOutParameter(7, Types.INTEGER);
            cs.execute();
            return cs.getInt(7);
        }
    }

    private static void callDeletePatientProcedure(Connection conn, int userId) throws SQLException {
        if (isPostgreSql()) {
            try (PreparedStatement ps = conn.prepareStatement("CALL sp_delete_patient_account(?)")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
                return;
            }
        }

        try (CallableStatement cs = conn.prepareCall("{ call sp_delete_patient_account(?) }")) {
            cs.setInt(1, userId);
            cs.execute();
        }
    }

    private static int insertPatientDirectly(Connection conn, String title, String firstName, String surname,
            Date dateOfBirth, String email, String passwordHash) throws SQLException {
        String sqlUser = "INSERT INTO users "
                + "(title, first_name, surname, dob, email, is_verified) "
                + "VALUES (?, ?, ?, ?, ?, FALSE)";
        String sqlAuth = "INSERT INTO user_auth (user_id, password_hash) VALUES (?, ?)";

        try (PreparedStatement psUser = conn.prepareStatement(sqlUser, PreparedStatement.RETURN_GENERATED_KEYS);
             PreparedStatement psAuth = conn.prepareStatement(sqlAuth)) {
            psUser.setString(1, title);
            psUser.setString(2, firstName);
            psUser.setString(3, surname);
            psUser.setDate(4, dateOfBirth);
            psUser.setString(5, email);

            int rowsInserted = psUser.executeUpdate();
            if (rowsInserted == 0) {
                throw new SQLException("Failed to insert user.");
            }

            int userId;
            try (ResultSet rs = psUser.getGeneratedKeys()) {
                if (rs.next()) {
                    userId = rs.getInt(1);
                } else {
                    throw new SQLException("Failed to get user ID.");
                }
            }

            psAuth.setInt(1, userId);
            psAuth.setString(2, passwordHash);
            psAuth.executeUpdate();
            return userId;
        }
    }

    private static void deletePatientDirectly(Connection conn, int userId) throws SQLException {
        try (PreparedStatement deleteAuthPs = conn.prepareStatement("DELETE FROM user_auth WHERE user_id = ?");
             PreparedStatement deleteUserPs = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            deleteAuthPs.setInt(1, userId);
            deleteAuthPs.executeUpdate();
            deleteUserPs.setInt(1, userId);
            deleteUserPs.executeUpdate();
        }
    }

    private static boolean isPostgreSql() {
        return DatabaseConfig.JDBC_URL.startsWith("jdbc:postgresql://");
    }

    private static boolean isMissingProcedureError(SQLException error) {
        String state = error.getSQLState();
        String message = error.getMessage() == null ? "" : error.getMessage().toLowerCase();
        return "42883".equals(state)
                || "42000".equals(state)
                || "42Y03".equals(state)
                || message.contains("does not exist")
                || message.contains("unknown procedure")
                || message.contains("not recognized as a function or procedure")
                || message.contains("function sp_register_patient_account")
                || message.contains("procedure sp_delete_patient_account");
    }
}
