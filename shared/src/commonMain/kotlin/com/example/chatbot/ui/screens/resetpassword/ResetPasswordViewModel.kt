package com.example.chatbot.ui.screens.resetpassword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatbot.domain.model.AuthError
import com.example.chatbot.domain.model.AuthResult
import com.example.chatbot.domain.repository.AuthRepository
import com.example.chatbot.domain.validation.CredentialsValidator
import com.example.chatbot.domain.validation.PasswordError
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ResetStage {
    /** The link code is being checked with the server. */
    Verifying,
    Ready,
    InvalidLink,
    /** The link could not be checked because there is no connection; it may still be valid. */
    Offline,
}

data class ResetPasswordUiState(
    val stage: ResetStage = ResetStage.Verifying,
    val password: String = "",
    val confirmPassword: String = "",
    val passwordError: PasswordError? = null,
    val passwordsMismatch: Boolean = false,
    val error: AuthError? = null,
    val isLoading: Boolean = false,
)

sealed interface ResetPasswordEvent {
    data object PasswordUpdated : ResetPasswordEvent

    /** The user left without choosing a password; the temporary session is already closed. */
    data object Cancelled : ResetPasswordEvent
}

class ResetPasswordViewModel(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val validator: CredentialsValidator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    private val _events = Channel<ResetPasswordEvent>(Channel.BUFFERED)
    val events: Flow<ResetPasswordEvent> = _events.receiveAsFlow()

    private val code: String? = savedStateHandle.get<String>(KEY_CODE)
    private var verification: Job? = null

    init {
        val errorCode = savedStateHandle.get<String>(KEY_ERROR_CODE)
        if (errorCode != null || code == null) {
            _uiState.update { it.copy(stage = ResetStage.InvalidLink) }
        } else {
            verify(code)
        }
    }

    /** Retries the check after a connection failure. A used code is never sent twice. */
    fun onRetryVerification() {
        val code = code ?: return
        if (_uiState.value.stage != ResetStage.Offline) return
        verify(code)
    }

    private fun verify(code: String) {
        _uiState.update { it.copy(stage = ResetStage.Verifying) }
        verification = viewModelScope.launch {
            val stage = when (val result = authRepository.exchangeRecoveryCode(code)) {
                AuthResult.Success -> ResetStage.Ready
                is AuthResult.Failure ->
                    if (result.error == AuthError.Network) ResetStage.Offline else ResetStage.InvalidLink
            }
            _uiState.update { it.copy(stage = stage) }
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, passwordsMismatch = false, error = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, passwordsMismatch = false, error = null) }
    }

    fun onSubmit() {
        val state = _uiState.value
        if (state.isLoading || state.stage != ResetStage.Ready) return

        val passwordError = validator.validatePassword(state.password)
        val mismatch = !validator.passwordsMatch(state.password, state.confirmPassword)
        if (passwordError != null || mismatch) {
            _uiState.update { it.copy(passwordError = passwordError, passwordsMismatch = mismatch, error = null) }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = authRepository.updatePassword(state.password)) {
                AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(ResetPasswordEvent.PasswordUpdated)
                }
                is AuthResult.Failure -> _uiState.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    /** Leaves without a new password: the temporary session from the link must not stay open. */
    fun cancel() {
        viewModelScope.launch {
            // Stop a check still in flight, so it cannot open a session after we sign out.
            verification?.cancelAndJoin()
            authRepository.signOut()
            _events.send(ResetPasswordEvent.Cancelled)
        }
    }

    private companion object {
        const val KEY_CODE = "code"
        const val KEY_ERROR_CODE = "error_code"
    }
}
