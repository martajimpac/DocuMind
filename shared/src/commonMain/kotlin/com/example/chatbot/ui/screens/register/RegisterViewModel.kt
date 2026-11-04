package com.example.chatbot.ui.screens.register

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatbot.domain.model.AuthError
import com.example.chatbot.domain.model.AuthResult
import com.example.chatbot.domain.repository.AuthRepository
import com.example.chatbot.domain.validation.CredentialsValidator
import com.example.chatbot.domain.validation.EmailError
import com.example.chatbot.domain.validation.PasswordError
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val emailError: EmailError? = null,
    val passwordError: PasswordError? = null,
    val passwordsMismatch: Boolean = false,
    val error: AuthError? = null,
    val isLoading: Boolean = false,
)

sealed interface RegisterEvent {
    /** The account was created and the confirmation email was sent to [email]. */
    data class ConfirmationSent(val email: String) : RegisterEvent
}

class RegisterViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val validator: CredentialsValidator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState(email = savedStateHandle[KEY_EMAIL] ?: ""))
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _events = Channel<RegisterEvent>(Channel.BUFFERED)
    val events: Flow<RegisterEvent> = _events.receiveAsFlow()

    fun onEmailChange(email: String) {
        savedStateHandle[KEY_EMAIL] = email
        _uiState.update { it.copy(email = email, emailError = null, error = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, passwordsMismatch = false, error = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, passwordsMismatch = false, error = null) }
    }

    fun onSubmit() {
        val state = _uiState.value
        if (state.isLoading) return

        val emailError = validator.validateEmail(state.email)
        val passwordError = validator.validatePassword(state.password)
        val mismatch = !validator.passwordsMatch(state.password, state.confirmPassword)
        if (emailError != null || passwordError != null || mismatch) {
            _uiState.update {
                it.copy(emailError = emailError, passwordError = passwordError, passwordsMismatch = mismatch, error = null)
            }
            return
        }

        val email = state.email.trim()
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            // An email that already has an account also ends here as Success: Supabase does not reveal
            // which emails are registered when confirmation is required (SPEC, business rules).
            when (val result = authRepository.signUp(email, state.password)) {
                AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(RegisterEvent.ConfirmationSent(email))
                }
                is AuthResult.Failure -> _uiState.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    private companion object {
        const val KEY_EMAIL = "email"
    }
}
