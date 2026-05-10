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
    private val requiredPermissions = setOf(heartRatePermission)

    suspend fun hasHeartRatePermission(): Boolean {
        return store.getGrantedPermissions(requiredPermissions).containsAll(requiredPermissions)
    }

    suspend fun requestHeartRatePermission(activity: Activity): Boolean {
        val grantedPermissions = store.getGrantedPermissions(requiredPermissions)
        if (grantedPermissions.containsAll(requiredPermissions)) {
            return true
        }

        val missingPermissions = requiredPermissions - grantedPermissions
        val newlyGranted = store.requestPermissions(missingPermissions, activity)
        return newlyGranted.containsAll(missingPermissions)
    }

    suspend fun readLatestHeartRateSection(
        windowMinutes: Long = DEFAULT_SECTION_WINDOW_MINUTES
    ): HealthSection {
        val end = Instant.now()
        val start = end.minus(windowMinutes, ChronoUnit.MINUTES)
        val request = DataTypes.HEART_RATE.readDataRequestBuilder
            .setInstantTimeFilter(InstantTimeFilter.of(start, end))
            .setOrdering(Ordering.ASC)
            .setLimit(MAX_RECORDS)
            .build()

        val dataPoints = store.readData(request).dataList
        val samples = dataPoints
            .flatMap { it.toHeartRateSamples() }
            .filter { it.measuredAt in start..end }
            .sortedBy { it.measuredAt }

        val values = samples.map { it.value }
        val latest = samples.lastOrNull()

        return HealthSection(
            payload = HealthSectionSyncPayload(
                windowStart = start.toString(),
                windowEnd = latest?.measuredAt?.toString() ?: end.toString(),
                source = SAMSUNG_HEALTH_SOURCE,
                heartRateLatest = latest?.value,
                heartRateMin = values.minOrNull(),
                heartRateMax = values.maxOrNull(),
                heartRateAverage = values.takeIf { it.isNotEmpty() }?.average(),
                heartRateCount = values.size,
                deviceType = "WATCH",
                deviceManufacturer = "Samsung",
                deviceModel = latest?.deviceId
            ),
            trendPoints = samples
                .takeLast(MAX_TREND_POINTS)
                .map { HealthSectionTrendPoint(heartRate = it.value) }
        )
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

    private data class SamsungHeartRateSample(
        val value: Int,
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
