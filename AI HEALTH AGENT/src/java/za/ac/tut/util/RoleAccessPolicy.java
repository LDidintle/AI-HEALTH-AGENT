package za.ac.tut.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class RoleAccessPolicy {

    private static final Set<String> PUBLIC_PATHS = new HashSet<>(Arrays.asList(
            "/",
            "/index.html",
            "/welcome.html",
            "/user_sign.html",
            "/admin",
            "/admin/",
            "/hospital_sign.jsp",
            "/documents/Health_System_Terms.html",
            "/reset_password.html",
            "/reset_password.jsp",
            "/PasswordResetRequestServlet.do",
            "/PasswordResetConfirmServlet.do",
            "/UserConfirmServlet.do",
            "/AdminServlet.do",
            "/HospitalLoginServlet.do",
            "/HospitalRegisterServlet.do",
            "/SignOutServlet.do",
            "/api/mobile/login",
            "/api/mobile/register",
            "/health"
    ));

    private static final Set<String> ADMIN_PATHS = new HashSet<>(Arrays.asList(
            "/admin_dashboard.jsp",
            "/ViewUsersServlet.do",
            "/DeleteUserServlet.do",
            "/EditUserServlet.do",
            "/UpdateUserServlet.do",
            "/ReadUserServlet.do",
            "/ReportsServlet.do",
            "/read_user.jsp",
            "/list_of_users.jsp",
            "/edit_user.jsp",
            "/read_user_result.jsp"
    ));

    private static final Set<String> HOSPITAL_PATHS = new HashSet<>(Arrays.asList(
            "/HospitalPatientsServlet.do",
            "/HospitalAlertStatusServlet.do",
            "/HospitalPatientDetailsServlet.do",
            "/ReportsServlet.do",
            "/read_user_result.jsp"
    ));

    private RoleAccessPolicy() {
    }

    public static boolean isPublic(String path) {
        return PUBLIC_PATHS.contains(normalize(path)) || normalize(path).startsWith("/images/")
                || normalize(path).endsWith(".css") || normalize(path).endsWith(".js")
                || normalize(path).endsWith(".png") || normalize(path).endsWith(".jpg")
                || normalize(path).endsWith(".svg");
    }

    public static boolean isAllowed(String role, String path, String method) {
        String normalizedPath = normalize(path);
        if (isPublic(normalizedPath)) {
            return true;
        }
        if (AuthUtil.ROLE_PATIENT.equals(role)) {
            return normalizedPath.startsWith("/api/mobile/")
                    || "/healthApp.html".equals(normalizedPath)
                    || "/AIChatServlet.do".equals(normalizedPath)
                    || "/ReadingServlet.do".equals(normalizedPath)
                    || "/CompleteProfileServlet.do".equals(normalizedPath)
                    || "/complete_profile.jsp".equals(normalizedPath);
        }
        if (AuthUtil.ROLE_ADMIN.equals(role)) {
            return ADMIN_PATHS.contains(normalizedPath) || HOSPITAL_PATHS.contains(normalizedPath)
                    || "/AIChatServlet.do".equals(normalizedPath);
        }
        if (AuthUtil.ROLE_HOSPITAL.equals(role)) {
            return HOSPITAL_PATHS.contains(normalizedPath);
        }
        return false;
    }

    public static boolean isAdminArea(String path) {
        String normalizedPath = normalize(path);
        return ADMIN_PATHS.contains(normalizedPath) || normalizedPath.startsWith("/admin");
    }

    public static boolean isHospitalArea(String path) {
        String normalizedPath = normalize(path);
        return HOSPITAL_PATHS.contains(normalizedPath) || normalizedPath.startsWith("/Hospital");
    }

    public static String normalize(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "/";
        }
        String clean = path.trim();
        int queryIndex = clean.indexOf('?');
        if (queryIndex >= 0) {
            clean = clean.substring(0, queryIndex);
        }
        return clean.startsWith("/") ? clean : "/" + clean;
    }
}
