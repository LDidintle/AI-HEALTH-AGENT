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
import za.ac.tut.healthmonitor.mobile.data.model.HealthSectionSyncPayload
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

    suspend fun hasAnyPermission(): Boolean {
        val client = getClientOrNull() ?: return false
        val grantedPermissions = client.permissionController.getGrantedPermissions()
        return grantedPermissions.any { it in requiredPermissions }
    }

    suspend fun readLatestSection(windowMinutes: Long = DEFAULT_SECTION_WINDOW_MINUTES): HealthSection {
        val client = getClientOrThrow()
        val grantedPermissions = client.permissionController.getGrantedPermissions()
        val end = Instant.now()
        val start = end.minus(windowMinutes, ChronoUnit.MINUTES)

        val heartRateSamples = readHeartRateSamples(client, grantedPermissions, start, end)
        val temperatureRecords = readTemperatureRecords(client, grantedPermissions, start, end)
        val bloodPressureRecords = readBloodPressureRecords(client, grantedPermissions, start, end)

        val latestHeartRate = heartRateSamples.maxByOrNull { it.measuredAt }
        val latestTemperature = temperatureRecords.maxByOrNull { it.time }
        val latestBloodPressure = bloodPressureRecords.maxByOrNull { it.time }
        val latestMetadata = listOfNotNull(
            latestHeartRate?.let { RecordMetadata(it.measuredAt, it.metadata) },
            latestTemperature?.let { RecordMetadata(it.time, it.metadata) },
            latestBloodPressure?.let { RecordMetadata(it.time, it.metadata) }
        ).maxByOrNull { it.measuredAt }

        val heartRateValues = heartRateSamples.map { it.value }
        val temperatureValues = temperatureRecords.map { it.temperature.inCelsius }
        val trendPoints = buildTrendPoints(
            heartRateSamples = heartRateSamples,
            temperatureRecords = temperatureRecords,
            bloodPressureRecords = bloodPressureRecords
        )

        return HealthSection(
            payload = HealthSectionSyncPayload(
                windowStart = start.toString(),
                windowEnd = end.toString(),
                source = "HEALTH_CONNECT_SECTION",
                heartRateLatest = latestHeartRate?.value,
                heartRateMin = heartRateValues.minOrNull(),
                heartRateMax = heartRateValues.maxOrNull(),
                heartRateAverage = heartRateValues.takeIf { it.isNotEmpty() }?.average(),
                heartRateCount = heartRateValues.size,
                temperatureLatest = latestTemperature?.temperature?.inCelsius,
                temperatureMin = temperatureValues.minOrNull(),
                temperatureMax = temperatureValues.maxOrNull(),
                temperatureAverage = temperatureValues.takeIf { it.isNotEmpty() }?.average(),
                temperatureCount = temperatureValues.size,
                systolicLatest = latestBloodPressure?.systolic?.inMillimetersOfMercury?.toInt(),
                diastolicLatest = latestBloodPressure?.diastolic?.inMillimetersOfMercury?.toInt(),
                bloodPressureCount = bloodPressureRecords.size,
                deviceType = latestMetadata?.metadata?.device?.typeName(),
                deviceManufacturer = latestMetadata?.metadata?.device?.manufacturer,
                deviceModel = latestMetadata?.metadata?.device?.model
            ),
            trendPoints = trendPoints
        )
    }

    suspend fun readLatestVitals(): HealthSyncPayload {
        val client = getClientOrThrow()
        val grantedPermissions = client.permissionController.getGrantedPermissions()
        val end = Instant.now()
        val start = end.minus(30, ChronoUnit.DAYS)

        val heartRateRecords = readIfPermitted(
            grantedPermissions = grantedPermissions,
            permission = HealthPermission.getReadPermission(HeartRateRecord::class)
        ) {
            client.readRecords(
                ReadRecordsRequest<HeartRateRecord>(
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            ).records
        }.orEmpty()

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

        val latestTemperatureRecord = readIfPermitted(
            grantedPermissions = grantedPermissions,
            permission = HealthPermission.getReadPermission(BodyTemperatureRecord::class)
        ) {
            client.readRecords(
                ReadRecordsRequest<BodyTemperatureRecord>(
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            ).records
        }.orEmpty()
            .maxByOrNull { it.time }

        val latestBloodPressure = readIfPermitted(
            grantedPermissions = grantedPermissions,
            permission = HealthPermission.getReadPermission(BloodPressureRecord::class)
        ) {
            client.readRecords(
                ReadRecordsRequest<BloodPressureRecord>(
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            ).records
        }.orEmpty()
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

    private suspend fun readHeartRateSamples(
        client: HealthConnectClient,
        grantedPermissions: Set<String>,
        start: Instant,
        end: Instant
    ): List<HeartRateSample> {
        val heartRateRecords = readIfPermitted(
            grantedPermissions = grantedPermissions,
            permission = HealthPermission.getReadPermission(HeartRateRecord::class)
        ) {
            client.readRecords(
                ReadRecordsRequest<HeartRateRecord>(
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            ).records
        }.orEmpty()

        return heartRateRecords.flatMap { record ->
            record.samples.map { sample ->
                HeartRateSample(
                    value = sample.beatsPerMinute.toInt(),
                    measuredAt = sample.time,
                    metadata = record.metadata
                )
            }
        }
    }

    private suspend fun readTemperatureRecords(
        client: HealthConnectClient,
        grantedPermissions: Set<String>,
        start: Instant,
        end: Instant
    ): List<BodyTemperatureRecord> {
        return readIfPermitted(
            grantedPermissions = grantedPermissions,
            permission = HealthPermission.getReadPermission(BodyTemperatureRecord::class)
        ) {
            client.readRecords(
                ReadRecordsRequest<BodyTemperatureRecord>(
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            ).records
        }.orEmpty()
    }

    private suspend fun readBloodPressureRecords(
        client: HealthConnectClient,
        grantedPermissions: Set<String>,
        start: Instant,
        end: Instant
    ): List<BloodPressureRecord> {
        return readIfPermitted(
            grantedPermissions = grantedPermissions,
            permission = HealthPermission.getReadPermission(BloodPressureRecord::class)
        ) {
            client.readRecords(
                ReadRecordsRequest<BloodPressureRecord>(
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            ).records
        }.orEmpty()
    }

    private fun buildTrendPoints(
        heartRateSamples: List<HeartRateSample>,
        temperatureRecords: List<BodyTemperatureRecord>,
        bloodPressureRecords: List<BloodPressureRecord>
    ): List<HealthSectionTrendPoint> {
        val heartRatePoints = heartRateSamples
            .sortedBy { it.measuredAt }
            .takeLast(MAX_TREND_POINTS)
            .map { HealthSectionTrendPoint(heartRate = it.value) }

        if (heartRatePoints.isNotEmpty()) {
            val latestTemperature = temperatureRecords.maxByOrNull { it.time }?.temperature?.inCelsius
            val latestBloodPressure = bloodPressureRecords.maxByOrNull { it.time }
            return heartRatePoints.mapIndexed { index, point ->
                if (index == heartRatePoints.lastIndex) {
                    point.copy(
                        temperature = latestTemperature,
                        systolic = latestBloodPressure?.systolic?.inMillimetersOfMercury?.toInt()
                    )
                } else {
                    point
                }
            }
        }

        val temperaturePoints = temperatureRecords
            .sortedBy { it.time }
            .takeLast(MAX_TREND_POINTS)
            .map { HealthSectionTrendPoint(temperature = it.temperature.inCelsius) }

        if (temperaturePoints.isNotEmpty()) {
            return temperaturePoints
        }

        return bloodPressureRecords
            .sortedBy { it.time }
            .takeLast(MAX_TREND_POINTS)
            .map { HealthSectionTrendPoint(systolic = it.systolic.inMillimetersOfMercury.toInt()) }
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

    private suspend fun <T> readIfPermitted(
        grantedPermissions: Set<String>,
        permission: String,
        block: suspend () -> T
    ): T? {
        if (permission !in grantedPermissions) {
            return null
        }

        return try {
            block()
        } catch (e: Exception) {
            if (e.isHealthConnectPermissionFailure()) {
                null
            } else {
                throw e
            }
        }
    }

    private fun Exception.isHealthConnectPermissionFailure(): Boolean {
        val className = javaClass.name
        val detail = message.orEmpty()
        return this is SecurityException ||
                (className == "android.health.connect.HealthConnectException" &&
                        detail.contains("requires", ignoreCase = true)) ||
                detail.contains("requires one of the permissions", ignoreCase = true)
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

    private companion object {
        const val DEFAULT_SECTION_WINDOW_MINUTES = 60L
        const val MAX_TREND_POINTS = 12
    }
}
