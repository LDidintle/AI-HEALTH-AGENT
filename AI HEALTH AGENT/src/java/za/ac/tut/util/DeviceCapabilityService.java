package za.ac.tut.util;

public final class DeviceCapabilityService {

    private DeviceCapabilityService() {
    }

    public static Capabilities forSource(String source) {
        if (WatchTemperaturePolicy.isSleepTemperatureTrend(source)) {
            return new Capabilities(true, true, true, true, false,
                    "Samsung Health BP is calibration/source dependent. Temperature is sleep-temperature trend only.");
        }
        if (source != null && source.toUpperCase().contains("HEALTH_CONNECT")) {
            return new Capabilities(true, true, true, true, false,
                    "Health Connect exposes only records shared by installed health apps and user permissions.");
        }
        return new Capabilities(false, false, false, false, false,
                "No connected watch/source capabilities are confirmed yet.");
    }

    public static final class Capabilities {
        private final boolean heartRateSupported;
        private final boolean bloodPressureSupported;
        private final boolean sleepTemperatureSupported;
        private final boolean sleepTemperatureTrendOnly;
        private final boolean activitySupported;
        private final String caveat;

        private Capabilities(boolean heartRateSupported, boolean bloodPressureSupported,
                boolean sleepTemperatureSupported, boolean sleepTemperatureTrendOnly,
                boolean activitySupported, String caveat) {
            this.heartRateSupported = heartRateSupported;
            this.bloodPressureSupported = bloodPressureSupported;
            this.sleepTemperatureSupported = sleepTemperatureSupported;
            this.sleepTemperatureTrendOnly = sleepTemperatureTrendOnly;
            this.activitySupported = activitySupported;
            this.caveat = caveat;
        }

        public boolean isHeartRateSupported() {
            return heartRateSupported;
        }

        public boolean isBloodPressureSupported() {
            return bloodPressureSupported;
        }

        public boolean isSleepTemperatureSupported() {
            return sleepTemperatureSupported;
        }

        public boolean isSleepTemperatureTrendOnly() {
            return sleepTemperatureTrendOnly;
        }

        public boolean isActivitySupported() {
            return activitySupported;
        }

        public String toJson() {
            return "{"
                    + "\"heartRateSupported\":" + heartRateSupported + ","
                    + "\"bloodPressureSupported\":" + bloodPressureSupported + ","
                    + "\"sleepTemperatureSupported\":" + sleepTemperatureSupported + ","
                    + "\"sleepTemperatureTrendOnly\":" + sleepTemperatureTrendOnly + ","
                    + "\"activitySupported\":" + activitySupported + ","
                    + "\"caveat\":" + JsonUtil.quote(caveat)
                    + "}";
        }
    }
}
