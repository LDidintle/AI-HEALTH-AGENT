package za.ac.tut.util;

import javax.servlet.http.HttpSession;

public final class MobileSessionPolicy {

    public static final int MOBILE_SESSION_TIMEOUT_SECONDS = 30 * 24 * 60 * 60;

    private MobileSessionPolicy() {
    }

    public static void apply(HttpSession session) {
        if (session != null) {
            session.setMaxInactiveInterval(MOBILE_SESSION_TIMEOUT_SECONDS);
        }
    }
}
