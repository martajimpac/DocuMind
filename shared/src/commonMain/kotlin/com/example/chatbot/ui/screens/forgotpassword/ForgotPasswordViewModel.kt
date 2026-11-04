package com.example.chatbot.ui.screens.forgotpassword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatbot.domain.model.AuthError
import com.example.chatbot.domain.model.AuthResult
import com.example.chatbot.domain.repository.AuthRepository
import com.example.chatbot.domain.validation.CredentialsValidator
import com.example.chatbot.domain.validation.EmailError
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: EmailError? = null,
    val error: AuthError? = null,
    val isLoading: Boolean = false,
)

sealed interface ForgotPasswordEvent {
    /** The request was accepted. The text shown must not reveal whether the email has an account. */
    data object LinkSent : ForgotPasswordEvent
}

class ForgotPasswordViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val validator: CredentialsValidator,
) : ViewModel() {

    // The navigation argument seeds the handle, so a restored process keeps what the user typed.
    private val _uiState = MutableStateFlow(ForgotPasswordUiState(email = savedStateHandle[KEY_EMAIL] ?: ""))
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _events = Channel<ForgotPasswordEvent>(Channel.BUFFERED)
    val events: Flow<ForgotPasswordEvent> = _events.receiveAsFlow()

    fun onEmailChange(email: String) {
        savedStateHandle[KEY_EMAIL] = email
        _uiState.update { it.copy(email = email, emailError = null, error = null) }
    }

    fun onSubmit() {
        val state = _uiState.value
        if (state.isLoading) return

        val emailError = validator.validateEmail(state.email)
        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError, error = null) }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = authRepository.sendPasswordReset(state.email.trim())) {
                AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(ForgotPasswordEvent.LinkSent)
                }
                is AuthResult.Failure -> _uiState.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    private companion object {
        const val KEY_EMAIL = "email"
    }
}
