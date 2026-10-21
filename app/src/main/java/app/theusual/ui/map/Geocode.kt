package app.theusual.ui.map

import app.theusual.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Finds an address on OpenStreetMap (Nominatim), so the map picker opens on the café's street.
 * One request when the picker opens; nothing is sent otherwise.
 */
object Geocode {
    suspend fun find(query: String, lang: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext null
        runCatching {
            val url = URL("https://nominatim.openstreetmap.org/search?format=json&limit=1&q=" + URLEncoder.encode(query, "UTF-8"))
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", "TheUsual/${BuildConfig.VERSION_NAME} (${BuildConfig.APPLICATION_ID})")
            conn.setRequestProperty("Accept-Language", lang)
            conn.connectTimeout = 8_000
            conn.readTimeout = 8_000
            conn.inputStream.bufferedReader().use { r ->
                val a = JSONArray(r.readText())
                if (a.length() == 0) null else a.getJSONObject(0).let { it.getString("lat").toDouble() to it.getString("lon").toDouble() }
            }.also { conn.disconnect() }
        }.getOrNull()
    }
}
