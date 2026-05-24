import za.ac.tut.model.PasswordUtils;
import za.ac.tut.model.ReportCriteria;
import za.ac.tut.util.AuthUtil;
import za.ac.tut.util.HealthRiskPredictionService;
import za.ac.tut.util.PasswordPolicy;
import za.ac.tut.util.PatientValidation;
import za.ac.tut.util.RateLimitService;
import za.ac.tut.util.ReportService;
import za.ac.tut.util.ResetOtpVisibility;
import za.ac.tut.util.VitalAlertEvaluator;
import za.ac.tut.util.WatchTemperaturePolicy;
import za.ac.tut.util.AlertLifecycleService;
import za.ac.tut.util.DeviceCapabilityService;
import za.ac.tut.util.RoleAccessPolicy;
import java.math.BigDecimal;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpSession;

public class BackendRiskChecks {

    public static void main(String[] args) {
        verifiesPbkdf2PasswordHash();
        acceptsLegacySha256Hash();
        hidesDemoOtpByDefault();
        showsDemoOtpOnlyWhenExplicitlyEnabled();
        rejectsMalformedStoredHashes();
        predictsLowRiskForNormalVitals();
        predictsHighRiskForHighBloodPressure();
        predictsUrgentRiskForEmergencyBloodPressure();
        predictsElevatedRiskForFastPulseWithFever();
        reportsLimitedDataWhenVitalsAreMissing();
        treatsSamsungSleepTemperatureAsTrendNotCoreTemperature();
        storesSamsungTemperatureAsTrendStatus();
        increasesScoreForRepeatedAbnormalSections();
        marksPredictionAsRuleBasedScreening();
        marksAuthRolesAndSessionTimeouts();
        validatesPatientPhoneIdAndBirthDate();
        enforcesPasswordPolicy();
        rateLimitsRepeatedSensitiveActions();
        classifiesEmergencyAlertDecisions();
        normalizesReportTypesByRole();
        enforcesRoleAccessMatrix();
        validatesAlertLifecycleTransitions();
        explainsSamsungDeviceCapabilities();
        System.out.println("Backend risk checks passed.");
    }

    private static void verifiesPbkdf2PasswordHash() {
        String hash = PasswordUtils.hashPassword("StrongPass22!");
        assertTrue(hash.startsWith("pbkdf2_sha256$"), "new hashes must use PBKDF2");
        assertTrue(PasswordUtils.verifyPassword("StrongPass22!", hash), "PBKDF2 hash must verify");
        assertFalse(PasswordUtils.verifyPassword("WrongPass22!", hash), "wrong password must not verify");
    }

    private static void acceptsLegacySha256Hash() {
        String legacyHash = "87620dfa4341eb12297901bfbb41857d6e88280e6519c12aa2346ce1bebe32b9";
        assertTrue(PasswordUtils.verifyPassword("Patient@12345", legacyHash), "legacy SHA-256 demo hash must verify");
        assertFalse(PasswordUtils.verifyPassword("wrong", legacyHash), "wrong password must not verify legacy SHA-256");
    }

    private static void hidesDemoOtpByDefault() {
        assertFalse(ResetOtpVisibility.isDemoOtpVisible(null, null), "demo OTP must be hidden by default");
        assertFalse(ResetOtpVisibility.isDemoOtpVisible("", ""), "blank demo OTP config must be hidden");
        assertFalse(ResetOtpVisibility.isDemoOtpVisible("false", "true"), "system property must override env false");
    }

    private static void showsDemoOtpOnlyWhenExplicitlyEnabled() {
        assertTrue(ResetOtpVisibility.isDemoOtpVisible("true", null), "property true must show demo OTP");
        assertTrue(ResetOtpVisibility.isDemoOtpVisible(null, "true"), "environment true must show demo OTP");
        assertFalse(ResetOtpVisibility.isDemoOtpVisible("yes", null), "only boolean true must show demo OTP");
    }

    private static void rejectsMalformedStoredHashes() {
        assertFalse(PasswordUtils.verifyPassword("StrongPass22!", ""), "blank hash must not verify");
        assertFalse(PasswordUtils.verifyPassword("StrongPass22!", "pbkdf2_sha256$bad$hash"), "malformed PBKDF2 hash must not verify");
    }

