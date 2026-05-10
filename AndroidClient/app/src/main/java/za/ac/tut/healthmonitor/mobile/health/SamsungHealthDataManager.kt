package za.ac.tut.healthmonitor.mobile.health

import android.app.Activity
import android.content.Context
import com.samsung.android.sdk.health.data.HealthDataService
import com.samsung.android.sdk.health.data.data.HealthDataPoint
import com.samsung.android.sdk.health.data.data.entries.HeartRate
import com.samsung.android.sdk.health.data.error.AuthorizationException
import com.samsung.android.sdk.health.data.error.HealthDataException
import com.samsung.android.sdk.health.data.error.ResolvablePlatformException
import com.samsung.android.sdk.health.data.permission.AccessType
import com.samsung.android.sdk.health.data.permission.Permission
import com.samsung.android.sdk.health.data.request.DataType
import com.samsung.android.sdk.health.data.request.DataTypes
import com.samsung.android.sdk.health.data.request.InstantTimeFilter
import com.samsung.android.sdk.health.data.request.Ordering
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import za.ac.tut.healthmonitor.mobile.data.model.HealthSectionSyncPayload

class SamsungHealthDataManager(
    context: Context
) {
    private val appContext = context.applicationContext
    private val store = HealthDataService.getStore(appContext)

    private val heartRatePermission = Permission.of(DataTypes.HEART_RATE, AccessType.READ)
    private val bloodPressurePermission = Permission.of(DataTypes.BLOOD_PRESSURE, AccessType.READ)
    private val bodyTemperaturePermission = Permission.of(DataTypes.BODY_TEMPERATURE, AccessType.READ)
    private val skinTemperaturePermission = Permission.of(DataTypes.SKIN_TEMPERATURE, AccessType.READ)
    private val bloodOxygenPermission = Permission.of(DataTypes.BLOOD_OXYGEN, AccessType.READ)
    private val requiredPermissions = setOf(
        heartRatePermission,
        bloodPressurePermission,
        bodyTemperaturePermission,
        skinTemperaturePermission,
        bloodOxygenPermission
    )

    suspend fun hasHeartRatePermission(): Boolean {
        return store.getGrantedPermissions(requiredPermissions).any { it in requiredPermissions }
    }

    suspend fun requestHeartRatePermission(activity: Activity): Boolean {
        val grantedPermissions = store.getGrantedPermissions(requiredPermissions)
        if (grantedPermissions.containsAll(requiredPermissions)) {
            return true
        }

        val missingPermissions = requiredPermissions - grantedPermissions
        val newlyGranted = store.requestPermissions(missingPermissions, activity)
        return (grantedPermissions + newlyGranted).any { it in requiredPermissions }
    }

    suspend fun readLatestHeartRateSection(
        windowMinutes: Long = DEFAULT_SECTION_WINDOW_MINUTES
    ): HealthSection {
        val end = Instant.now()
        val start = end.minus(windowMinutes, ChronoUnit.MINUTES)
        val grantedPermissions = store.getGrantedPermissions(requiredPermissions)
        val heartRateSamples = readIfPermitted(grantedPermissions, heartRatePermission) {
            readSamsungDataPoints(DataTypes.HEART_RATE.readDataRequestBuilder, start, end)
                .flatMap { it.toHeartRateSamples() }
        }.orEmpty()
            .filter { it.measuredAt in start..end }
            .sortedBy { it.measuredAt }

        val bloodPressureSamples = readIfPermitted(grantedPermissions, bloodPressurePermission) {
            readSamsungDataPoints(DataTypes.BLOOD_PRESSURE.readDataRequestBuilder, start, end)
                .mapNotNull { it.toBloodPressureSample() }
        }.orEmpty()
            .filter { it.measuredAt in start..end }
            .sortedBy { it.measuredAt }

        val bodyTemperatureSamples = readIfPermitted(grantedPermissions, bodyTemperaturePermission) {
            readSamsungDataPoints(DataTypes.BODY_TEMPERATURE.readDataRequestBuilder, start, end)
                .mapNotNull { it.toBodyTemperatureSample() }
        }.orEmpty()
            .filter { it.measuredAt in start..end }
            .sortedBy { it.measuredAt }

        val skinTemperatureSamples = readIfPermitted(grantedPermissions, skinTemperaturePermission) {
            readSamsungDataPoints(DataTypes.SKIN_TEMPERATURE.readDataRequestBuilder, start, end)
                .flatMap { it.toSkinTemperatureSamples() }
        }.orEmpty()
            .filter { it.measuredAt in start..end }
            .sortedBy { it.measuredAt }

        // Read now so unsupported/empty blood oxygen sources do not block the other vitals.
        readIfPermitted(grantedPermissions, bloodOxygenPermission) {
            readSamsungDataPoints(DataTypes.BLOOD_OXYGEN.readDataRequestBuilder, start, end)
        }

        val heartRateValues = heartRateSamples.map { it.value }
        val temperatureSamples = (bodyTemperatureSamples + skinTemperatureSamples).sortedBy { it.measuredAt }
        val temperatureValues = temperatureSamples.map { it.value }
        val latestHeartRate = heartRateSamples.lastOrNull()
        val latestBloodPressure = bloodPressureSamples.lastOrNull()
        val latestTemperature = temperatureSamples.lastOrNull()
        val latestMeasuredAt = listOfNotNull(
            latestHeartRate?.measuredAt,
            latestBloodPressure?.measuredAt,
            latestTemperature?.measuredAt
        ).maxOrNull()

        return HealthSection(
            payload = HealthSectionSyncPayload(
                windowStart = start.toString(),
                windowEnd = latestMeasuredAt?.toString() ?: end.toString(),
                source = SAMSUNG_HEALTH_SOURCE,
                heartRateLatest = latestHeartRate?.value,
                heartRateMin = heartRateValues.minOrNull(),
                heartRateMax = heartRateValues.maxOrNull(),
                heartRateAverage = heartRateValues.takeIf { it.isNotEmpty() }?.average(),
                heartRateCount = heartRateValues.size,
                temperatureLatest = latestTemperature?.value,
                temperatureMin = temperatureValues.minOrNull(),
                temperatureMax = temperatureValues.maxOrNull(),
                temperatureAverage = temperatureValues.takeIf { it.isNotEmpty() }?.average(),
                temperatureCount = temperatureValues.size,
                systolicLatest = latestBloodPressure?.systolic,
                diastolicLatest = latestBloodPressure?.diastolic,
                bloodPressureCount = bloodPressureSamples.size,
                deviceType = "WATCH",
                deviceManufacturer = "Samsung",
                deviceModel = latestHeartRate?.deviceId ?: latestBloodPressure?.deviceId ?: latestTemperature?.deviceId
            ),
            trendPoints = buildTrendPoints(
                heartRateSamples = heartRateSamples,
                bloodPressureSamples = bloodPressureSamples,
                temperatureSamples = temperatureSamples
            )
        )
    }

    private suspend fun readSamsungDataPoints(
        builder: com.samsung.android.sdk.health.data.request.ReadDataRequest.DualTimeBuilder<HealthDataPoint>,
        start: Instant,
        end: Instant
    ): List<HealthDataPoint> {
        val request = builder
            .setInstantTimeFilter(InstantTimeFilter.of(start, end))
            .setOrdering(Ordering.ASC)
            .setLimit(MAX_RECORDS)
            .build()
        return store.readData(request).dataList
    }

    private suspend fun <T> readIfPermitted(
        grantedPermissions: Set<Permission>,
        permission: Permission,
        block: suspend () -> T
    ): T? {
        if (permission !in grantedPermissions) {
            return null
        }

        return try {
            block()
        } catch (e: Exception) {
            null
        }
    }

    private fun buildTrendPoints(
        heartRateSamples: List<SamsungHeartRateSample>,
        bloodPressureSamples: List<SamsungBloodPressureSample>,
        temperatureSamples: List<SamsungTemperatureSample>
    ): List<HealthSectionTrendPoint> {
        val heartRatePoints = heartRateSamples
            .takeLast(MAX_TREND_POINTS)
            .map { HealthSectionTrendPoint(heartRate = it.value) }

        if (heartRatePoints.isNotEmpty()) {
            val latestBloodPressure = bloodPressureSamples.lastOrNull()
            val latestTemperature = temperatureSamples.lastOrNull()
            return heartRatePoints.mapIndexed { index, point ->
                if (index == heartRatePoints.lastIndex) {
                    point.copy(
                        systolic = latestBloodPressure?.systolic,
                        temperature = latestTemperature?.value
                    )
                } else {
                    point
                }
            }
        }

        val bloodPressurePoints = bloodPressureSamples
            .takeLast(MAX_TREND_POINTS)
            .map { HealthSectionTrendPoint(systolic = it.systolic) }

        if (bloodPressurePoints.isNotEmpty()) {
            return bloodPressurePoints
        }

        return temperatureSamples
                .takeLast(MAX_TREND_POINTS)
            .map { HealthSectionTrendPoint(temperature = it.value) }
    }

    fun resolveIfPossible(error: Exception, activity: Activity): Boolean {
        val resolvable = error as? ResolvablePlatformException ?: return false
        if (!resolvable.hasResolution) {
            return false
        }

        resolvable.resolve(activity)
        return true
    }

    fun toUserMessage(error: Exception): String {
        val healthError = error as? HealthDataException
        if (healthError?.errorCode == ERR_ACCESS_CONTROL || error is AuthorizationException) {
            return "Samsung Health blocked this debug app. Enable Samsung Health developer mode for Data Read, or register this package and release SHA-256 with Samsung."
        }

        return error.message ?: "Samsung Health is not ready to share data yet."
    }

    private fun HealthDataPoint.toHeartRateSamples(): List<SamsungHeartRateSample> {
        val seriesSamples = getValue(DataType.HeartRateType.SERIES_DATA)
            .orEmpty()
            .map { it.toSample(dataSource?.deviceId) }

        if (seriesSamples.isNotEmpty()) {
            return seriesSamples
        }

        val heartRate = getValue(DataType.HeartRateType.HEART_RATE) ?: return emptyList()
        return listOf(
            SamsungHeartRateSample(
                value = heartRate.roundToInt(),
                measuredAt = endTime ?: startTime,
                deviceId = dataSource?.deviceId
            )
        )
    }

    private fun HeartRate.toSample(deviceId: String?): SamsungHeartRateSample {
        return SamsungHeartRateSample(
            value = heartRate.roundToInt(),
            measuredAt = endTime,
            deviceId = deviceId
        )
    }

    private fun HealthDataPoint.toBloodPressureSample(): SamsungBloodPressureSample? {
        val systolic = getValue(DataType.BloodPressureType.SYSTOLIC) ?: return null
        val diastolic = getValue(DataType.BloodPressureType.DIASTOLIC) ?: return null
        return SamsungBloodPressureSample(
            systolic = systolic.roundToInt(),
            diastolic = diastolic.roundToInt(),
            measuredAt = endTime ?: startTime,
            deviceId = dataSource?.deviceId
        )
    }

    private fun HealthDataPoint.toBodyTemperatureSample(): SamsungTemperatureSample? {
        val bodyTemperature = getValue(DataType.BodyTemperatureType.BODY_TEMPERATURE) ?: return null
        return SamsungTemperatureSample(
            value = bodyTemperature.toDouble(),
            measuredAt = endTime ?: startTime,
            deviceId = dataSource?.deviceId
        )
    }

    private fun HealthDataPoint.toSkinTemperatureSamples(): List<SamsungTemperatureSample> {
        val seriesSamples = getValue(DataType.SkinTemperatureType.SERIES_DATA)
            .orEmpty()
            .map {
                SamsungTemperatureSample(
                    value = it.skinTemperature.toDouble(),
                    measuredAt = it.endTime,
                    deviceId = dataSource?.deviceId
                )
            }

        if (seriesSamples.isNotEmpty()) {
            return seriesSamples
        }

        val skinTemperature = getValue(DataType.SkinTemperatureType.SKIN_TEMPERATURE) ?: return emptyList()
        return listOf(
            SamsungTemperatureSample(
                value = skinTemperature.toDouble(),
                measuredAt = endTime ?: startTime,
                deviceId = dataSource?.deviceId
            )
        )
    }

    private data class SamsungHeartRateSample(
        val value: Int,
        val measuredAt: Instant,
        val deviceId: String?
    )

    private data class SamsungBloodPressureSample(
        val systolic: Int,
        val diastolic: Int,
        val measuredAt: Instant,
        val deviceId: String?
    )

    private data class SamsungTemperatureSample(
        val value: Double,
        val measuredAt: Instant,
        val deviceId: String?
    )

    private companion object {
        const val DEFAULT_SECTION_WINDOW_MINUTES = 30L * 24L * 60L
        const val MAX_RECORDS = 200
        const val MAX_TREND_POINTS = 12
        const val SAMSUNG_HEALTH_SOURCE = "SAMSUNG_HEALTH_DATA"
        const val ERR_ACCESS_CONTROL = 2003
    }
}
