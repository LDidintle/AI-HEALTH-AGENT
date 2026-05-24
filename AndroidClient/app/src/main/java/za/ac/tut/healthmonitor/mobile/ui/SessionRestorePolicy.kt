package za.ac.tut.healthmonitor.mobile.ui

import za.ac.tut.healthmonitor.mobile.data.api.BackendHttpException

internal object SessionRestorePolicy {

    fun shouldClearSession(error: Throwable): Boolean {
        return error is BackendHttpException && error.statusCode == 401
    }
}
