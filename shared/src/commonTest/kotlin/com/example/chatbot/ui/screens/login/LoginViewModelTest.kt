package com.example.chatbot.ui.screens.login

import androidx.lifecycle.SavedStateHandle
import com.example.chatbot.domain.model.AuthError
import com.example.chatbot.domain.model.AuthResult
import com.example.chatbot.domain.validation.CredentialsValidator
import com.example.chatbot.domain.validation.EmailError
import com.example.chatbot.domain.validation.PasswordError
import com.example.chatbot.testing.FakeAuthRepository
import com.example.chatbot.testing.MainDispatcherTest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.Test

class LoginViewModelTest : MainDispatcherTest() {
    private val repository = FakeAuthRepository()
    private val savedStateHandle = SavedStateHandle()
    private val viewModel = LoginViewModel(savedStateHandle, repository, CredentialsValidator())

    private fun fillValid() {
        viewModel.onEmailChange(" ana@mail.com ")
        viewModel.onPasswordChange("password1")
    }

    @Test
    fun `invalid input shows field errors and sends no request`() = runTest {
        viewModel.onEmailChange("ana@")
        viewModel.onPasswordChange("short")
        viewModel.onSubmit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(EmailError.Invalid, state.emailError)
        assertEquals(PasswordError.TooShort, state.passwordError)
        assertTrue(repository.signInCalls.isEmpty())
    }

    @Test
    fun `editing a field clears its error`() = runTest {
        viewModel.onSubmit()
        viewModel.onEmailChange("a")
        assertNull(viewModel.uiState.value.emailError)
        assertEquals(PasswordError.Required, viewModel.uiState.value.passwordError)
    }

    @Test
    fun `successful sign in trims email and emits SignedIn (CA-05)`() = runTest {
        fillValid()
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(listOf("ana@mail.com" to "password1"), repository.signInCalls)
        assertEquals(LoginEvent.SignedIn, viewModel.events.first())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `wrong credentials keep the email and show the error (CA-06)`() = runTest {
        repository.nextResult = AuthResult.Failure(AuthError.InvalidCredentials)
        fillValid()
        viewModel.onSubmit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AuthError.InvalidCredentials, state.error)
        assertEquals(" ana@mail.com ", state.email)
        assertFalse(state.isLoading)
    }

    @Test
    fun `unconfirmed email shows its own error (CA-03)`() = runTest {
        repository.nextResult = AuthResult.Failure(AuthError.EmailNotConfirmed)
        fillValid()
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(AuthError.EmailNotConfirmed, viewModel.uiState.value.error)
    }

    @Test
    fun `repeated taps while loading send a single request (CA-09)`() = runTest {
        repository.gate = CompletableDeferred()
        fillValid()
        viewModel.onSubmit()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isLoading)

        viewModel.onSubmit()
        viewModel.onSubmit()
        repository.gate?.complete(Unit)
        advanceUntilIdle()

        assertEquals(1, repository.signInCalls.size)
    }

    @Test
    fun `network error keeps the data and a retry can succeed (CA-11)`() = runTest {
        repository.nextResult = AuthResult.Failure(AuthError.Network)
        fillValid()
        viewModel.onSubmit()
        advanceUntilIdle()
        assertEquals(AuthError.Network, viewModel.uiState.value.error)
        assertEquals("password1", viewModel.uiState.value.password)

        repository.nextResult = AuthResult.Success
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(LoginEvent.SignedIn, viewModel.events.first())
        assertEquals(2, repository.signInCalls.size)
    }

    @Test
    fun `email survives process death but the password does not (CA-12)`() = runTest {
        fillValid()
        val restored = LoginViewModel(savedStateHandle, repository, CredentialsValidator())

        assertEquals(" ana@mail.com ", restored.uiState.value.email)
        assertEquals("", restored.uiState.value.password)
    }
}
