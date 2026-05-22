package za.ac.tut.healthmonitor.mobile.ui

import org.junit.Assert.assertTrue
import org.junit.Test

class SyncFailureMessageTest {

    @Test
    fun explainsSectionAndFallbackFailures() {
        val message = SyncFailureMessage.from(
            sectionError = IllegalStateException("Unable to synchronize health section."),
            fallbackError = IllegalStateException("Unable to synchronize health readings."),
            refreshError = null
        )

        assertTrue(message.contains("section save failed"))
        assertTrue(message.contains("fallback reading save failed"))
    }

    @Test
    fun explainsRefreshFailureAfterSuccessfulSave() {
        val message = SyncFailureMessage.from(
            sectionError = null,
            fallbackError = null,
            refreshError = IllegalStateException("Unable to read synchronized data.")
        )

        assertTrue(message.contains("saved"))
        assertTrue(message.contains("latest refresh failed"))
    }
}
