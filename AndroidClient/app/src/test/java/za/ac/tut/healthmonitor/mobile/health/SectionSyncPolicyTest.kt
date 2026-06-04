package za.ac.tut.healthmonitor.mobile.health

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import za.ac.tut.healthmonitor.mobile.data.model.HealthSectionSyncPayload

class SectionSyncPolicyTest {

    @Test
    fun sectionWindowStartsAtBeginningOfCurrentDay() {
        val zoneId = ZoneId.of("Africa/Johannesburg")
        val now = Instant.parse("2026-05-29T21:30:00Z")

        assertEquals("2026-05-28T22:00:00Z", currentSectionStart(now, zoneId).toString())
    }

    @Test
    fun sectionIsCurrentWhenItEndedToday() {
        val zoneId = ZoneId.of("Africa/Johannesburg")
        val now = Instant.parse("2026-05-29T21:30:00Z")
        val payload = HealthSectionSyncPayload(
            windowStart = "2026-05-28T22:00:00Z",
            windowEnd = "2026-05-29T03:45:00Z",
            heartRateLatest = 78,
            heartRateCount = 32
        )

        assertTrue(payload.isCurrentDaySection(now, zoneId))
    }

    @Test
    fun sectionIsStaleWhenItEndedYesterdayEvenInsideRolling24Hours() {
        val zoneId = ZoneId.of("Africa/Johannesburg")
        val now = Instant.parse("2026-05-29T23:23:39Z")
        val payload = HealthSectionSyncPayload(
            windowStart = "2026-05-28T22:00:00Z",
            windowEnd = "2026-05-29T21:59:59Z",
            heartRateLatest = 75,
            heartRateCount = 996
        )

        assertFalse(payload.isCurrentDaySection(now, zoneId))
    }
}
