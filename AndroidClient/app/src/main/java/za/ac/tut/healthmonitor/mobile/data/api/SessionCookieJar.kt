package za.ac.tut.healthmonitor.mobile.data.api

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

class SessionCookieJar : CookieJar {

    private val cookieStore = ConcurrentHashMap<String, MutableList<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val currentCookies = cookieStore[url.host].orEmpty().toMutableList()

        cookies.forEach { incoming ->
            currentCookies.removeAll { existing ->
                existing.name == incoming.name &&
                    existing.domain == incoming.domain &&
                    existing.path == incoming.path
            }
            currentCookies.add(incoming)
        }

        cookieStore[url.host] = currentCookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        val validCookies = cookieStore[url.host]
            .orEmpty()
            .filter { it.expiresAt > now && it.matches(url) }

        cookieStore[url.host] = validCookies.toMutableList()
        return validCookies
    }

    fun clear() {
        cookieStore.clear()
    }
}
