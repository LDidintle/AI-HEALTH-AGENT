package za.ac.tut.healthmonitor.mobile.insights

import java.time.LocalTime
import za.ac.tut.healthmonitor.mobile.data.model.LatestReadingsResponse

data class HealthInsight(
    val title: String,
    val possibleConcern: String,
    val suggestion: String,
    val severity: InsightSeverity
)

enum class InsightSeverity {
    Info,
    Watch,
    Urgent
}

enum class ActivityState(val label: String) {
    Resting("Resting"),
    Exercising("Exercising"),
    Sleeping("Sleeping"),
    NotSure("Not sure")
}

data class ReadingContext(
    val activityState: ActivityState = ActivityState.NotSure,
    val sleepStart: String? = null,
    val sleepEnd: String? = null,
    val now: LocalTime = LocalTime.now()
)

object HealthInsightEngine {

    fun hasConnectedWatchData(readings: LatestReadingsResponse?): Boolean {
        return listOf(
            readings?.heartRate?.source,
            readings?.temperature?.source,
            readings?.bloodPressure?.source
        ).any {
            it.equals("HEALTH_CONNECT", ignoreCase = true)
                    || it.equals("HEALTH_CONNECT_SECTION", ignoreCase = true)
                    || it.equals("SAMSUNG_HEALTH_DATA", ignoreCase = true)
        }
    }

    fun hasAnyReading(readings: LatestReadingsResponse?): Boolean {
        return readings?.heartRate != null || readings?.temperature != null || readings?.bloodPressure != null
    }