    private static void predictsLowRiskForNormalVitals() {
        HealthRiskPredictionService.PredictionResult result = HealthRiskPredictionService.predict(
                new HealthRiskPredictionService.VitalSnapshot(76, new BigDecimal("36.70"), 118, 76),
                0
        );
        assertEquals(HealthRiskPredictionService.RiskLevel.LOW, result.getRiskLevel(), "normal vitals should be low risk");
    }

    private static void predictsHighRiskForHighBloodPressure() {
        HealthRiskPredictionService.PredictionResult result = HealthRiskPredictionService.predict(
                new HealthRiskPredictionService.VitalSnapshot(82, new BigDecimal("36.80"), 154, 96),
                1
        );
        assertEquals(HealthRiskPredictionService.RiskLevel.HIGH, result.getRiskLevel(), "high BP should be high risk");
    }

    private static void predictsUrgentRiskForEmergencyBloodPressure() {
        HealthRiskPredictionService.PredictionResult result = HealthRiskPredictionService.predict(
                new HealthRiskPredictionService.VitalSnapshot(94, new BigDecimal("37.10"), 184, 122),
                1
        );
        assertEquals(HealthRiskPredictionService.RiskLevel.URGENT, result.getRiskLevel(), "emergency BP should be urgent risk");
    }

    private static void predictsElevatedRiskForFastPulseWithFever() {
        HealthRiskPredictionService.PredictionResult result = HealthRiskPredictionService.predict(
                new HealthRiskPredictionService.VitalSnapshot(112, new BigDecimal("38.40"), 126, 82),
                1
        );
        assertTrue(result.getScore() >= 50, "fast pulse with fever should elevate the score");
    }

    private static void reportsLimitedDataWhenVitalsAreMissing() {
        HealthRiskPredictionService.PredictionResult result = HealthRiskPredictionService.predict(
                new HealthRiskPredictionService.VitalSnapshot(84, null, null, null),
                0
        );
        assertEquals(HealthRiskPredictionService.DataQuality.LIMITED_DATA, result.getDataQuality(), "missing BP/temp should report limited data");
    }

    private static void treatsSamsungSleepTemperatureAsTrendNotCoreTemperature() {
        HealthRiskPredictionService.PredictionResult result = HealthRiskPredictionService.predict(
                new HealthRiskPredictionService.VitalSnapshot(82, new BigDecimal("34.10"), 99, 58, true),
                0
        );
        assertFalse(result.getReasons().contains("Temperature is dangerously low."),
                "Samsung sleep temperature must not be interpreted as dangerously low core temperature");
        assertTrue(result.getReasons().contains("Samsung watch temperature is treated as a sleep-temperature trend and is not scored as core body temperature."),
                "Samsung sleep temperature should be explained as trend-only context");
        assertTrue(result.getScore() < 75, "sleep-temperature trend should not force an urgent score");
        assertEquals(HealthRiskPredictionService.DataQuality.LIMITED_DATA, result.getDataQuality(),
                "trend-only temperature should not count as a clinical body-temperature reading");
    }

    private static void storesSamsungTemperatureAsTrendStatus() {
        assertEquals("TREND",
                WatchTemperaturePolicy.statusFor("SAMSUNG_HEALTH_DATA", new BigDecimal("34.10")),
                "Samsung sleep temperature should be stored as a trend rather than a low core reading");
        assertTrue(WatchTemperaturePolicy.temperatureForAlertEvaluation(
                "SAMSUNG_HEALTH_DATA", new BigDecimal("39.10")) == null,
                "Samsung sleep temperature should not trigger core-temperature emergency alerts");
    }

    private static void increasesScoreForRepeatedAbnormalSections() {
        HealthRiskPredictionService.PredictionResult isolated = HealthRiskPredictionService.predict(
                new HealthRiskPredictionService.VitalSnapshot(104, new BigDecimal("36.80"), 122, 78),
                1
        );
        HealthRiskPredictionService.PredictionResult repeated = HealthRiskPredictionService.predict(
                new HealthRiskPredictionService.VitalSnapshot(104, new BigDecimal("36.80"), 122, 78),
                3
        );
        assertTrue(repeated.getScore() > isolated.getScore(), "repeated abnormal sections should increase score");
    }

