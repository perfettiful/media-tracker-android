package edu.metrostate.ics342.mediatracker.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val _registerState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val registerState: StateFlow<AuthUiState> = _registerState.asStateFlow()

    fun clearRegisterError() {
        if (_registerState.value is AuthUiState.Error) _registerState.value = AuthUiState.Idle
    }

    fun onSignUpClicked(
        displayName: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
    ) {
        val name    = displayName.trim()
        val user    = username.trim()
        val em      = email.trim()
        val pw      = password
        val confirm = confirmPassword

        val validation = when {
            name.isBlank() || user.isBlank() || em.isBlank() ||
                pw.isBlank() || confirm.isBlank()    -> R.string.error_empty_fields
            !Patterns.EMAIL_ADDRESS.matcher(em).matches() -> R.string.error_invalid_email
            user.length < 3                           -> R.string.error_username_too_short
            pw.length < 8                             -> R.string.error_password_too_short
            pw != confirm                             -> R.string.error_passwords_dont_match
            else -> null
        }

        if (validation != null) {
            _registerState.value = AuthUiState.Error(validation)
            return
        }

        viewModelScope.launch {
            _registerState.value = AuthUiState.Loading
            delay(800) // fake network for now, swap for repo.register(...) once /users is wired
            _registerState.value = AuthUiState.Success
        }
    }

    fun resetRegisterState() { _registerState.value = AuthUiState.Idle }
}
