package za.ac.tut.healthmonitor.mobile.insights

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime
import za.ac.tut.healthmonitor.mobile.data.model.LatestReadingsResponse
import za.ac.tut.healthmonitor.mobile.data.model.ReadingValue
import za.ac.tut.healthmonitor.mobile.data.model.TemperatureValue

class HealthInsightEngineTest {

    @Test
    fun sleepSkinTemperatureIsTreatedAsTrendNotFever() {
        val readings = LatestReadingsResponse(
            success = true,
            temperature = TemperatureValue(value = 38.2, source = "SAMSUNG_HEALTH_DATA")
        )

        val insights = HealthInsightEngine.buildInsights(
            readings,
            ReadingContext(activityState = ActivityState.Sleeping)
        )

        assertTrue(insights.any { it.title.contains("sleep temperature", ignoreCase = true) })
        assertFalse(insights.any { it.title.contains("fever", ignoreCase = true) })
    }

    @Test
    fun highHeartRateDuringExerciseUsesRecoveryGuidance() {
        val readings = LatestReadingsResponse(
            success = true,
            heartRate = ReadingValue(value = 118, source = "SAMSUNG_HEALTH_DATA")
        )

        val insights = HealthInsightEngine.buildInsights(
            readings,
            ReadingContext(activityState = ActivityState.Exercising)
        )

        assertTrue(insights.any { it.suggestion.contains("cool down", ignoreCase = true) })
        assertFalse(insights.any { it.title.contains("fast heart rate pattern", ignoreCase = true) })
    }

    @Test
    fun highHeartRateAtRestKeepsWarningGuidance() {
        val readings = LatestReadingsResponse(
            success = true,
            heartRate = ReadingValue(value = 118, source = "SAMSUNG_HEALTH_DATA")
        )

        val insights = HealthInsightEngine.buildInsights(
            readings,
            ReadingContext(activityState = ActivityState.Resting)
        )

        assertTrue(insights.any { it.title.contains("resting fast heart rate", ignoreCase = true) })
    }

    @Test
    fun notSureInsideSleepScheduleUsesSleepContext() {
        val readings = LatestReadingsResponse(
            success = true,
            temperature = TemperatureValue(value = 38.2, source = "SAMSUNG_HEALTH_DATA")
        )

        val insights = HealthInsightEngine.buildInsights(
            readings,
            ReadingContext(
                activityState = ActivityState.NotSure,
                sleepStart = "22:00",
                sleepEnd = "06:30",
                now = LocalTime.of(23, 15)
            )
        )

        assertTrue(insights.any { it.title.contains("sleep temperature", ignoreCase = true) })
    }

    @Test
    fun samsungSleepTemperatureRemainsTrendWhenReviewedDuringDay() {
        val readings = LatestReadingsResponse(
            success = true,
            temperature = TemperatureValue(value = 34.1, source = "SAMSUNG_HEALTH_DATA")
        )

        val insights = HealthInsightEngine.buildInsights(
            readings,
            ReadingContext(
                activityState = ActivityState.NotSure,
                sleepStart = "00:00",
                sleepEnd = "08:00",
                now = LocalTime.of(14, 36)
            )
        )

        assertTrue(insights.any { it.title.contains("sleep temperature", ignoreCase = true) })
        assertFalse(insights.any { it.title.contains("low temperature", ignoreCase = true) })
    }
}
