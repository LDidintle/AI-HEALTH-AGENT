package za.ac.tut.healthmonitor.mobile.data.api

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SharedPreferencesSessionCookieStorage(context: Context) : SessionCookieStorage {

    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    override fun load(): List<StoredSessionCookie> {
        val encryptedCookies = preferences.getString(KEY_COOKIES_BLOB, null)
        if (encryptedCookies != null) {
            return decrypt(encryptedCookies)
                ?.split(RECORD_SEPARATOR)
                .orEmpty()
                .filter { it.isNotBlank() }
                .mapNotNull { encoded -> encoded.toStoredCookieOrNull() }
        }

        return preferences.getStringSet(LEGACY_KEY_COOKIES, emptySet())
            .orEmpty()
            .mapNotNull { encoded -> encoded.toStoredCookieOrNull() }
    }

    override fun save(cookies: List<StoredSessionCookie>) {
        if (cookies.isEmpty()) {
            clear()
            return
        }

        val encodedCookies = cookies.joinToString(RECORD_SEPARATOR) { it.encode() }
        preferences.edit()
            .putString(KEY_COOKIES_BLOB, encrypt(encodedCookies))
            .remove(LEGACY_KEY_COOKIES)
            .apply()
    }

    override fun clear() {
        preferences.edit()
            .remove(KEY_COOKIES_BLOB)
            .remove(LEGACY_KEY_COOKIES)
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

    private fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))
        return listOf(cipher.iv, encrypted).joinToString(BLOB_SEPARATOR) { bytes ->
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        }
    }

    private fun decrypt(blob: String): String? {
        return runCatching {
            val parts = blob.split(BLOB_SEPARATOR)
            if (parts.size != 2) {
                return null
            }

            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val encrypted = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
            String(cipher.doFinal(encrypted), StandardCharsets.UTF_8)
        }.getOrNull()
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()
        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }

    private companion object {
        const val PREFERENCES_NAME = "smarthealth_session"
        const val KEY_COOKIES_BLOB = "cookies_blob"
        const val LEGACY_KEY_COOKIES = "cookies"
        const val SEPARATOR = "\n"
        const val RECORD_SEPARATOR = "\u001E"
        const val BLOB_SEPARATOR = ":"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "smarthealth_session_cookie_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_BITS = 128
    }
}
