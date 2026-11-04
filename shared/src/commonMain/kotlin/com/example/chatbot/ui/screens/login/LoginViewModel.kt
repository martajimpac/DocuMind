package com.example.chatbot.ui.screens.login

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

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: EmailError? = null,
    val passwordError: PasswordError? = null,
    val error: AuthError? = null,
    val isLoading: Boolean = false,
)

sealed interface LoginEvent {
    data object SignedIn : LoginEvent
}

class LoginViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val validator: CredentialsValidator,
) : ViewModel() {

    // Only the email survives process death; the password is never written to saved state.
    private val _uiState = MutableStateFlow(LoginUiState(email = savedStateHandle[KEY_EMAIL] ?: ""))
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events: Flow<LoginEvent> = _events.receiveAsFlow()

    fun onEmailChange(email: String) {
        savedStateHandle[KEY_EMAIL] = email
        _uiState.update { it.copy(email = email, emailError = null, error = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, error = null) }
    }

    fun onSubmit() {
        val state = _uiState.value
        if (state.isLoading) return

        val emailError = validator.validateEmail(state.email)
        val passwordError = validator.validatePassword(state.password)
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError, error = null) }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = authRepository.signIn(state.email.trim(), state.password)) {
                AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(LoginEvent.SignedIn)
                }
                is AuthResult.Failure -> _uiState.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    private companion object {
        const val KEY_EMAIL = "email"
    }
}