    private static void marksPredictionAsRuleBasedScreening() {
        String json = HealthRiskPredictionService.toJson(HealthRiskPredictionService.predict(
                new HealthRiskPredictionService.VitalSnapshot(76, new BigDecimal("36.70"), 118, 76),
                0
        ));
        assertTrue(json.contains("\"modelType\":\"RULE_BASED_SCREENING_V1\""), "prediction JSON must identify the rule-based model");
        assertTrue(json.contains("not a diagnosis"), "prediction JSON must include diagnostic disclaimer");
        assertTrue(json.contains("not a trained machine-learning model"), "prediction JSON must not imply trained ML");
    }

    private static void marksAuthRolesAndSessionTimeouts() {
        HttpSession patientSession = session();
        AuthUtil.markPatient(patientSession, "patient@example.com", 42);
        assertTrue(AuthUtil.isPatient(patientSession), "patient session should be recognized");
        assertEquals(AuthUtil.ROLE_PATIENT, AuthUtil.currentRole(patientSession), "patient role should be current");
        assertEquals(AuthUtil.SESSION_TIMEOUT_SECONDS, patientSession.getMaxInactiveInterval(), "patient session timeout should be set");

        HttpSession adminSession = session();
        AuthUtil.markAdmin(adminSession);
        assertTrue(AuthUtil.isAdmin(adminSession), "admin session should be recognized");
        assertEquals(AuthUtil.ROLE_ADMIN, AuthUtil.currentRole(adminSession), "admin role should be current");
    }

    private static void validatesPatientPhoneIdAndBirthDate() {
        assertTrue(PatientValidation.isValidSouthAfricanPhone("+27 72 123 4567"), "SA mobile number should normalize and validate");
        assertFalse(PatientValidation.isValidSouthAfricanPhone("0211234567"), "landline-like number should not validate as mobile");
        assertTrue(PatientValidation.samePhone("0721234567", "+27 72 123 4567"), "same phone should compare after normalization");
        assertTrue(PatientValidation.isValidIdNumber("9901015009087"), "13 digit SA ID shape should validate");
        assertFalse(PatientValidation.isValidIdNumber("990101500908X"), "non-numeric SA ID should fail");
        assertTrue(PatientValidation.isValidDateOfBirth("2000-01-01"), "past date of birth should validate");
        assertFalse(PatientValidation.isValidDateOfBirth("2999-01-01"), "future date of birth should fail");
    }

    private static void enforcesPasswordPolicy() {
        assertTrue(PasswordPolicy.isStrongPassword("Strong22!"), "strong password should pass");
        assertFalse(PasswordPolicy.isStrongPassword("Strong2!"), "one digit should fail");
        assertFalse(PasswordPolicy.isStrongPassword("strong22!"), "missing uppercase should fail");
        assertFalse(PasswordPolicy.isStrongPassword("Strong22"), "missing special character should fail");
    }

    private static void rateLimitsRepeatedSensitiveActions() {
        RateLimitService.clearForTests();
        String key = RateLimitService.key("login", "127.0.0.1", "patient@example.com");
        assertTrue(RateLimitService.allow(key, 2, 60_000), "first attempt should pass");
        assertTrue(RateLimitService.allow(key, 2, 60_000), "second attempt should pass");
        assertFalse(RateLimitService.allow(key, 2, 60_000), "third attempt should be rate limited");
    }

    private static void classifiesEmergencyAlertDecisions() {
        VitalAlertEvaluator.AlertDecision emergencyBp = VitalAlertEvaluator.assess(null, null, 184, 122);
        assertEquals("CRITICAL", emergencyBp.getStatus(), "emergency BP should be critical");
        assertEquals(30, emergencyBp.getCountdownSeconds(), "critical alerts should use short countdown");

        VitalAlertEvaluator.AlertDecision feverAndFastPulse = VitalAlertEvaluator.assess(
                112, new BigDecimal("38.50"), 120, 80);
        assertEquals("WARNING", feverAndFastPulse.getStatus(), "fast pulse plus fever should warn");
        assertTrue(VitalAlertEvaluator.assess(76, new BigDecimal("36.70"), 118, 76) == null, "normal vitals should not alert");
    }

