package com.example.chatbot.ui.screens.login

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.input.ImeAction
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chatbot.resources.Res
import com.example.chatbot.resources.login_forgot_password
import com.example.chatbot.resources.login_password
import com.example.chatbot.resources.login_register
import com.example.chatbot.resources.login_submit
import com.example.chatbot.resources.login_tagline
import com.example.chatbot.ui.components.DmPrimaryButton
import com.example.chatbot.ui.components.DmTextButton
import com.example.chatbot.ui.theme.ChatbotTheme
import com.example.chatbot.ui.utils.ObserveAsEvents
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    onSignedIn: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: (email: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            LoginEvent.SignedIn -> onSignedIn()
        }
    }
    LoginContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::onSubmit,
        onRegisterClick = onRegisterClick,
        onForgotPasswordClick = { onForgotPasswordClick(uiState.email) },
        modifier = modifier,
    )
}

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = !uiState.isLoading
    AuthFormLayout(
        subtitle = stringResource(Res.string.login_tagline),
        modifier = modifier,
        footer = {
            DmTextButton(text = stringResource(Res.string.login_register), onClick = { if (enabled) onRegisterClick() })
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
            placeholder = stringResource(Res.string.login_password),
            errorText = uiState.passwordError?.let { passwordErrorText(it) },
            enabled = enabled,
            imeAction = ImeAction.Done,
            onDone = onSubmit,
        )
        DmTextButton(
            text = stringResource(Res.string.login_forgot_password),
            onClick = { if (enabled) onForgotPasswordClick() },
            modifier = Modifier.align(Alignment.End),
        )
        AuthErrorMessage(uiState.error)
        DmPrimaryButton(
            text = stringResource(Res.string.login_submit),
            onClick = onSubmit,
            loading = uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun LoginContentPreview() {
    ChatbotTheme {
        LoginContent(LoginUiState(), {}, {}, {}, {}, {})
    }
}
