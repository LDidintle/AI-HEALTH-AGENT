package za.ac.tut.healthmonitor.mobile.insights

import android.content.Context

class UserContextSettingsStorage(context: Context) {

    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun sleepStart(): String {
        return preferences.getString(KEY_SLEEP_START, DEFAULT_SLEEP_START) ?: DEFAULT_SLEEP_START
    }

    fun sleepEnd(): String {
        return preferences.getString(KEY_SLEEP_END, DEFAULT_SLEEP_END) ?: DEFAULT_SLEEP_END
    }

    fun saveSleepStart(value: String) {
        preferences.edit().putString(KEY_SLEEP_START, value).apply()
    }

    fun saveSleepEnd(value: String) {
        preferences.edit().putString(KEY_SLEEP_END, value).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "smarthealth_context_settings"
        const val KEY_SLEEP_START = "sleep_start"
        const val KEY_SLEEP_END = "sleep_end"
        const val DEFAULT_SLEEP_START = "22:00"
        const val DEFAULT_SLEEP_END = "06:30"
    }
}
