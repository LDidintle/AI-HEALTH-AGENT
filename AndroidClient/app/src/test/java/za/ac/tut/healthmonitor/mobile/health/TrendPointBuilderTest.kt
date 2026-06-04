package za.ac.tut.healthmonitor.mobile.health

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrendPointBuilderTest {

    @Test
    fun keepsMultipleTemperaturePointsWhenHeartRateAlsoExists() {
        val points = buildAlignedTrendPoints(
            heartRates = listOf(70, 72, 75),
            systolics = listOf(99),
            temperatures = listOf(35.1, 35.2, 35.3),
            maxPoints = 12
        )

        assertEquals(3, points.mapNotNull { it.heartRate }.size)
        assertEquals(3, points.mapNotNull { it.temperature }.size)
        assertTrue(points.mapNotNull { it.temperature }.contains(35.3))
    }

    @Test
    fun limitsEachSeriesToLatestMaximumPoints() {
        val points = buildAlignedTrendPoints(
            heartRates = (1..20).toList(),
            systolics = (100..119).toList(),
            temperatures = (1..20).map { 34.0 + it },
            maxPoints = 12
        )

        assertEquals(12, points.size)
        assertEquals(9, points.first().heartRate)
        assertEquals(108, points.first().systolic)
        assertEquals(43.0, points.first().temperature ?: 0.0, 0.001)
    }
}
