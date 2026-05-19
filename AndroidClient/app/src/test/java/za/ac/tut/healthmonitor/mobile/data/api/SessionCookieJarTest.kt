package za.ac.tut.healthmonitor.mobile.data.api

import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionCookieJarTest {

    private val baseUrl = "https://ai-health-helper.onrender.com/".toHttpUrl()

    @Test
    fun loadForRequestReturnsSavedMatchingCookies() {
        val jar = SessionCookieJar()
        val cookie = sessionCookie("JSESSIONID", "abc123")

        jar.saveFromResponse(baseUrl, listOf(cookie))

        assertEquals(listOf(cookie), jar.loadForRequest(baseUrl))
    }

    @Test
    fun saveFromResponseReplacesCookieWithSameNameDomainAndPath() {
        val jar = SessionCookieJar()
        val oldCookie = sessionCookie("JSESSIONID", "old")
        val newCookie = sessionCookie("JSESSIONID", "new")

        jar.saveFromResponse(baseUrl, listOf(oldCookie))
        jar.saveFromResponse(baseUrl, listOf(newCookie))

        assertEquals(listOf(newCookie), jar.loadForRequest(baseUrl))
    }

    @Test
    fun loadForRequestDropsExpiredCookies() {
        val jar = SessionCookieJar()
        val expiredCookie = Cookie.Builder()
            .name("JSESSIONID")
            .value("expired")
            .domain("ai-health-helper.onrender.com")
            .path("/")
            .expiresAt(System.currentTimeMillis() - 1_000)
            .build()

        jar.saveFromResponse(baseUrl, listOf(expiredCookie))

        assertTrue(jar.loadForRequest(baseUrl).isEmpty())
    }

    @Test
    fun clearRemovesSavedCookies() {
        val jar = SessionCookieJar()
        jar.saveFromResponse(baseUrl, listOf(sessionCookie("JSESSIONID", "abc123")))

        jar.clear()

        assertTrue(jar.loadForRequest(baseUrl).isEmpty())
    }

    @Test
    fun savedCookiesCanBeRestoredFromStorage() {
        val storage = InMemorySessionCookieStorage()
        val firstJar = SessionCookieJar(storage)
        val cookie = sessionCookie("JSESSIONID", "abc123")

        firstJar.saveFromResponse(baseUrl, listOf(cookie))

        val restoredJar = SessionCookieJar(storage)
        assertEquals(listOf(cookie), restoredJar.loadForRequest(baseUrl))
    }

    @Test
    fun clearRemovesPersistedCookies() {
        val storage = InMemorySessionCookieStorage()
        val jar = SessionCookieJar(storage)
        jar.saveFromResponse(baseUrl, listOf(sessionCookie("JSESSIONID", "abc123")))

        jar.clear()

        val restoredJar = SessionCookieJar(storage)
        assertTrue(restoredJar.loadForRequest(baseUrl).isEmpty())
    }

    private fun sessionCookie(name: String, value: String): Cookie {
        return Cookie.Builder()
            .name(name)
            .value(value)
            .domain("ai-health-helper.onrender.com")
            .path("/")
            .build()
    }

    private class InMemorySessionCookieStorage : SessionCookieStorage {
        private var cookies: List<StoredSessionCookie> = emptyList()

        override fun load(): List<StoredSessionCookie> {
            return cookies
        }

        override fun save(cookies: List<StoredSessionCookie>) {
            this.cookies = cookies
        }

        override fun clear() {
            cookies = emptyList()
        }
    }
}
