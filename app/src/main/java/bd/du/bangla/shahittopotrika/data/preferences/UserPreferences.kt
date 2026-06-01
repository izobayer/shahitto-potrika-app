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
        val DARK_MODE            = booleanPreferencesKey("dark_mode")
        val FONT_SCALE           = floatPreferencesKey("font_scale")
        val LAST_ISSUE           = stringPreferencesKey("last_issue_id")
        val NOTIF_ENABLED        = booleanPreferencesKey("notifications_enabled")
        val NOTIF_UPDATE         = booleanPreferencesKey("notifications_app_update")
        val OFFLINE_CACHE        = booleanPreferencesKey("offline_cache_enabled")
        val HISTORY_TRACKING     = booleanPreferencesKey("history_tracking_enabled")
        val OPEN_PDF_EXTERNAL    = booleanPreferencesKey("open_pdf_external")
        val SHOW_ABSTRACT        = booleanPreferencesKey("show_abstract_in_list")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.DARK_MODE] ?: true }

    val fontScale: Flow<Float> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.FONT_SCALE] ?: 1.15f }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.NOTIF_ENABLED] ?: true }

    val notificationsAppUpdate: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.NOTIF_UPDATE] ?: true }

    val offlineCacheEnabled: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.OFFLINE_CACHE] ?: true }

    val historyTrackingEnabled: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.HISTORY_TRACKING] ?: true }

    val openPdfExternal: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.OPEN_PDF_EXTERNAL] ?: false }

    val showAbstractInList: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.SHOW_ABSTRACT] ?: true }

    val lastSeenIssueId: Flow<String> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.LAST_ISSUE] ?: "" }

    suspend fun setDarkMode(enabled: Boolean)              { context.dataStore.edit { it[Keys.DARK_MODE] = enabled } }
    suspend fun setFontScale(scale: Float)                 { context.dataStore.edit { it[Keys.FONT_SCALE] = scale } }
    suspend fun setNotificationsEnabled(enabled: Boolean)  { context.dataStore.edit { it[Keys.NOTIF_ENABLED] = enabled } }
    suspend fun setNotificationsAppUpdate(enabled: Boolean){ context.dataStore.edit { it[Keys.NOTIF_UPDATE] = enabled } }
    suspend fun setOfflineCacheEnabled(enabled: Boolean)   { context.dataStore.edit { it[Keys.OFFLINE_CACHE] = enabled } }
    suspend fun setHistoryTrackingEnabled(enabled: Boolean){ context.dataStore.edit { it[Keys.HISTORY_TRACKING] = enabled } }
    suspend fun setOpenPdfExternal(enabled: Boolean)       { context.dataStore.edit { it[Keys.OPEN_PDF_EXTERNAL] = enabled } }
    suspend fun setShowAbstractInList(enabled: Boolean)    { context.dataStore.edit { it[Keys.SHOW_ABSTRACT] = enabled } }
    suspend fun setLastSeenIssueId(id: String)             { context.dataStore.edit { it[Keys.LAST_ISSUE] = id } }
}
