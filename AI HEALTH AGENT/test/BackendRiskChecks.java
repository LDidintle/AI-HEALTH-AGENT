import za.ac.tut.model.PasswordUtils;
import za.ac.tut.util.ResetOtpVisibility;

public class BackendRiskChecks {

    public static void main(String[] args) {
        verifiesPbkdf2PasswordHash();
        acceptsLegacySha256Hash();
        hidesDemoOtpByDefault();
        showsDemoOtpOnlyWhenExplicitlyEnabled();
        rejectsMalformedStoredHashes();
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

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }
}
