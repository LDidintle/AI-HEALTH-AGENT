import za.ac.tut.model.PasswordUtils;
import za.ac.tut.util.HealthRiskPredictionService;
import za.ac.tut.util.ResetOtpVisibility;
import java.math.BigDecimal;

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
        increasesScoreForRepeatedAbnormalSections();
        marksPredictionAsRuleBasedScreening();
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

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " expected=" + expected + " actual=" + actual);
        }
    }
}
