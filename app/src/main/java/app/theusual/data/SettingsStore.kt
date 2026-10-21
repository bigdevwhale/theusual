package app.theusual.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

data class Settings(
    val onboarded: Boolean = false,
    /** The name signed under a packet sent to a friend. */
    val name: String = "",
    /** Written after prices: "₽", "€", "$"; blank hides prices. */
    val currency: String = "",
    /** The city new cafés are in, unless said otherwise. */
    val city: String = "",
)

class SettingsStore(context: Context) {
    private val store = context.applicationContext.dataStore

    val settings: Flow<Settings> = store.data.map { it.read() }

    private fun Preferences.read() = Settings(
        onboarded = this[ONBOARDED] ?: false,
        name = this[NAME] ?: "",
        currency = this[CURRENCY] ?: "",
        city = this[CITY] ?: "",
    )

    suspend fun now(): Settings = settings.first()

    suspend fun update(change: (Settings) -> Settings) {
        store.edit {
            val s = change(it.read())
            it[ONBOARDED] = s.onboarded
            it[NAME] = s.name
            it[CURRENCY] = s.currency
            it[CITY] = s.city
        }
    }

    private companion object {
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val NAME = stringPreferencesKey("name")
        val CURRENCY = stringPreferencesKey("currency")
        val CITY = stringPreferencesKey("city")
    }
}
