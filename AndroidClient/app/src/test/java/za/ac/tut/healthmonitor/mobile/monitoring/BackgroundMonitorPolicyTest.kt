package za.ac.tut.healthmonitor.mobile.monitoring

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackgroundMonitorPolicyTest {

    @Test
    fun monitorIntervalIsBatteryConservative() {
        assertTrue(BackgroundMonitorPolicy.MONITOR_INTERVAL_MILLIS >= 15L * 60L * 1000L)
    }

    @Test
    fun shouldRunOnlyAfterIntervalPasses() {
        val now = 60L * 60L * 1000L

        assertTrue(BackgroundMonitorPolicy.shouldRun(lastRunAtMillis = 0L, nowMillis = now))
        assertFalse(
            BackgroundMonitorPolicy.shouldRun(
                lastRunAtMillis = now - 5L * 60L * 1000L,
                nowMillis = now
            )
        )
    }
}
