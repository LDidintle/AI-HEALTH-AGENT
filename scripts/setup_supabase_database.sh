#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${1:-$ROOT_DIR/.env}"
SCHEMA_FILE="$ROOT_DIR/database/supabase_schema.sql"
POSTGRES_JAR="$ROOT_DIR/AI HEALTH AGENT/web/WEB-INF/lib/postgresql-42.7.11.jar"
JAVA_BIN="${JAVA_BIN:-/Library/Java/JavaVirtualMachines/temurin-8.jdk/Contents/Home/bin/java}"
JAVAC_BIN="${JAVAC_BIN:-/Library/Java/JavaVirtualMachines/temurin-8.jdk/Contents/Home/bin/javac}"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Missing env file: $ENV_FILE" >&2
  exit 1
fi

if [[ ! -f "$SCHEMA_FILE" ]]; then
  echo "Missing schema file: $SCHEMA_FILE" >&2
  exit 1
fi

if [[ ! -f "$POSTGRES_JAR" ]]; then
  echo "Missing PostgreSQL JDBC driver: $POSTGRES_JAR" >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

required=(
  SMARTHEALTH_DB_URL
  SMARTHEALTH_DB_USER
  SMARTHEALTH_DB_PASSWORD
)

for key in "${required[@]}"; do
  if [[ -z "${!key:-}" ]]; then
    echo "Missing required setting in $ENV_FILE: $key" >&2
    exit 1
  fi
done

WORK_DIR="$(mktemp -d)"
trap 'rm -rf "$WORK_DIR"' EXIT

cat > "$WORK_DIR/SetupSupabaseDatabase.java" <<'JAVA'
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SetupSupabaseDatabase {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: SetupSupabaseDatabase <schema.sql>");
        }

        Class.forName("org.postgresql.Driver");

        String url = requiredEnv("SMARTHEALTH_DB_URL");
        String user = requiredEnv("SMARTHEALTH_DB_USER");
        String password = requiredEnv("SMARTHEALTH_DB_PASSWORD");

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(false);
            applySchema(conn, args[0]);
            seedDemoPatient(conn);
            conn.commit();
        }

        System.out.println("Supabase schema is ready and demo patient exists.");
    }

    private static void applySchema(Connection conn, String schemaPath) throws IOException, SQLException {
        String schema = new String(Files.readAllBytes(Paths.get(schemaPath)), StandardCharsets.UTF_8);
        for (String statement : schema.split(";")) {
            String sql = statement.trim();
            if (sql.isEmpty()) {
                continue;
            }

            try (Statement st = conn.createStatement()) {
                st.execute(sql);
            }
        }
    }

    private static void seedDemoPatient(Connection conn) throws Exception {
        int userId = findUserId(conn, "john@gmail.com");
        if (userId == 0) {
            String sql = "INSERT INTO users "
                    + "(title, first_name, surname, dob, gender, marital_status, email, cell_number, address) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, "Mr");
                ps.setString(2, "John");
                ps.setString(3, "Patient");
                ps.setDate(4, Date.valueOf("2000-01-01"));
                ps.setString(5, "Male");
                ps.setString(6, "Single");
                ps.setString(7, "john@gmail.com");
                ps.setString(8, "0000000000");
                ps.setString(9, "Demo address");
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException("Could not read generated user id.");
                    }
                    userId = rs.getInt(1);
                }
            }
        }

        String hash = sha256Hex("test123");
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE user_auth SET password_hash = ? WHERE user_id = ?")) {
            ps.setString(1, hash);
            ps.setInt(2, userId);
            if (ps.executeUpdate() > 0) {
                return;
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO user_auth (user_id, password_hash) VALUES (?, ?)")) {
            ps.setInt(1, userId);
            ps.setString(2, hash);
            ps.executeUpdate();
        }
    }

    private static int findUserId(Connection conn, String email) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("id") : 0;
            }
        }
    }

    private static String sha256Hex(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(name + " is required.");
        }
        return value.trim();
    }
}
JAVA

"$JAVAC_BIN" -cp "$POSTGRES_JAR" "$WORK_DIR/SetupSupabaseDatabase.java"
"$JAVA_BIN" -cp "$WORK_DIR:$POSTGRES_JAR" SetupSupabaseDatabase "$SCHEMA_FILE"
