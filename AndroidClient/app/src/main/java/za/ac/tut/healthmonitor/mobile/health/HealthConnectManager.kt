package za.ac.tut.healthmonitor.mobile.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
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
            .flatMap { record ->
                record.samples.map { sample ->
                    HeartRateSample(
                        value = sample.beatsPerMinute.toInt(),
                        measuredAt = sample.time,
                        metadata = record.metadata
                    )
                }
            }
            .maxByOrNull { it.measuredAt }

        val latestTemperatureRecord = client.readRecords(
            ReadRecordsRequest<BodyTemperatureRecord>(
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        ).records
            .maxByOrNull { it.time }

        val latestBloodPressure = client.readRecords(
            ReadRecordsRequest<BloodPressureRecord>(
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        ).records
            .maxByOrNull { it.time }

        val latestMetadata = listOfNotNull(
            latestHeartRate?.let { RecordMetadata(it.measuredAt, it.metadata) },
            latestTemperatureRecord?.let { RecordMetadata(it.time, it.metadata) },
            latestBloodPressure?.let { RecordMetadata(it.time, it.metadata) }
        ).maxByOrNull { it.measuredAt }

        return HealthSyncPayload(
            heartRate = latestHeartRate?.value,
            temperature = latestTemperatureRecord?.temperature?.inCelsius,
            systolic = latestBloodPressure?.systolic?.inMillimetersOfMercury?.toInt(),
            diastolic = latestBloodPressure?.diastolic?.inMillimetersOfMercury?.toInt(),
            recordedAt = latestMetadata?.measuredAt?.toString(),
            externalRecordId = latestMetadata?.metadata?.id?.takeIf { it.isNotBlank() },
            deviceType = latestMetadata?.metadata?.device?.typeName(),
            deviceManufacturer = latestMetadata?.metadata?.device?.manufacturer,
            deviceModel = latestMetadata?.metadata?.device?.model
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

    private data class HeartRateSample(
        val value: Int,
        val measuredAt: Instant,
        val metadata: Metadata
    )

    private data class RecordMetadata(
        val measuredAt: Instant,
        val metadata: Metadata
    )

    private fun Device.typeName(): String {
        return when (type) {
            Device.TYPE_WATCH -> "WATCH"
            Device.TYPE_PHONE -> "PHONE"
            Device.TYPE_SCALE -> "SCALE"
            Device.TYPE_RING -> "RING"
            Device.TYPE_HEAD_MOUNTED -> "HEAD_MOUNTED"
            Device.TYPE_FITNESS_BAND -> "FITNESS_BAND"
            Device.TYPE_CHEST_STRAP -> "CHEST_STRAP"
            Device.TYPE_SMART_DISPLAY -> "SMART_DISPLAY"
            else -> "UNKNOWN"
        }
    }
}
