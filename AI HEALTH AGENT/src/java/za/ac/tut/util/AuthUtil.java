package za.ac.tut.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public final class AuthUtil {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_HOSPITAL = "HOSPITAL";
    public static final String ROLE_PATIENT = "PATIENT";

    private AuthUtil() {
    }

    public static void markAdmin(HttpSession session) {
        session.setAttribute("admin", "true");
        session.setAttribute("role", ROLE_ADMIN);
    }

    public static void markHospital(HttpSession session) {
        session.setAttribute("hospital", "true");
        session.setAttribute("role", ROLE_HOSPITAL);
    }

    public static void markPatient(HttpSession session, String email, int userId) {
        session.setAttribute("user", email);
        session.setAttribute("userId", userId);
        session.setAttribute("role", ROLE_PATIENT);
    }

    public static boolean isAdmin(HttpServletRequest request) {
        return isAdmin(request.getSession(false));
    }

    public static boolean isAdmin(HttpSession session) {
        return session != null
                && (ROLE_ADMIN.equals(session.getAttribute("role"))
                || "true".equals(String.valueOf(session.getAttribute("admin"))));
    }

    public static boolean isHospital(HttpServletRequest request) {
        return isHospital(request.getSession(false));
    }

    public static boolean isHospital(HttpSession session) {
        return session != null
                && (ROLE_HOSPITAL.equals(session.getAttribute("role"))
                || "true".equals(String.valueOf(session.getAttribute("hospital"))));
    }

    public static boolean isPatient(HttpServletRequest request) {
        return isPatient(request.getSession(false));
    }

    public static boolean isPatient(HttpSession session) {
        return session != null
                && (ROLE_PATIENT.equals(session.getAttribute("role"))
                || session.getAttribute("userId") instanceof Integer);
    }

    public static String currentRole(HttpSession session) {
        if (isAdmin(session)) {
            return ROLE_ADMIN;
        }
        if (isHospital(session)) {
            return ROLE_HOSPITAL;
        }
        if (isPatient(session)) {
            return ROLE_PATIENT;
        }
        return "ANONYMOUS";
    }
}
