package com.example.chatbot.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalFocusManager
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import com.example.chatbot.resources.Res
import com.example.chatbot.resources.auth_error_email_invalid
import com.example.chatbot.resources.auth_error_email_not_confirmed
import com.example.chatbot.resources.auth_error_email_required
import com.example.chatbot.resources.auth_error_invalid_credentials
import com.example.chatbot.resources.auth_error_invalid_link
import com.example.chatbot.resources.auth_error_network
import com.example.chatbot.resources.auth_error_password_required
import com.example.chatbot.resources.auth_error_password_too_short
import com.example.chatbot.resources.auth_error_rate_limited
import com.example.chatbot.resources.auth_error_same_password
import com.example.chatbot.resources.auth_error_unknown
import com.example.chatbot.resources.auth_error_weak_password
import com.example.chatbot.resources.login_email
import com.example.chatbot.resources.login_hide_password
import com.example.chatbot.resources.login_show_password
import com.example.chatbot.domain.model.AuthError
import com.example.chatbot.domain.validation.CredentialsValidator
import com.example.chatbot.domain.validation.EmailError
import com.example.chatbot.domain.validation.PasswordError
import com.example.chatbot.ui.components.BrandLockup
import com.example.chatbot.ui.components.DmElevation
import com.example.chatbot.ui.components.DmIconButton
import com.example.chatbot.ui.components.DmPillField
import com.example.chatbot.ui.components.DmSurfaceCard
import com.example.chatbot.ui.components.SystemBarsProtection
import com.example.chatbot.ui.theme.Dimens

/**
 * Shared frame of the sign-in and sign-up screens: brand, subtitle, form card and footer.
 * contentWindowInsets includes the keyboard, so the scrollable form stays above it.
 */
@Composable
fun AuthFormLayout(
    subtitle: String,
    modifier: Modifier = Modifier,
    footer: @Composable () -> Unit = {},
    form: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.screenPadding, vertical = Dimens.space28),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                BrandLockup()
                Spacer(Modifier.height(Dimens.space8))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(Dimens.space28))
                DmSurfaceCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    elevation = DmElevation.High,
                    verticalArrangement = Arrangement.spacedBy(Dimens.space12),
                    content = form,
                )
                Spacer(Modifier.height(Dimens.space16))
                footer()
            }
            SystemBarsProtection()
        }
    }
}

@Composable
fun AuthEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    error: EmailError?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    DmPillField(
        value = value,
        onValueChange = onValueChange,
        placeholder = stringResource(Res.string.login_email),
        leadingIcon = rememberVectorPainter(Icons.Outlined.Mail),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        keyboardActions = moveDownOnNext(),
        enabled = enabled,
        errorText = error?.let { emailErrorText(it) },
        modifier = modifier.fillMaxWidth(),
    )
}

/** Password field with its own show/hide toggle. [onDone] runs from the keyboard when [imeAction] is Done. */
@Composable
fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    errorText: String?,
    enabled: Boolean,
    imeAction: ImeAction,
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {},
) {
    var isVisible by rememberSaveable { mutableStateOf(false) }
    DmPillField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        leadingIcon = rememberVectorPainter(Icons.Outlined.Lock),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = KeyboardActions(onNext = moveDownOnNext().onNext, onDone = { onDone() }),
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        contentPadding = PaddingValues(start = Dimens.space16),
        enabled = enabled,
        errorText = errorText,
        modifier = modifier.fillMaxWidth(),
        trailing = {
            DmIconButton(
                icon = rememberVectorPainter(if (isVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility),
                contentDescription = stringResource(if (isVisible) Res.string.login_hide_password else Res.string.login_show_password),
                onClick = { isVisible = !isVisible },
                iconSize = Dimens.iconXl,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}

/**
 * "Next" moves to the field below. The default would stop on the password show/hide button,
 * which sits to the right of the field.
 */
@Composable
private fun moveDownOnNext(): KeyboardActions {
    val focusManager = LocalFocusManager.current
    return KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
}

/** Request error shown above the main button; announced by screen readers when it appears. */
@Composable
fun AuthErrorMessage(error: AuthError?, modifier: Modifier = Modifier) {
    if (error == null) return
    Text(
        text = authErrorText(error),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
    )
}

@Composable
fun emailErrorText(error: EmailError): String = when (error) {
    EmailError.Required -> stringResource(Res.string.auth_error_email_required)
    EmailError.Invalid -> stringResource(Res.string.auth_error_email_invalid)
}

@Composable
fun passwordErrorText(error: PasswordError): String = when (error) {
    PasswordError.Required -> stringResource(Res.string.auth_error_password_required)
    PasswordError.TooShort -> CredentialsValidator.MIN_PASSWORD_LENGTH.let {
        pluralStringResource(Res.plurals.auth_error_password_too_short, it, it)
    }
}

@Composable
private fun authErrorText(error: AuthError): String = when (error) {
    AuthError.InvalidCredentials -> stringResource(Res.string.auth_error_invalid_credentials)
    AuthError.EmailNotConfirmed -> stringResource(Res.string.auth_error_email_not_confirmed)
    AuthError.WeakPassword -> stringResource(Res.string.auth_error_weak_password)
    AuthError.InvalidLink -> stringResource(Res.string.auth_error_invalid_link)
    AuthError.SamePassword -> stringResource(Res.string.auth_error_same_password)
    AuthError.RateLimited -> stringResource(Res.string.auth_error_rate_limited)
    AuthError.Network -> stringResource(Res.string.auth_error_network)
    AuthError.Unknown -> stringResource(Res.string.auth_error_unknown)
}
