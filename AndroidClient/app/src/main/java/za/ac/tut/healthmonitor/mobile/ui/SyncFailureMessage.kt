package za.ac.tut.healthmonitor.mobile.ui

object SyncFailureMessage {

    fun from(sectionError: Throwable?, fallbackError: Throwable?, refreshError: Throwable?): String {
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
}
