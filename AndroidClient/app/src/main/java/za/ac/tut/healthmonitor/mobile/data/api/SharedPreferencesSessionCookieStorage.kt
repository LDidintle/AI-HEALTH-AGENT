package za.ac.tut.healthmonitor.mobile.data.api

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SharedPreferencesSessionCookieStorage(context: Context) : SessionCookieStorage {

    private val preferences = encryptedPreferences(context.applicationContext)

    override fun load(): List<StoredSessionCookie> {
        return preferences.getStringSet(KEY_COOKIES, emptySet())
            .orEmpty()
            .mapNotNull { encoded -> encoded.toStoredCookieOrNull() }
    }

    override fun save(cookies: List<StoredSessionCookie>) {
        preferences.edit()
            .putStringSet(KEY_COOKIES, cookies.map { it.encode() }.toSet())
            .apply()
    }

    override fun clear() {
        preferences.edit()
            .remove(KEY_COOKIES)
            .apply()
    }

    private fun StoredSessionCookie.encode(): String {
        return host + SEPARATOR + setCookieHeader
    }

    private fun String.toStoredCookieOrNull(): StoredSessionCookie? {
        val separatorIndex = indexOf(SEPARATOR)
        if (separatorIndex <= 0 || separatorIndex == lastIndex) {
            return null
        }

        return StoredSessionCookie(
            host = substring(0, separatorIndex),
            setCookieHeader = substring(separatorIndex + SEPARATOR.length)
        )
    }

    private fun encryptedPreferences(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFERENCES_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private companion object {
        const val PREFERENCES_NAME = "smarthealth_session"
        const val KEY_COOKIES = "cookies"
        const val SEPARATOR = "\n"
    }
}
