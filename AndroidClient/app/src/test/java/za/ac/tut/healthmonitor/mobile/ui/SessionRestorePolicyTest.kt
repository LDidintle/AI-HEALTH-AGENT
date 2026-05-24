package za.ac.tut.healthmonitor.mobile.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import za.ac.tut.healthmonitor.mobile.data.api.BackendHttpException

class SessionRestorePolicyTest {

    @Test
    fun clearsPersistedSessionOnlyWhenServerRejectsAuthentication() {
        assertTrue(SessionRestorePolicy.shouldClearSession(BackendHttpException(401, "Sign in again.")))
        assertFalse(SessionRestorePolicy.shouldClearSession(BackendHttpException(500, "Server unavailable.")))
        assertFalse(SessionRestorePolicy.shouldClearSession(IllegalStateException("Cannot reach service.")))
    }
}
