package za.ac.tut.healthmonitor.mobile.data.model

data class LoginResponse(
    val success: Boolean,
    val message: String? = null,
    val user: BackendUser? = null
)

data class ProfileResponse(
    val success: Boolean,
    val user: BackendProfile? = null,
    val message: String? = null
)

data class SyncResponse(
    val success: Boolean,
    val message: String? = null
)

data class AiChatResponse(
    val reply: String? = null,
    val source: String? = null
)

data class LatestReadingsResponse(
    val success: Boolean,
    val email: String? = null,
    val heartRate: ReadingValue? = null,
    val temperature: TemperatureValue? = null,
    val bloodPressure: BloodPressureValue? = null,
    val message: String? = null
)

data class BackendUser(
    val id: Int,
    val email: String,
    val fullName: String,
    val isVerified: Boolean = false
)

data class BackendProfile(
    val id: Int,
    val email: String,
    val title: String? = null,
    val firstName: String,
    val surname: String,
    val gender: String? = null,
    val cellNumber: String? = null,
    val isVerified: Boolean = false
)

data class ReadingValue(
    val value: Int,
    val status: String? = null,
    val recordedAt: String? = null,
    val source: String? = null
)

data class TemperatureValue(
    val value: Double,
    val status: String? = null,
    val recordedAt: String? = null,
    val source: String? = null
)

data class BloodPressureValue(
    val systolic: Int,
    val diastolic: Int,
    val status: String? = null,
    val recordedAt: String? = null,
    val source: String? = null
)

data class HealthSyncPayload(
    val heartRate: Int? = null,
    val temperature: Double? = null,
    val systolic: Int? = null,
    val diastolic: Int? = null,
    val source: String? = null,
    val recordedAt: String? = null,
    val externalRecordId: String? = null,
    val deviceType: String? = null,
    val deviceManufacturer: String? = null,
    val deviceModel: String? = null
) {
    fun isEmpty(): Boolean {
        return heartRate == null && temperature == null && systolic == null && diastolic == null
    }
}

data class HealthSectionSyncPayload(
    val windowStart: String,
    val windowEnd: String,
    val source: String = "HEALTH_CONNECT",
    val heartRateLatest: Int? = null,
    val heartRateMin: Int? = null,
    val heartRateMax: Int? = null,
    val heartRateAverage: Double? = null,
    val heartRateCount: Int = 0,
    val temperatureLatest: Double? = null,
    val temperatureMin: Double? = null,
    val temperatureMax: Double? = null,
    val temperatureAverage: Double? = null,
    val temperatureCount: Int = 0,
    val systolicLatest: Int? = null,
    val diastolicLatest: Int? = null,
    val bloodPressureCount: Int = 0,
    val deviceType: String? = null,
    val deviceManufacturer: String? = null,
    val deviceModel: String? = null
) {
    fun isEmpty(): Boolean {
        return heartRateCount == 0 && temperatureCount == 0 && bloodPressureCount == 0
    }
}
