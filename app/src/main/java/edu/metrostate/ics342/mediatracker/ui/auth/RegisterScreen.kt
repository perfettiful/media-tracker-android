package edu.metrostate.ics342.mediatracker.ui.auth

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

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val displayName     by viewModel.displayName.collectAsState()
    val username        by viewModel.username.collectAsState()
    val email           by viewModel.registerEmail.collectAsState()
    val password        by viewModel.registerPassword.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val state           by viewModel.registerState.collectAsState()

    val focusManager = LocalFocusManager.current

    LaunchedEffect(state) {
        if (state is AuthViewModel.AuthUiState.Success) {
            viewModel.resetRegisterState()
            onRegisterSuccess()
        }
    }

    val isLoading = state is AuthViewModel.AuthUiState.Loading
    val errorMsg  = (state as? AuthViewModel.AuthUiState.Error)?.msgResId?.let { stringResource(it) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // smaller header than login (60dp vs 72dp per wireframe)
        Surface(
            shape    = RoundedCornerShape(16.dp),
            color    = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(60.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_smart_display),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text  = stringResource(R.string.create_account_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
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
            onValueChange = viewModel::onDisplayNameChange,
            label         = { Text(stringResource(R.string.display_name_label)) },
            singleLine    = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction    = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value         = username,
            onValueChange = viewModel::onUsernameChange,
            label         = { Text(stringResource(R.string.username_label)) },
            singleLine    = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction    = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value         = email,
            onValueChange = viewModel::onRegisterEmailChange,
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
            onValueChange = viewModel::onRegisterPasswordChange,
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
            onValueChange = viewModel::onConfirmPasswordChange,
            label         = { Text(stringResource(R.string.confirm_password_label)) },
            singleLine    = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction    = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                viewModel.onRegisterClick()
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
            onClick  = { focusManager.clearFocus(); viewModel.onRegisterClick() },
            enabled  = !isLoading,
            modifier = Modifier.fillMaxWidth().height(48.dp)
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
