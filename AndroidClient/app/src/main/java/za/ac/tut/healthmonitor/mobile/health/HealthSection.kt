package za.ac.tut.healthmonitor.mobile.health

import za.ac.tut.healthmonitor.mobile.data.model.HealthSectionSyncPayload

data class HealthSection(
    val payload: HealthSectionSyncPayload,
    val trendPoints: List<HealthSectionTrendPoint>
) {
    fun isEmpty(): Boolean = payload.isEmpty()
}

data class HealthSectionTrendPoint(
    val heartRate: Int? = null,
    val systolic: Int? = null,
    val temperature: Double? = null
)
