package za.ac.tut.healthmonitor.mobile.health

import java.time.Instant
import java.time.ZoneId
import za.ac.tut.healthmonitor.mobile.data.model.HealthSectionSyncPayload

fun currentSectionStart(
    now: Instant = Instant.now(),
    zoneId: ZoneId = ZoneId.systemDefault()
): Instant {
    return now.atZone(zoneId)
        .toLocalDate()
        .atStartOfDay(zoneId)
        .toInstant()
}

fun HealthSectionSyncPayload.isCurrentDaySection(
    now: Instant = Instant.now(),
    zoneId: ZoneId = ZoneId.systemDefault()
): Boolean {
    val sectionEnd = runCatching { Instant.parse(windowEnd) }.getOrNull() ?: return false
    return !sectionEnd.isBefore(currentSectionStart(now, zoneId))
}
