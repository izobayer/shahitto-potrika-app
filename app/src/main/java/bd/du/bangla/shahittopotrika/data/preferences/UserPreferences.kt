package bd.du.bangla.shahittopotrika.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    private object Keys {
        val DARK_MODE   = booleanPreferencesKey("dark_mode")
        val FONT_SCALE  = floatPreferencesKey("font_scale")
        val LAST_ISSUE  = stringPreferencesKey("last_issue_id")
        val NOTIF_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.DARK_MODE] ?: false }

    val fontScale: Flow<Float> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.FONT_SCALE] ?: 1.0f }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.NOTIF_ENABLED] ?: true }

    val lastSeenIssueId: Flow<String> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.LAST_ISSUE] ?: "" }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setFontScale(scale: Float) {
        context.dataStore.edit { it[Keys.FONT_SCALE] = scale }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIF_ENABLED] = enabled }
    }

    suspend fun setLastSeenIssueId(id: String) {
        context.dataStore.edit { it[Keys.LAST_ISSUE] = id }
    }
}
