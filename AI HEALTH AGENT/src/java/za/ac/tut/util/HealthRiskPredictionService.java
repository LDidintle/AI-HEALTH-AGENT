package za.ac.tut.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public final class HealthRiskPredictionService {

    public static final String MODEL_TYPE = "RULE_BASED_SCREENING_V1";
    public static final String DIAGNOSTIC_DISCLAIMER =
            "This is a rule-based screening score, not a diagnosis and not a trained machine-learning model.";

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }

    public enum DataQuality {
        ENOUGH_DATA,
        LIMITED_DATA,
        MISSING_VITALS
    }

    private HealthRiskPredictionService() {
    }

    public static PredictionResult predictForUser(Connection conn, int userId) throws Exception {
        List<VitalSection> sections = loadRecentSections(conn, userId);
        if (sections.isEmpty()) {
            return predict(VitalSnapshot.empty(), 0);
        }

        VitalSection latest = sections.get(0);
        VitalSnapshot snapshot = new VitalSnapshot(
                latest.heartRate,
                latest.temperature,
                latest.systolic,
                latest.diastolic
        );
        return predict(snapshot, countAbnormalSections(sections));
    }

    public static PredictionResult predict(VitalSnapshot snapshot, int recentAbnormalSections) {
        if (!snapshot.hasAnyReading()) {
            return new PredictionResult(
                    RiskLevel.LOW,
                    0,
                    "No synced vitals are available yet.",
                    "Sync Samsung Health or Health Connect readings before using this screening score.",
                    DataQuality.MISSING_VITALS,
                    list("No heart rate, temperature, or blood pressure reading was available.")
            );
        }

        int score = 10;
        List<String> reasons = new ArrayList<>();

        if (snapshot.heartRate == null) {
            reasons.add("Heart rate is missing.");
        } else if (snapshot.heartRate < 45) {
            score += 45;
            reasons.add("Heart rate is severely low.");
        } else if (snapshot.heartRate < 60) {
            score += 18;
            reasons.add("Heart rate is below the common adult resting range.");
        } else if (snapshot.heartRate > 130) {
            score += 42;
            reasons.add("Heart rate is severely elevated.");
        } else if (snapshot.heartRate > 100) {
            score += 20;
            reasons.add("Heart rate is elevated for a resting reading.");
        }

        if (snapshot.temperature == null) {
            reasons.add("Temperature is missing.");
        } else if (snapshot.temperature.compareTo(new BigDecimal("39.0")) >= 0) {
            score += 35;
            reasons.add("Temperature is very high.");
        } else if (snapshot.temperature.compareTo(new BigDecimal("38.0")) >= 0) {
            score += 20;
            reasons.add("Temperature is raised.");
        } else if (snapshot.temperature.compareTo(new BigDecimal("35.0")) < 0) {
            score += 38;
            reasons.add("Temperature is dangerously low.");
        }

        if (snapshot.systolic == null || snapshot.diastolic == null) {
            reasons.add("Blood pressure is missing.");
        } else if (snapshot.systolic >= 180 || snapshot.diastolic >= 120) {
            score += 65;
            reasons.add("Blood pressure is in an emergency range.");
        } else if (snapshot.systolic >= 140 || snapshot.diastolic >= 90) {
            score += 40;
            reasons.add("Blood pressure is high.");
        } else if (snapshot.systolic < 90 || snapshot.diastolic < 60) {
            score += 24;
            reasons.add("Blood pressure is low.");
        }

        if (snapshot.heartRate != null && snapshot.temperature != null
                && snapshot.heartRate > 100
                && snapshot.temperature.compareTo(new BigDecimal("38.0")) >= 0) {
            score += 18;
            reasons.add("Fast pulse and raised temperature appear together.");
        }

        if (recentAbnormalSections >= 3) {
            score += 15;
            reasons.add("Recent synced sections show repeated abnormal readings.");
        } else if (recentAbnormalSections >= 2) {
            score += 8;
            reasons.add("More than one recent section is abnormal.");
        }

        if (reasons.isEmpty()) {
            reasons.add("Latest readings are within the app's simple screening ranges.");
        }

        score = Math.max(0, Math.min(100, score));
        RiskLevel level = riskLevel(score);
        return new PredictionResult(
                level,
                score,
                summary(level),
                recommendedAction(level),
                dataQuality(snapshot),
                reasons
        );
    }

    public static String toJson(PredictionResult prediction) {
        StringBuilder reasonsJson = new StringBuilder("[");
        for (int i = 0; i < prediction.reasons.size(); i++) {
            if (i > 0) {
                reasonsJson.append(",");
            }
            reasonsJson.append(JsonUtil.quote(prediction.reasons.get(i)));
        }
        reasonsJson.append("]");

        return "{"
                + "\"modelType\":" + JsonUtil.quote(MODEL_TYPE) + ","
                + "\"riskLevel\":" + JsonUtil.quote(prediction.riskLevel.name()) + ","
                + "\"score\":" + prediction.score + ","
                + "\"summary\":" + JsonUtil.quote(prediction.summary) + ","
                + "\"reasons\":" + reasonsJson + ","
                + "\"recommendedAction\":" + JsonUtil.quote(prediction.recommendedAction) + ","
                + "\"dataQuality\":" + JsonUtil.quote(prediction.dataQuality.name()) + ","
                + "\"diagnosticDisclaimer\":" + JsonUtil.quote(DIAGNOSTIC_DISCLAIMER)
                + "}";
    }

    private static List<VitalSection> loadRecentSections(Connection conn, int userId) throws Exception {
        String sql = "SELECT heart_rate_latest, temperature_latest, systolic_latest, diastolic_latest "
                + "FROM health_sync_sections WHERE user_id = ? "
                + "ORDER BY window_end DESC, section_id DESC LIMIT 6";
        List<VitalSection> sections = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sections.add(new VitalSection(
                            nullableInteger(rs, "heart_rate_latest"),
                            rs.getBigDecimal("temperature_latest"),
                            nullableInteger(rs, "systolic_latest"),
                            nullableInteger(rs, "diastolic_latest")
                    ));
                }
            }
        }
        return sections;
    }

    private static int countAbnormalSections(List<VitalSection> sections) {
        int count = 0;
        for (VitalSection section : sections) {
            if (isAbnormal(section)) {
                count++;
            }
        }
        return count;
    }

    private static boolean isAbnormal(VitalSection section) {
        return (section.heartRate != null && (section.heartRate < 60 || section.heartRate > 100))
                || (section.temperature != null
                && (section.temperature.compareTo(new BigDecimal("38.0")) >= 0
                || section.temperature.compareTo(new BigDecimal("35.0")) < 0))
                || (section.systolic != null && section.diastolic != null
                && (section.systolic >= 140 || section.diastolic >= 90
                || section.systolic < 90 || section.diastolic < 60));
    }

    private static Integer nullableInteger(ResultSet rs, String column) throws Exception {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static DataQuality dataQuality(VitalSnapshot snapshot) {
        int present = 0;
        if (snapshot.heartRate != null) {
            present++;
        }
        if (snapshot.temperature != null) {
            present++;
        }
        if (snapshot.systolic != null && snapshot.diastolic != null) {
            present++;
        }
        if (present == 3) {
            return DataQuality.ENOUGH_DATA;
        }
        return present == 0 ? DataQuality.MISSING_VITALS : DataQuality.LIMITED_DATA;
    }

    private static RiskLevel riskLevel(int score) {
        if (score >= 75) {
            return RiskLevel.URGENT;
        }
        if (score >= 50) {
            return RiskLevel.HIGH;
        }
        if (score >= 25) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    private static String summary(RiskLevel level) {
        switch (level) {
            case URGENT:
                return "Urgent warning pattern detected.";
            case HIGH:
                return "High-risk screening pattern detected.";
            case MEDIUM:
                return "Moderate screening pattern detected.";
            default:
                return "Low-risk screening pattern.";
        }
    }

    private static String recommendedAction(RiskLevel level) {
        switch (level) {
            case URGENT:
                return "Seek urgent medical help now if symptoms are present or readings remain this abnormal.";
            case HIGH:
                return "Rest, re-check readings, and contact a clinic or doctor if the pattern continues.";
            case MEDIUM:
                return "Monitor closely and repeat the measurement when rested.";
            default:
                return "Keep monitoring trends. This score does not diagnose or rule out illness.";
        }
    }

    private static List<String> list(String value) {
        List<String> result = new ArrayList<>();
        result.add(value);
        return result;
    }

    public static final class VitalSnapshot {
        private final Integer heartRate;
        private final BigDecimal temperature;
        private final Integer systolic;
        private final Integer diastolic;

        public VitalSnapshot(Integer heartRate, BigDecimal temperature, Integer systolic, Integer diastolic) {
            this.heartRate = heartRate;
            this.temperature = temperature == null ? null : temperature.setScale(2, RoundingMode.HALF_UP);
            this.systolic = systolic;
            this.diastolic = diastolic;
        }

        public static VitalSnapshot empty() {
            return new VitalSnapshot(null, null, null, null);
        }

        private boolean hasAnyReading() {
            return heartRate != null || temperature != null || (systolic != null && diastolic != null);
        }
    }

    public static final class PredictionResult {
        private final RiskLevel riskLevel;
        private final int score;
        private final String summary;
        private final String recommendedAction;
        private final DataQuality dataQuality;
        private final List<String> reasons;

        private PredictionResult(RiskLevel riskLevel, int score, String summary,
                String recommendedAction, DataQuality dataQuality, List<String> reasons) {
            this.riskLevel = riskLevel;
            this.score = score;
            this.summary = summary;
            this.recommendedAction = recommendedAction;
            this.dataQuality = dataQuality;
            this.reasons = reasons;
        }

        public RiskLevel getRiskLevel() {
            return riskLevel;
        }

        public int getScore() {
            return score;
        }

        public String getSummary() {
            return summary;
        }

        public String getRecommendedAction() {
            return recommendedAction;
        }

        public DataQuality getDataQuality() {
            return dataQuality;
        }

        public List<String> getReasons() {
            return reasons;
        }
    }

    private static final class VitalSection {
        private final Integer heartRate;
        private final BigDecimal temperature;
        private final Integer systolic;
        private final Integer diastolic;

        private VitalSection(Integer heartRate, BigDecimal temperature, Integer systolic, Integer diastolic) {
            this.heartRate = heartRate;
            this.temperature = temperature;
            this.systolic = systolic;
            this.diastolic = diastolic;
        }
    }
}
