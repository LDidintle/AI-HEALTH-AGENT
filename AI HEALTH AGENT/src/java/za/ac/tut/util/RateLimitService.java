package za.ac.tut.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RateLimitService {

    private static final Map<String, Window> WINDOWS = new ConcurrentHashMap<>();

    private RateLimitService() {
    }

    public static boolean allow(String key, int maxAttempts, long windowMillis) {
        long now = System.currentTimeMillis();
        Window updated = WINDOWS.compute(key, (ignored, existing) -> {
            if (existing == null || now >= existing.resetAtMillis) {
                return new Window(1, now + windowMillis);
            }
            return new Window(existing.count + 1, existing.resetAtMillis);
        });
        return updated.count <= maxAttempts;
    }

    public static String key(String action, String clientAddress, String account) {
        return clean(action) + ":" + clean(clientAddress) + ":" + clean(account);
    }

    public static void clearForTests() {
        WINDOWS.clear();
    }

    private static String clean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        return value.trim().toLowerCase();
    }

    private static final class Window {
        private final int count;
        private final long resetAtMillis;

        private Window(int count, long resetAtMillis) {
            this.count = count;
            this.resetAtMillis = resetAtMillis;
        }
    }
}
