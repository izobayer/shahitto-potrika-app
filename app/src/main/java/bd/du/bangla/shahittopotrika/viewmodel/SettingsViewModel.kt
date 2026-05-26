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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val fontScale = prefs.fontScale
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    val notificationsEnabled = prefs.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun toggleDarkMode() = viewModelScope.launch {
        prefs.setDarkMode(!isDarkMode.value)
    }

    fun setFontScale(scale: Float) = viewModelScope.launch {
        prefs.setFontScale(scale)
    }

    fun toggleNotifications() = viewModelScope.launch {
        prefs.setNotificationsEnabled(!notificationsEnabled.value)
    }
}
