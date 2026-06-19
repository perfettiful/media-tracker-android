package edu.metrostate.ics342.mediatracker.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.metrostate.ics342.mediatracker.MediaTrackerApp
import edu.metrostate.ics342.mediatracker.data.FakeMediaRepository
import edu.metrostate.ics342.mediatracker.data.TokenStore
import edu.metrostate.ics342.mediatracker.data.model.LibraryItem
import edu.metrostate.ics342.mediatracker.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val tokenStore: TokenStore,
) : ViewModel() {

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _libraryPreview = MutableStateFlow<List<LibraryItem>>(emptyList())
    val libraryPreview: StateFlow<List<LibraryItem>> = _libraryPreview.asStateFlow()

    private val _editDisplayName = MutableStateFlow("")
    val editDisplayName: StateFlow<String> = _editDisplayName.asStateFlow()

    private val _editUsername = MutableStateFlow("")
    val editUsername: StateFlow<String> = _editUsername.asStateFlow()

    private val _editBio = MutableStateFlow("")
    val editBio: StateFlow<String> = _editBio.asStateFlow()

    init {
        viewModelScope.launch {
            // show the signed in user if we have one, fall back to the fake profile
            val user = tokenStore.currentUser.first() ?: FakeMediaRepository.currentUser
            _currentUser.value     = user
            _editDisplayName.value = user.displayName
            _editUsername.value    = user.username
            _editBio.value         = user.bio ?: ""
        }
        _libraryPreview.value = FakeMediaRepository.libraryItems.take(6)
    }

    fun onEditDisplayNameChange(value: String) { _editDisplayName.value = value }
    fun onEditUsernameChange(value: String)    { _editUsername.value    = value }
    fun onEditBioChange(value: String)          { _editBio.value        = value }

    fun saveProfile() {
        // TODO (Week 10): Call PUT /users/me with Retrofit
        _currentUser.value = _currentUser.value?.copy(
            displayName = _editDisplayName.value,
            username    = _editUsername.value,
            bio         = _editBio.value.ifBlank { null }
        )
    }

    fun loadUserById(userId: String): UserProfile? {
        // TODO (Week 10): Call GET /users/{id} with Retrofit
        return FakeMediaRepository.followers.find { it.id == userId }
            ?: FakeMediaRepository.following.find { it.id == userId }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MediaTrackerApp
                ProfileViewModel(app.tokenStore)
            }
        }
    }
}