    private static void normalizesReportTypesByRole() {
        assertEquals(ReportService.REPORT_MANAGEMENT, ReportService.normalizeReportType(null, false), "blank admin report should default to management");
        assertEquals(ReportService.REPORT_ALERTS, ReportService.normalizeReportType("unknown", true), "hospital report should be alerts-only");
        assertEquals(ReportService.REPORT_VITALS, ReportService.normalizeReportType(ReportService.REPORT_VITALS, false), "admin vitals report should be allowed");

        ReportCriteria criteria = new ReportCriteria();
        criteria.setHospital(true);
        criteria.setReportType(ReportService.REPORT_MANAGEMENT);
        assertEquals(ReportService.REPORT_ALERTS, ReportService.normalizeReportType(criteria.getReportType(), criteria.isHospital()), "hospital criteria should normalize to alerts");
    }

    private static void enforcesRoleAccessMatrix() {
        assertTrue(RoleAccessPolicy.isAllowed(AuthUtil.ROLE_PATIENT, "/api/mobile/health-sync", "GET"), "patient mobile read should be allowed");
        assertTrue(RoleAccessPolicy.isAllowed(AuthUtil.ROLE_PATIENT, "/api/mobile/health-section-sync", "POST"), "patient mobile sync should be allowed");
        assertFalse(RoleAccessPolicy.isAllowed(AuthUtil.ROLE_PATIENT, "/ReportsServlet.do", "GET"), "patient must not access staff reports");
        assertTrue(RoleAccessPolicy.isAllowed(AuthUtil.ROLE_ADMIN, "/ReportsServlet.do", "GET"), "admin should access reports");
        assertTrue(RoleAccessPolicy.isAllowed(AuthUtil.ROLE_HOSPITAL, "/HospitalPatientsServlet.do", "GET"), "hospital should access hospital patients");
        assertFalse(RoleAccessPolicy.isAllowed(AuthUtil.ROLE_HOSPITAL, "/ViewUsersServlet.do", "GET"), "hospital must not access staff CRUD");
        assertTrue(RoleAccessPolicy.isPublic("/health"), "health check should be public");
    }

    private static void validatesAlertLifecycleTransitions() {
        assertTrue(AlertLifecycleService.isValidStatus("CREATED"), "created should be a valid lifecycle status");
        assertTrue(AlertLifecycleService.canTransition("CREATED", "ACKNOWLEDGED"), "created alert can be acknowledged");
        assertTrue(AlertLifecycleService.canTransition("ACKNOWLEDGED", "RESOLVED"), "acknowledged alert can be resolved");
        assertTrue(AlertLifecycleService.canTransition("CREATED", "CANCELLED"), "created alert can be cancelled");
        assertFalse(AlertLifecycleService.canTransition("RESOLVED", "ACKNOWLEDGED"), "resolved alert must not reopen silently");
    }

    private static void explainsSamsungDeviceCapabilities() {
        DeviceCapabilityService.Capabilities samsung = DeviceCapabilityService.forSource("SAMSUNG_HEALTH_DATA");
        assertTrue(samsung.isHeartRateSupported(), "Samsung section should support heart rate when available");
        assertTrue(samsung.isBloodPressureSupported(), "Samsung section should describe BP as supported when calibrated/source exposes it");
        assertTrue(samsung.isSleepTemperatureTrendOnly(), "Samsung temperature should be sleep trend only");
        assertTrue(samsung.toJson().contains("calibration/source dependent"), "BP caveat should be visible in JSON");
    }

    private static HttpSession session() {
        Map<String, Object> attributes = new HashMap<>();
        int[] timeout = new int[] { -1 };
        return (HttpSession) Proxy.newProxyInstance(
                BackendRiskChecks.class.getClassLoader(),
                new Class<?>[] { HttpSession.class },
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("setAttribute".equals(name)) {
                        attributes.put((String) args[0], args[1]);
                        return null;
                    }
                    if ("getAttribute".equals(name)) {
                        return attributes.get((String) args[0]);
                    }
                    if ("setMaxInactiveInterval".equals(name)) {
                        timeout[0] = (Integer) args[0];
                        return null;
                    }
                    if ("getMaxInactiveInterval".equals(name)) {
                        return timeout[0];
                    }
                    if ("toString".equals(name)) {
                        return "BackendRiskChecksSession";
                    }
                    return null;
                });
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null) {
            if (actual != null) {
                throw new AssertionError(message + " expected=null actual=" + actual);
            }
            return;
        }
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " expected=" + expected + " actual=" + actual);
        }
    }
}
