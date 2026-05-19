package za.ac.tut.healthmonitor.mobile.data.api

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.util.concurrent.ConcurrentHashMap

class SessionCookieJar(
    private val storage: SessionCookieStorage? = null
) : CookieJar {

    private val cookieStore = ConcurrentHashMap<String, MutableList<Cookie>>()

    init {
        storage?.load()?.forEach { storedCookie ->
            val restoreUrl = "https://${storedCookie.host}/".toHttpUrlOrNull() ?: return@forEach
            val cookie = Cookie.parse(restoreUrl, storedCookie.setCookieHeader) ?: return@forEach
            val hostCookies = cookieStore[storedCookie.host].orEmpty().toMutableList()
            hostCookies.add(cookie)
            cookieStore[storedCookie.host] = hostCookies
        }
    }

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
        persist()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        val validCookies = cookieStore[url.host]
            .orEmpty()
            .filter { it.expiresAt > now && it.matches(url) }

        cookieStore[url.host] = validCookies.toMutableList()
        persist()
        return validCookies
    }

    fun clear() {
        cookieStore.clear()
        storage?.clear()
    }

    private fun persist() {
        val storedCookies = cookieStore.flatMap { (host, cookies) ->
            cookies.map { cookie ->
                StoredSessionCookie(
                    host = host,
                    setCookieHeader = cookie.toString()
                )
            }
        }
        storage?.save(storedCookies)
    }
}

interface SessionCookieStorage {
    fun load(): List<StoredSessionCookie>
    fun save(cookies: List<StoredSessionCookie>)
    fun clear()
}

data class StoredSessionCookie(
    val host: String,
    val setCookieHeader: String
)
