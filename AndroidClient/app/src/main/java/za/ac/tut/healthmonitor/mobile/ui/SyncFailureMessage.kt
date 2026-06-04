package za.ac.tut.healthmonitor.mobile.ui

import za.ac.tut.healthmonitor.mobile.data.api.BackendHttpException

object SyncFailureMessage {

    fun from(sectionError: Throwable?, fallbackError: Throwable?, refreshError: Throwable?): String {
        if (isAuthFailure(sectionError) || isAuthFailure(fallbackError) || isAuthFailure(refreshError)) {
            return expiredSessionMessage()
        }
        if (sectionError == null && fallbackError == null && refreshError != null) {
            return "Readings saved, but latest refresh failed: ${clean(refreshError)}"
        }
        if (sectionError != null && fallbackError == null && refreshError == null) {
            return "Latest readings saved through fallback, but section save failed: ${clean(sectionError)}"
        }

        val parts = mutableListOf<String>()
        sectionError?.let { parts += "section save failed: ${clean(it)}" }
        fallbackError?.let { parts += "fallback reading save failed: ${clean(it)}" }
        refreshError?.let { parts += "latest refresh failed: ${clean(it)}" }
        return if (parts.isEmpty()) {
            "Section displayed, but saving to the database failed."
        } else {
            "Section displayed, but ${parts.joinToString("; ")}"
        }
    }

    private fun clean(error: Throwable): String {
        return error.message?.takeIf { it.isNotBlank() } ?: "unknown error"
    }

    fun isAuthFailure(error: Throwable?): Boolean {
        return error is BackendHttpException && error.statusCode == 401
    }

    fun expiredSessionMessage(): String {
        return "Your SmartHealth session expired before the watch section could be saved. Sign in again, then the next automatic sync will save fresh readings."
    }
}
