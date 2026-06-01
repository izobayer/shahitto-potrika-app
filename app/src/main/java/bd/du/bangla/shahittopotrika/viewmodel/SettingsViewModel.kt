package bd.du.bangla.shahittopotrika.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import bd.du.bangla.shahittopotrika.ShahittoPotrikaApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = (app as ShahittoPotrikaApplication).repository.prefs

    val isDarkMode = prefs.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val fontScale = prefs.fontScale
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.15f)

    val notificationsEnabled = prefs.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val notificationsAppUpdate = prefs.notificationsAppUpdate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val offlineCacheEnabled = prefs.offlineCacheEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val historyTrackingEnabled = prefs.historyTrackingEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val openPdfExternal = prefs.openPdfExternal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val showAbstractInList = prefs.showAbstractInList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun toggleDarkMode()           = viewModelScope.launch { prefs.setDarkMode(!isDarkMode.value) }
    fun setFontScale(scale: Float) = viewModelScope.launch { prefs.setFontScale(scale) }
    fun toggleNotifications()      = viewModelScope.launch { prefs.setNotificationsEnabled(!notificationsEnabled.value) }
    fun toggleNotifUpdate()        = viewModelScope.launch { prefs.setNotificationsAppUpdate(!notificationsAppUpdate.value) }
    fun toggleOfflineCache()       = viewModelScope.launch { prefs.setOfflineCacheEnabled(!offlineCacheEnabled.value) }
    fun toggleHistoryTracking()    = viewModelScope.launch { prefs.setHistoryTrackingEnabled(!historyTrackingEnabled.value) }
    fun toggleOpenPdfExternal()    = viewModelScope.launch { prefs.setOpenPdfExternal(!openPdfExternal.value) }
    fun toggleShowAbstract()       = viewModelScope.launch { prefs.setShowAbstractInList(!showAbstractInList.value) }
}
