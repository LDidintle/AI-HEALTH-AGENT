package za.ac.tut.util;

public final class JsonUtil {

    private JsonUtil() {
    }

    public static String quote(String value) {
        if (value == null) {
            return "null";
        }

        String escaped = value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        return "\"" + escaped + "\"";
    }
}
