package edu.metrostate.ics342.mediatracker.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.metrostate.ics342.mediatracker.MediaTrackerApp
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.LoginResult
import edu.metrostate.ics342.mediatracker.data.UserRepository
import edu.metrostate.ics342.mediatracker.data.network.DefaultUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    sealed class AuthUiState {
        data object Idle    : AuthUiState()
        data object Loading : AuthUiState()
        data object Success : AuthUiState()
        data class Error(val msgResId: Int) : AuthUiState()
    }

    private val _email    = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _loginState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val loginState: StateFlow<AuthUiState> = _loginState.asStateFlow()

    fun onEmailChange(value: String)    { _email.value    = value }
    fun onPasswordChange(value: String) { _password.value = value }

    fun onLoginClick() {
        val em = _email.value.trim()
        // trim, the soft keyboard likes to tack a trailing space on and the server 401s
        val pw = _password.value.trim()
        if (em.isBlank() || pw.isBlank()) {
            _loginState.value = AuthUiState.Error(R.string.error_empty_credentials)
            return
        }

        viewModelScope.launch {
            _loginState.value = AuthUiState.Loading
            val result = userRepository.login(em, pw)
            _loginState.value = when (result) {
                LoginResult.Success            -> AuthUiState.Success
                LoginResult.InvalidCredentials -> AuthUiState.Error(R.string.error_invalid_login)
                LoginResult.NetworkError       -> AuthUiState.Error(R.string.error_network)
                LoginResult.UnknownError       -> AuthUiState.Error(R.string.error_login_failed)
            }
        }
    }

    fun resetLoginState() { _loginState.value = AuthUiState.Idle }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MediaTrackerApp
                AuthViewModel(DefaultUserRepository(app.tokenStore))
            }
        }
    }
}
