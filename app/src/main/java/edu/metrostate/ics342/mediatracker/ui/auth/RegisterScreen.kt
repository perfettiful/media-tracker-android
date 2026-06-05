package edu.metrostate.ics342.mediatracker.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.theme.OnPrimaryContainer
import edu.metrostate.ics342.mediatracker.theme.PrimaryContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val displayNameState     = rememberTextFieldState()
    val usernameState        = rememberTextFieldState()
    val emailState           = rememberTextFieldState()
    val passwordState        = rememberTextFieldState()
    val confirmPasswordState = rememberTextFieldState()

    val state by viewModel.registerState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state) {
        if (state is AuthUiState.Success) {
            viewModel.resetRegisterState()
            onRegisterSuccess()
        }
    }

    // clear the error as soon as they start fixing whichever field
    LaunchedEffect(displayNameState, usernameState, emailState, passwordState, confirmPasswordState) {
        snapshotFlow {
            displayNameState.text.toString() + "|" +
            usernameState.text.toString() + "|" +
            emailState.text.toString() + "|" +
            passwordState.text.toString() + "|" +
            confirmPasswordState.text.toString()
        }.collect { viewModel.clearRegisterError() }
    }

    val isLoading = state is AuthUiState.Loading
    val errorMsg  = (state as? AuthUiState.Error)?.msgResId?.let { stringResource(it) }

    fun submit() {
        focusManager.clearFocus()
        viewModel.onSignUpClicked(
            displayName     = displayNameState.text.toString(),
            username        = usernameState.text.toString(),
            email           = emailState.text.toString(),
            password        = passwordState.text.toString(),
            confirmPassword = confirmPasswordState.text.toString(),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // smaller header than login (60dp vs 72dp per wireframe)
        Image(
            painter = painterResource(id = R.drawable.smart_display),
            contentDescription = null,
            modifier = Modifier
                .size(width = 60.dp, height = 60.dp)
                .background(color = PrimaryContainer, shape = RoundedCornerShape(16.dp))
                .padding(all = 16.dp),
            colorFilter = ColorFilter.tint(color = OnPrimaryContainer),
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text  = stringResource(R.string.create_account_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text  = stringResource(R.string.create_account_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(28.dp))

        TextField(
            state       = displayNameState,
            modifier    = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.display_name_label)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            onKeyboardAction = { focusManager.moveFocus(FocusDirection.Down) },
            lineLimits  = TextFieldLineLimits.SingleLine,
        )

        Spacer(Modifier.height(10.dp))

        TextField(
            state       = usernameState,
            modifier    = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.username_label)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            onKeyboardAction = { focusManager.moveFocus(FocusDirection.Down) },
            lineLimits  = TextFieldLineLimits.SingleLine,
        )

        Spacer(Modifier.height(10.dp))

        TextField(
            state       = emailState,
            modifier    = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.email_label)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction    = ImeAction.Next,
            ),
            onKeyboardAction = { focusManager.moveFocus(FocusDirection.Down) },
            lineLimits  = TextFieldLineLimits.SingleLine,
        )

        Spacer(Modifier.height(10.dp))

        SecureTextField(
            state       = passwordState,
            modifier    = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.password_label)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            onKeyboardAction = { focusManager.moveFocus(FocusDirection.Down) },
        )

        Spacer(Modifier.height(10.dp))

        SecureTextField(
            state       = confirmPasswordState,
            modifier    = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.confirm_password_label)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            onKeyboardAction = { submit() },
        )

        if (errorMsg != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text  = errorMsg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick  = { submit() },
            enabled  = !isLoading,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color       = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(stringResource(R.string.register_action))
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text  = stringResource(R.string.login_prompt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text       = stringResource(R.string.sign_in_button),
                style      = MaterialTheme.typography.bodyMedium,
                color      = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.clickable(onClick = onNavigateToLogin)
            )
        }
    }
}
