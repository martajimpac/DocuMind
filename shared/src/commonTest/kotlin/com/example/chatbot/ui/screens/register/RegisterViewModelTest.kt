package com.example.chatbot.ui.screens.register

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
import kotlin.test.assertTrue
import kotlin.test.Test

class RegisterViewModelTest : MainDispatcherTest() {
    private val repository = FakeAuthRepository()
    private val viewModel = RegisterViewModel(SavedStateHandle(), repository, CredentialsValidator())

    private fun fill(email: String = "ana@mail.com", password: String = "password1", confirm: String = password) {
        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)
        viewModel.onConfirmPasswordChange(confirm)
    }

    @Test
    fun `invalid email, short password or mismatch send no request (CA-04)`() = runTest {
        fill(email = "ana@mail")
        viewModel.onSubmit()
        assertEquals(EmailError.Invalid, viewModel.uiState.value.emailError)

        fill(password = "1234567")
        viewModel.onSubmit()
        assertEquals(PasswordError.TooShort, viewModel.uiState.value.passwordError)

        fill(confirm = "password2")
        viewModel.onSubmit()
        assertTrue(viewModel.uiState.value.passwordsMismatch)

        advanceUntilIdle()
        assertTrue(repository.signUpCalls.isEmpty())
    }

    @Test
    fun `valid data creates the account and asks to check the email (CA-02)`() = runTest {
        fill(email = " ana@mail.com")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(listOf("ana@mail.com" to "password1"), repository.signUpCalls)
        assertEquals(RegisterEvent.ConfirmationSent("ana@mail.com"), viewModel.events.first())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `server rejection is shown and the form stays filled`() = runTest {
        repository.nextResult = AuthResult.Failure(AuthError.WeakPassword)
        fill()
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(AuthError.WeakPassword, viewModel.uiState.value.error)
        assertEquals("password1", viewModel.uiState.value.confirmPassword)
    }

    @Test
    fun `repeated taps while loading send a single request (CA-09)`() = runTest {
        repository.gate = CompletableDeferred()
        fill()
        viewModel.onSubmit()
        advanceUntilIdle()
        viewModel.onSubmit()
        repository.gate?.complete(Unit)
        advanceUntilIdle()

        assertEquals(1, repository.signUpCalls.size)
    }
}
