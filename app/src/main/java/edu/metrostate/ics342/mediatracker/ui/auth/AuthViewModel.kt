package edu.metrostate.ics342.mediatracker.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.metrostate.ics342.mediatracker.MediaTrackerApp
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.TokenStore
import edu.metrostate.ics342.mediatracker.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AuthViewModel(
    private val tokenStore: TokenStore,
    private val userRepository: UserRepository,
) : ViewModel() {

    sealed class AuthUiState {
        object Idle    : AuthUiState()
        object Loading : AuthUiState()
        object Success : AuthUiState()
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
        val pw = _password.value
        if (em.isBlank() || pw.isBlank()) {
            _loginState.value = AuthUiState.Error(R.string.error_empty_credentials)
            return
        }

        viewModelScope.launch {
            _loginState.value = AuthUiState.Loading
            try {
                val tokens = userRepository.login(em, pw)
                tokenStore.saveTokens(tokens.accessToken, tokens.refreshToken)
                tokenStore.saveUser(tokens.user)
                _loginState.value = AuthUiState.Success
            } catch (e: HttpException) {
                // 401 is bad creds, anything else is a server hiccup
                val msg = if (e.code() == 401) R.string.error_invalid_login
                          else R.string.error_login_failed
                _loginState.value = AuthUiState.Error(msg)
            } catch (e: IOException) {
                _loginState.value = AuthUiState.Error(R.string.error_network)
            } catch (e: Exception) {
                _loginState.value = AuthUiState.Error(R.string.error_login_failed)
            }
        }
    }

    fun resetLoginState() { _loginState.value = AuthUiState.Idle }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MediaTrackerApp
                AuthViewModel(app.tokenStore, UserRepository())
            }
        }
    }
}
