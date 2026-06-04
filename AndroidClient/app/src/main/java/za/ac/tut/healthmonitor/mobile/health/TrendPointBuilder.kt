package za.ac.tut.healthmonitor.mobile.health

internal fun buildAlignedTrendPoints(
    heartRates: List<Int>,
    systolics: List<Int>,
    temperatures: List<Double>,
    maxPoints: Int
): List<HealthSectionTrendPoint> {
    val latestHeartRates = heartRates.takeLast(maxPoints)
    val latestSystolics = systolics.takeLast(maxPoints)
    val latestTemperatures = temperatures.takeLast(maxPoints)
    val pointCount = maxOf(latestHeartRates.size, latestSystolics.size, latestTemperatures.size)
    if (pointCount == 0) {
        return emptyList()
    }

    return (0 until pointCount).map { index ->
        HealthSectionTrendPoint(
            heartRate = alignedValue(latestHeartRates, pointCount, index),
            systolic = alignedValue(latestSystolics, pointCount, index),
            temperature = alignedValue(latestTemperatures, pointCount, index)
        )
    }
}

private fun <T> alignedValue(values: List<T>, pointCount: Int, index: Int): T? {
    val firstValueIndex = pointCount - values.size
    return if (index >= firstValueIndex) {
        values[index - firstValueIndex]
    } else {
        null
    }
}
