package za.ac.tut.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

public final class DatabaseConfig {

    private static final String RAW_DB_URL = requiredConfig(
            "SMARTHEALTH_DB_URL",
            "SUPABASE_DB_URL"
    );
    public static final String JDBC_URL = normalizeJdbcUrl(RAW_DB_URL);
    public static final String DB_USER = valueOrDefault(configValue(
            "SMARTHEALTH_DB_USER",
            "SUPABASE_DB_USER"
    ), userFromUrl(RAW_DB_URL));
    public static final String DB_PASS = valueOrDefault(configValue(
            "SMARTHEALTH_DB_PASSWORD",
            "SUPABASE_DB_PASSWORD"
    ), passwordFromUrl(RAW_DB_URL));
    public static final String JDBC_DRIVER = valueOrDefault(configValue(
            "SMARTHEALTH_DB_DRIVER",
            "SUPABASE_DB_DRIVER"
    ), driverFor(JDBC_URL));

    private DatabaseConfig() {
    }

    private static String configValue(String primaryName, String fallbackName) {
        String value = System.getProperty(primaryName);
        if (isBlank(value)) {
            value = System.getenv(primaryName);
        }
        if (isBlank(value)) {
            value = System.getProperty(fallbackName);
        }
        if (isBlank(value)) {
            value = System.getenv(fallbackName);
        }
        return value;
    }

    private static String requiredConfig(String primaryName, String fallbackName) {
        String value = configValue(primaryName, fallbackName);
        if (isBlank(value)) {
            throw new IllegalStateException(primaryName + " or " + fallbackName
                    + " must be configured with a JDBC database URL.");
        }
        return value;
    }

    private static String valueOrDefault(String value, String fallback) {
        if (isBlank(value)) {
            return fallback;
        }
        return value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean hasSeparateCredentials() {
        return !isBlank(DB_USER) || !isBlank(DB_PASS);
    }

    public static String dbUserOrEmpty() {
        return valueOrDefault(DB_USER, "");
    }

    public static String dbPassOrEmpty() {
        return valueOrDefault(DB_PASS, "");
    }

    private static String normalizeJdbcUrl(String value) {
        String jdbcUrl = value.trim().replace("\\:", ":");
        if (jdbcUrl.startsWith("postgres://") || jdbcUrl.startsWith("postgresql://")) {
            jdbcUrl = postgresUriToJdbcUrl(jdbcUrl);
        }

        if (jdbcUrl.startsWith("jdbc:postgresql://")
                && jdbcUrl.substring("jdbc:postgresql://".length()).contains("@")) {
            jdbcUrl = postgresUriToJdbcUrl("postgresql://"
                    + jdbcUrl.substring("jdbc:postgresql://".length()));
        }

        if (jdbcUrl.startsWith("postgres://")) {
            jdbcUrl = "jdbc:postgresql://" + jdbcUrl.substring("postgres://".length());
        } else if (jdbcUrl.startsWith("postgresql://")) {
            jdbcUrl = "jdbc:postgresql://" + jdbcUrl.substring("postgresql://".length());
        }

        if (jdbcUrl.startsWith("jdbc:postgresql://")
                && jdbcUrl.contains("supabase.")
                && !jdbcUrl.contains("sslmode=")) {
            jdbcUrl += jdbcUrl.contains("?") ? "&sslmode=require" : "?sslmode=require";
        }

        return jdbcUrl;
    }

    private static String postgresUriToJdbcUrl(String value) {
        try {
            URI uri = new URI(value);
            StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
            jdbcUrl.append(uri.getHost());

            if (uri.getPort() > 0) {
                jdbcUrl.append(":").append(uri.getPort());
            }

            jdbcUrl.append(valueOrDefault(uri.getRawPath(), "/postgres"));

            if (!isBlank(uri.getRawQuery())) {
                jdbcUrl.append("?").append(uri.getRawQuery());
            }

            return jdbcUrl.toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid PostgreSQL database URL.", e);
        }
    }

    private static String userFromUrl(String value) {
        String userInfo = userInfoFromUrl(value);
        if (isBlank(userInfo)) {
            return "";
        }

        int separator = userInfo.indexOf(':');
        return decodeUrlPart(separator >= 0 ? userInfo.substring(0, separator) : userInfo);
    }

    private static String passwordFromUrl(String value) {
        String userInfo = userInfoFromUrl(value);
        if (isBlank(userInfo)) {
            return "";
        }

        int separator = userInfo.indexOf(':');
        return separator >= 0 ? decodeUrlPart(userInfo.substring(separator + 1)) : "";
    }

    private static String userInfoFromUrl(String value) {
        try {
            String normalized = value.trim().replace("\\:", ":");
            if (normalized.startsWith("jdbc:postgresql://")) {
                normalized = "postgresql://" + normalized.substring("jdbc:postgresql://".length());
            }
            if (!normalized.startsWith("postgres://") && !normalized.startsWith("postgresql://")) {
                return "";
            }
            return valueOrDefault(new URI(normalized).getRawUserInfo(), "");
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid PostgreSQL database URL.", e);
        }
    }

    private static String decodeUrlPart(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 is not supported by this Java runtime.", e);
        }
    }

    private static String driverFor(String jdbcUrl) {
        if (jdbcUrl.startsWith("jdbc:postgresql://")) {
            return "org.postgresql.Driver";
        }
        if (jdbcUrl.startsWith("jdbc:mariadb://")) {
            return "org.mariadb.jdbc.Driver";
        }
        throw new IllegalStateException("Unsupported JDBC URL. Expected PostgreSQL or MariaDB.");
    }
}
