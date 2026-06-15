package za.ac.tut.util;

public final class HospitalAlertStatusService {

    public static final String ASSIGNED = "ASSIGNED";
    public static final String ONGOING = "ONGOING";
    public static final String RESOLVED = "RESOLVED";
    public static final String REMOVED = "REMOVED";

    private HospitalAlertStatusService() {
    }

    public static String statusForAction(String action) {
        if (action == null) {
            return null;
        }
        switch (action.trim().toLowerCase()) {
            case "not_solved":
                return ASSIGNED;
            case "ongoing":
                return ONGOING;
            case "resolved":
                return RESOLVED;
            case "remove":
                return REMOVED;
            default:
                return null;
        }
    }

    public static boolean isVisibleInHospitalPortal(String status) {
        return !REMOVED.equalsIgnoreCase(clean(status));
    }

    public static String displayStatus(String status) {
        String clean = clean(status);
        if (clean == null || ASSIGNED.equalsIgnoreCase(clean)) {
            return "Not solved";
        }
        if (ONGOING.equalsIgnoreCase(clean)) {
            return "Ongoing";
        }
        if (RESOLVED.equalsIgnoreCase(clean)) {
            return "Resolved";
        }
        if (REMOVED.equalsIgnoreCase(clean)) {
            return "Removed";
        }
        return clean;
    }

    private static String clean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
