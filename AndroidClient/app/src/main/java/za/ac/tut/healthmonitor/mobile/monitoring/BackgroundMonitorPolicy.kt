package za.ac.tut.healthmonitor.mobile.monitoring

object BackgroundMonitorPolicy {
    const val MONITOR_INTERVAL_MILLIS = 15L * 60L * 1000L

    fun shouldRun(lastRunAtMillis: Long, nowMillis: Long): Boolean {
        return lastRunAtMillis <= 0L || nowMillis - lastRunAtMillis >= MONITOR_INTERVAL_MILLIS
    }
}
