package com.example.chatbot.ui.screens.resetpassword

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chatbot.R
import com.example.chatbot.domain.model.AuthError
import com.example.chatbot.ui.components.DmPrimaryButton
import com.example.chatbot.ui.screens.login.AuthErrorMessage
import com.example.chatbot.ui.screens.login.AuthFormLayout
import com.example.chatbot.ui.screens.login.AuthPasswordField
import com.example.chatbot.ui.screens.login.passwordErrorText
import com.example.chatbot.ui.theme.ChatbotTheme
import com.example.chatbot.ui.utils.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun ResetPasswordScreen(
    onPasswordUpdated: () -> Unit,
    onCancelled: () -> Unit,
    onRequestNewLink: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResetPasswordViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ResetPasswordEvent.PasswordUpdated -> onPasswordUpdated()
            ResetPasswordEvent.Cancelled -> onCancelled()
        }
    }

    // Leaving with a live temporary session would sign the user in without a password change.
    BackHandler {
        if (uiState.stage == ResetStage.InvalidLink) onCancelled() else viewModel.cancel()
    }

    ResetPasswordContent(
        uiState = uiState,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onSubmit = viewModel::onSubmit,
        onRequestNewLink = onRequestNewLink,
        onRetry = viewModel::onRetryVerification,
        modifier = modifier,
    )
}

@Composable
fun ResetPasswordContent(
    uiState: ResetPasswordUiState,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onRequestNewLink: () -> Unit,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
) {
    val enabled = !uiState.isLoading
    AuthFormLayout(
        subtitle = stringResource(R.string.reset_subtitle),
        modifier = modifier,
    ) {
        when (uiState.stage) {
            ResetStage.Verifying -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                Text(
                    text = stringResource(R.string.reset_verifying),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            ResetStage.Ready -> {
                AuthPasswordField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    placeholder = stringResource(R.string.reset_new_password),
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
                    text = stringResource(R.string.reset_submit),
                    onClick = onSubmit,
                    loading = uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            ResetStage.Offline -> {
                AuthErrorMessage(AuthError.Network)
                DmPrimaryButton(
                    text = stringResource(R.string.reset_retry),
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            ResetStage.InvalidLink -> {
                Text(
                    text = stringResource(R.string.reset_invalid_link),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                DmPrimaryButton(
                    text = stringResource(R.string.reset_request_new_link),
                    onClick = onRequestNewLink,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview
@Composable
private fun ResetPasswordReadyPreview() {
    ChatbotTheme {
        ResetPasswordContent(ResetPasswordUiState(stage = ResetStage.Ready), {}, {}, {}, {})
    }
}

@Preview
@Composable
private fun ResetPasswordInvalidPreview() {
    ChatbotTheme {
        ResetPasswordContent(ResetPasswordUiState(stage = ResetStage.InvalidLink), {}, {}, {}, {})
    }
}
