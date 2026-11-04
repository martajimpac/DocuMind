package com.example.chatbot.ui.screens.register

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chatbot.R
import com.example.chatbot.ui.components.DmPrimaryButton
import com.example.chatbot.ui.components.DmTextButton
import com.example.chatbot.ui.screens.login.AuthEmailField
import com.example.chatbot.ui.screens.login.AuthErrorMessage
import com.example.chatbot.ui.screens.login.AuthFormLayout
import com.example.chatbot.ui.screens.login.AuthPasswordField
import com.example.chatbot.ui.screens.login.passwordErrorText
import com.example.chatbot.ui.theme.ChatbotTheme
import com.example.chatbot.ui.utils.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Kept across rotation so the confirmation dialog does not disappear.
    var sentToEmail by rememberSaveable { mutableStateOf<String?>(null) }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is RegisterEvent.ConfirmationSent -> sentToEmail = event.email
        }
    }

    RegisterContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onSubmit = viewModel::onSubmit,
        onBackToLogin = onBackToLogin,
        modifier = modifier,
    )

    sentToEmail?.let { email ->
        ConfirmationSentDialog(
            email = email,
            onDismiss = {
                sentToEmail = null
                onBackToLogin()
            },
        )
    }
}

@Composable
fun RegisterContent(
    uiState: RegisterUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = !uiState.isLoading
    AuthFormLayout(
        subtitle = stringResource(R.string.register_subtitle),
        modifier = modifier,
        footer = {
            DmTextButton(text = stringResource(R.string.register_have_account), onClick = { if (enabled) onBackToLogin() })
        },
    ) {
        AuthEmailField(
            value = uiState.email,
            onValueChange = onEmailChange,
            error = uiState.emailError,
            enabled = enabled,
        )
        AuthPasswordField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            placeholder = stringResource(R.string.login_password),
            errorText = uiState.passwordError?.let { passwordErrorText(it) },
            enabled = enabled,
            imeAction = ImeAction.Next,
        )
        AuthPasswordField(
            value = uiState.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            placeholder = stringResource(R.string.register_confirm_password),
            errorText = if (uiState.passwordsMismatch) stringResource(R.string.auth_error_passwords_mismatch) else null,
            enabled = enabled,
            imeAction = ImeAction.Done,
            onDone = onSubmit,
        )
        AuthErrorMessage(uiState.error)
        DmPrimaryButton(
            text = stringResource(R.string.register_submit),
            onClick = onSubmit,
            loading = uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ConfirmationSentDialog(email: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = stringResource(R.string.register_email_sent_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.register_email_sent_body, email),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            DmTextButton(text = stringResource(R.string.register_email_sent_ok), onClick = onDismiss)
        },
    )
}

@Preview
@Composable
private fun RegisterContentPreview() {
    ChatbotTheme {
        RegisterContent(RegisterUiState(), {}, {}, {}, {}, {})
    }
}
