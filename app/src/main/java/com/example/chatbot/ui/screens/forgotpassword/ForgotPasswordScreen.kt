package com.example.chatbot.ui.screens.forgotpassword

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chatbot.R
import com.example.chatbot.ui.components.DmPrimaryButton
import com.example.chatbot.ui.components.DmTextButton
import com.example.chatbot.ui.screens.login.AuthEmailField
import com.example.chatbot.ui.screens.login.AuthErrorMessage
import com.example.chatbot.ui.screens.login.AuthFormLayout
import com.example.chatbot.ui.theme.ChatbotTheme
import com.example.chatbot.ui.utils.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Kept across rotation so the dialog does not disappear.
    var showSentDialog by rememberSaveable { mutableStateOf(false) }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ForgotPasswordEvent.LinkSent -> showSentDialog = true
        }
    }

    ForgotPasswordContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onSubmit = viewModel::onSubmit,
        onBackToLogin = onBackToLogin,
        modifier = modifier,
    )

    if (showSentDialog) {
        LinkSentDialog(
            onDismiss = {
                showSentDialog = false
                onBackToLogin()
            },
        )
    }
}

@Composable
fun ForgotPasswordContent(
    uiState: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = !uiState.isLoading
    AuthFormLayout(
        subtitle = stringResource(R.string.forgot_subtitle),
        modifier = modifier,
        footer = {
            DmTextButton(text = stringResource(R.string.forgot_back_to_login), onClick = { if (enabled) onBackToLogin() })
        },
    ) {
        AuthEmailField(
            value = uiState.email,
            onValueChange = onEmailChange,
            error = uiState.emailError,
            enabled = enabled,
        )
        AuthErrorMessage(uiState.error)
        DmPrimaryButton(
            text = stringResource(R.string.forgot_submit),
            onClick = onSubmit,
            loading = uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun LinkSentDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = stringResource(R.string.forgot_sent_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.forgot_sent_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            DmTextButton(text = stringResource(R.string.forgot_sent_ok), onClick = onDismiss)
        },
    )
}

@Preview
@Composable
private fun ForgotPasswordContentPreview() {
    ChatbotTheme {
        ForgotPasswordContent(ForgotPasswordUiState(), {}, {}, {})
    }
}
