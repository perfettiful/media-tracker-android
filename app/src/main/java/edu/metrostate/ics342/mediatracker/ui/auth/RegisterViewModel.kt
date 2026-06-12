package edu.metrostate.ics342.mediatracker.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RegisterViewModel(
    // kept around so we can wire userRepository.createAccount(...) when networking lands
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _displayName = MutableStateFlow("")
    val displayName = _displayName.asStateFlow()

    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword = _confirmPassword.asStateFlow()

    private val _registerState = MutableStateFlow<AuthViewModel.AuthUiState>(AuthViewModel.AuthUiState.Idle)
    val registerState = _registerState.asStateFlow()

    fun setDisplayName(newValue: String)     { _displayName.value     = newValue; clearError() }
    fun setUsername(newValue: String)        { _username.value        = newValue; clearError() }
    fun setEmail(newValue: String)           { _email.value           = newValue; clearError() }
    fun setPassword(newValue: String)        { _password.value        = newValue; clearError() }
    fun setConfirmPassword(newValue: String) { _confirmPassword.value = newValue; clearError() }

    private fun clearError() {
        if (_registerState.value is AuthViewModel.AuthUiState.Error) {
            _registerState.value = AuthViewModel.AuthUiState.Idle
        }
    }

    fun onSignUpClicked() {
        val name    = _displayName.value.trim()
        val user    = _username.value.trim()
        val em      = _email.value.trim()
        val pw      = _password.value
        val confirm = _confirmPassword.value

        val validation = when {
            name.isBlank() || user.isBlank() || em.isBlank() ||
                pw.isBlank() || confirm.isBlank()         -> R.string.error_empty_fields
            !Patterns.EMAIL_ADDRESS.matcher(em).matches() -> R.string.error_invalid_email
            user.length < 3                               -> R.string.error_username_too_short
            pw.length < 8                                 -> R.string.error_password_too_short
            pw != confirm                                 -> R.string.error_passwords_dont_match
            else -> null
        }
        if (validation != null) {
            _registerState.value = AuthViewModel.AuthUiState.Error(validation)
            return
        }

        // validation passed. real network call goes here once we wire it.
        // for now the screen catches Success and shows a "not implemented" snackbar.
        _registerState.value = AuthViewModel.AuthUiState.Success
    }

    fun resetRegisterState() { _registerState.value = AuthViewModel.AuthUiState.Idle }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { RegisterViewModel(UserRepository()) }
        }
    }
}