    fun buildInsights(
        readings: LatestReadingsResponse?,
        context: ReadingContext = ReadingContext()
    ): List<HealthInsight> {
        if (!hasAnyReading(readings)) {
            return listOf(
                HealthInsight(
                    title = "No health data connected yet",
                    possibleConcern = "No Galaxy Watch or Health Connect readings were found.",
                    suggestion = "Connect the Galaxy Watch 5 to Samsung Health, allow Samsung Health to share with Health Connect, then sync again in this app.",
                    severity = InsightSeverity.Info
                )
            )
        }

        val insights = mutableListOf<HealthInsight>()
        val heartRate = readings?.heartRate?.value
        val temperature = readings?.temperature?.value
        val bloodPressure = readings?.bloodPressure
        val samsungTemperature = readings?.temperature?.source.equals("SAMSUNG_HEALTH_DATA", ignoreCase = true)
        val activityState = inferActivityState(context)

        if (bloodPressure != null) {
            when {
                bloodPressure.systolic >= 180 || bloodPressure.diastolic >= 120 -> {
                    insights += HealthInsight(
                        title = "Very high blood pressure pattern",
                        possibleConcern = "Possible hypertensive crisis indicator.",
                        suggestion = "Rest and re-check. If it stays this high, or there is chest pain, weakness, confusion, severe headache, or shortness of breath, seek urgent medical help.",
                        severity = InsightSeverity.Urgent
                    )
                }
                bloodPressure.systolic >= 140 || bloodPressure.diastolic >= 90 -> {
                    insights += HealthInsight(
                        title = "High blood pressure pattern",
                        possibleConcern = "Possible hypertension indicator.",
                        suggestion = "Re-check when rested, avoid caffeine/exercise right before measuring, and discuss repeated high readings with a doctor or clinic.",
                        severity = InsightSeverity.Watch
                    )
                }
                bloodPressure.systolic < 90 || bloodPressure.diastolic < 60 -> {
                    insights += HealthInsight(
                        title = "Low blood pressure pattern",
                        possibleConcern = "Possible hypotension, dehydration, or medication-related pattern.",
                        suggestion = "Sit or lie down, drink water if safe for you, and seek care if there is fainting, dizziness, confusion, chest pain, or shortness of breath.",
                        severity = InsightSeverity.Watch
                    )
                }
            }
        }

        if (temperature != null) {
            if (activityState == ActivityState.Sleeping && samsungTemperature) {
                insights += HealthInsight(
                    title = "Sleep temperature trend available",
                    possibleConcern = "Galaxy Watch temperature is usually collected during sleep and is best treated as a trend signal.",
                    suggestion = "Use this as sleep-temperature context, not a direct fever reading. Re-check with a normal thermometer if you feel unwell, feverish, or symptoms continue.",
                    severity = InsightSeverity.Info
                )
            } else {
                when {
                    temperature >= 38.0 -> {
                        insights += HealthInsight(
                            title = "High temperature pattern",
                            possibleConcern = "Possible fever or infection indicator.",
                            suggestion = "Hydrate, rest, monitor symptoms, and contact a clinic if the fever is persistent, very high, or comes with worrying symptoms.",
                            severity = InsightSeverity.Watch
                        )
                    }
                    temperature < 35.0 -> {
                        insights += HealthInsight(
                            title = "Low temperature pattern",
                            possibleConcern = "Possible hypothermia or measurement issue.",
                            suggestion = "Warm up gradually and re-check with a reliable thermometer. Seek urgent care if the person is confused, shivering severely, very drowsy, or the low reading continues.",
                            severity = InsightSeverity.Urgent
                        )
                    }
                }
            }
        }

        if (heartRate != null) {
            when {
                heartRate > 100 && activityState == ActivityState.Exercising -> {
                    insights += HealthInsight(
                        title = "Exercise heart-rate context",
                        possibleConcern = "A faster pulse can be expected during or soon after training.",
                        suggestion = "Cool down, hydrate, and re-check after resting. If it stays high at rest or comes with chest pain, fainting, or shortness of breath, seek medical help.",
                        severity = InsightSeverity.Watch
                    )
                }
                heartRate > 100 && activityState == ActivityState.Resting -> {
                    insights += HealthInsight(
                        title = "Resting fast heart rate pattern",
                        possibleConcern = "Possible tachycardia indicator, stress response, dehydration, fever, or another body-stress pattern.",
                        suggestion = "Rest for a few minutes and re-check. If it remains high at rest, or there is chest pain, fainting, or shortness of breath, seek medical help.",
                        severity = InsightSeverity.Watch
                    )
                }
                heartRate > 100 -> {
                    insights += HealthInsight(
                        title = "Fast heart rate pattern",
                        possibleConcern = "Possible tachycardia indicator, stress response, dehydration, fever, or recent activity.",
                        suggestion = "Rest for a few minutes and re-check. If it remains high at rest, or there is chest pain, fainting, or shortness of breath, seek medical help.",
                        severity = InsightSeverity.Watch
                    )
                }
                heartRate < 60 && activityState == ActivityState.Sleeping -> {
                    insights += HealthInsight(
                        title = "Sleep heart-rate context",
                        possibleConcern = "Heart rate often drops during sleep, especially in fit people.",
                        suggestion = "Keep monitoring the trend. Seek care if there is dizziness, fainting, weakness, chest pain, or shortness of breath while awake.",
                        severity = InsightSeverity.Info
                    )
                }
                heartRate < 60 -> {
                    insights += HealthInsight(
                        title = "Slow heart rate pattern",
                        possibleConcern = "Possible bradycardia indicator, though this can be normal during sleep or in very fit people.",
                        suggestion = "Check whether the reading was taken at rest or during sleep. Seek care if there is dizziness, fainting, weakness, chest pain, or shortness of breath.",
                        severity = InsightSeverity.Watch
                    )
                }
            }
        }

        if (heartRate != null && temperature != null && heartRate > 100 && temperature >= 38.0
            && !(activityState == ActivityState.Sleeping && samsungTemperature)
        ) {
            insights += HealthInsight(
                title = "Fever with fast pulse pattern",
                possibleConcern = "Possible infection, dehydration, or body stress pattern.",
                suggestion = "Rest, hydrate, monitor temperature and pulse, and contact a clinic if symptoms worsen or the pattern continues.",
                severity = InsightSeverity.Watch
            )
        }

        if (insights.isEmpty()) {
            insights += HealthInsight(
                title = "No obvious warning pattern",
                possibleConcern = "The latest synced readings are within the simple screening ranges used by this app.",
                suggestion = "Keep monitoring trends. This app gives suggestions only and cannot rule out illness or replace a healthcare professional.",
                severity = InsightSeverity.Info
            )
        }

        return insights
    }

    private fun inferActivityState(context: ReadingContext): ActivityState {
        if (context.activityState != ActivityState.NotSure) {
            return context.activityState
        }

        val sleepStart = parseTime(context.sleepStart) ?: return ActivityState.NotSure
        val sleepEnd = parseTime(context.sleepEnd) ?: return ActivityState.NotSure
        return if (context.now.isInSleepWindow(sleepStart, sleepEnd)) {
            ActivityState.Sleeping
        } else {
            ActivityState.NotSure
        }
    }

    private fun parseTime(value: String?): LocalTime? {
        return runCatching {
            value?.takeIf { it.isNotBlank() }?.let { LocalTime.parse(it) }
        }.getOrNull()
    }

    private fun LocalTime.isInSleepWindow(start: LocalTime, end: LocalTime): Boolean {
        return if (start <= end) {
            this >= start && this <= end
        } else {
            this >= start || this <= end
        }
    }
}
