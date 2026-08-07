package edu.metrostate.ics342.mediatracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.metrostate.ics342.mediatracker.MediaTrackerApp
import edu.metrostate.ics342.mediatracker.data.TokenStore
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val tokenStore: TokenStore,
) : ViewModel() {

    // wipe the saved tokens and profile before we send them back to login,
    // otherwise the next person to open the app is still signed in as you
    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            tokenStore.clear()
            onDone()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MediaTrackerApp
                SettingsViewModel(app.tokenStore)
            }
        }
    }
}
