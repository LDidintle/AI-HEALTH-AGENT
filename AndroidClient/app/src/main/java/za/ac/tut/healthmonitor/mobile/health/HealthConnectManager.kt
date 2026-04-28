package za.ac.tut.healthmonitor.mobile.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.temporal.ChronoUnit
import za.ac.tut.healthmonitor.mobile.data.model.HealthSyncPayload

class HealthConnectManager(
    private val context: Context
) {

    val requiredPermissions: Set<String> = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(BloodPressureRecord::class),
        HealthPermission.getReadPermission(BodyTemperatureRecord::class)
    )

    fun availabilityStatus(): Int {
        return HealthConnectClient.getSdkStatus(context)
    }

    suspend fun hasAllPermissions(): Boolean {
        val client = getClientOrNull() ?: return false
        val grantedPermissions = client.permissionController.getGrantedPermissions()
        return grantedPermissions.containsAll(requiredPermissions)
    }

    suspend fun readLatestVitals(): HealthSyncPayload {
        val client = getClientOrThrow()
        val end = Instant.now()
        val start = end.minus(30, ChronoUnit.DAYS)

        val heartRateRecords = client.readRecords(
            ReadRecordsRequest<HeartRateRecord>(
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        ).records

        val latestHeartRate = heartRateRecords
            .flatMap { it.samples }
            .maxByOrNull { it.time }
            ?.beatsPerMinute
            ?.toInt()

        val latestTemperature = client.readRecords(
            ReadRecordsRequest<BodyTemperatureRecord>(
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        ).records
            .maxByOrNull { it.time }
            ?.temperature
            ?.inCelsius

        val latestBloodPressure = client.readRecords(
            ReadRecordsRequest<BloodPressureRecord>(
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        ).records
            .maxByOrNull { it.time }

        return HealthSyncPayload(
            heartRate = latestHeartRate,
            temperature = latestTemperature,
            systolic = latestBloodPressure?.systolic?.inMillimetersOfMercury?.toInt(),
            diastolic = latestBloodPressure?.diastolic?.inMillimetersOfMercury?.toInt()
        )
    }

    private fun getClientOrNull(): HealthConnectClient? {
        return if (availabilityStatus() == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }

    private fun getClientOrThrow(): HealthConnectClient {
        return HealthConnectClient.getOrCreate(context)
    }
}
