package edu.metrostate.ics342.mediatracker.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.theme.OnPrimaryContainer
import edu.metrostate.ics342.mediatracker.theme.PrimaryContainer

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel(factory = RegisterViewModel.Factory),
) {
    val displayName     by viewModel.displayName.collectAsState()
    val username        by viewModel.username.collectAsState()
    val email           by viewModel.email.collectAsState()
    val password        by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val state           by viewModel.registerState.collectAsState()

    val focusManager      = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val notImplementedMsg = stringResource(R.string.signup_not_implemented)

    // success here just means validation passed, network is week-05 work.
    // show the snackbar, then reset so the user can try again.
    LaunchedEffect(state) {
        if (state is AuthViewModel.AuthUiState.Success) {
            snackbarHostState.showSnackbar(
                message  = notImplementedMsg,
                duration = SnackbarDuration.Short,
            )
            viewModel.resetRegisterState()
        }
    }

    val isLoading = state is AuthViewModel.AuthUiState.Loading
    val errorMsg  = (state as? AuthViewModel.AuthUiState.Error)?.msgResId?.let { stringResource(it) }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData   = data,
                    modifier       = Modifier.padding(12.dp),
                    shape          = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor   = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

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

            OutlinedTextField(
                value         = displayName,
                onValueChange = viewModel::setDisplayName,
                label         = { Text(stringResource(R.string.display_name_label)) },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value         = username,
                onValueChange = viewModel::setUsername,
                label         = { Text(stringResource(R.string.username_label)) },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value         = email,
                onValueChange = viewModel::setEmail,
                label         = { Text(stringResource(R.string.email_label)) },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value         = password,
                onValueChange = viewModel::setPassword,
                label         = { Text(stringResource(R.string.password_label)) },
                singleLine    = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value         = confirmPassword,
                onValueChange = viewModel::setConfirmPassword,
                label         = { Text(stringResource(R.string.confirm_password_label)) },
                singleLine    = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    viewModel.onSignUpClicked()
                }),
                modifier = Modifier.fillMaxWidth()
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
                onClick  = { focusManager.clearFocus(); viewModel.onSignUpClicked() },
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
}
