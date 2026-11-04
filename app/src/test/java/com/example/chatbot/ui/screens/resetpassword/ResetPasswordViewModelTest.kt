package com.example.chatbot.ui.screens.resetpassword

import androidx.lifecycle.SavedStateHandle
import com.example.chatbot.domain.model.AuthError
import com.example.chatbot.domain.model.AuthResult
import com.example.chatbot.domain.validation.CredentialsValidator
import com.example.chatbot.domain.validation.PasswordError
import com.example.chatbot.testing.FakeAuthRepository
import com.example.chatbot.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ResetPasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAuthRepository()

    private fun viewModel(code: String? = "abc", errorCode: String? = null): ResetPasswordViewModel {
        val args = mapOf("code" to code, "error_code" to errorCode)
        return ResetPasswordViewModel(SavedStateHandle(args), repository, CredentialsValidator())
    }

    private fun ResetPasswordViewModel.fill(password: String = "password1", confirm: String = password) {
        onPasswordChange(password)
        onConfirmPasswordChange(confirm)
    }

    @Test
    fun `error_code in the link shows the invalid link without exchanging (CA-18)`() = runTest {
        val viewModel = viewModel(code = null, errorCode = "otp_expired")
        advanceUntilIdle()

        assertEquals(ResetStage.InvalidLink, viewModel.uiState.value.stage)
        assertTrue(repository.exchangeCalls.isEmpty())
    }

    @Test
    fun `link without a code is invalid`() = runTest {
        val viewModel = viewModel(code = null)
        advanceUntilIdle()

        assertEquals(ResetStage.InvalidLink, viewModel.uiState.value.stage)
        assertTrue(repository.exchangeCalls.isEmpty())
    }

    @Test
    fun `failed exchange shows the invalid link (CA-18)`() = runTest {
        repository.exchangeResult = AuthResult.Failure(AuthError.InvalidLink)
        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals(ResetStage.InvalidLink, viewModel.uiState.value.stage)
    }

    @Test
    fun `starts verifying and shows the form once the code is exchanged (CA-16)`() = runTest {
        val viewModel = viewModel()
        assertEquals(ResetStage.Verifying, viewModel.uiState.value.stage)
        advanceUntilIdle()

        assertEquals(ResetStage.Ready, viewModel.uiState.value.stage)
        assertEquals(listOf("abc"), repository.exchangeCalls)
    }

    @Test
    fun `short password or mismatch send no update`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.fill(password = "1234567")
        viewModel.onSubmit()
        assertEquals(PasswordError.TooShort, viewModel.uiState.value.passwordError)

        viewModel.fill(confirm = "password2")
        viewModel.onSubmit()
        assertTrue(viewModel.uiState.value.passwordsMismatch)

        advanceUntilIdle()
        assertTrue(repository.updatePasswordCalls.isEmpty())
    }

    @Test
    fun `valid password is saved and the user moves on (CA-17)`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.fill()
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(listOf("password1"), repository.updatePasswordCalls)
        assertEquals(ResetPasswordEvent.PasswordUpdated, viewModel.events.first())
    }

    @Test
    fun `same password error is shown`() = runTest {
        repository.updateResult = AuthResult.Failure(AuthError.SamePassword)
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.fill()
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(AuthError.SamePassword, viewModel.uiState.value.error)
        assertEquals(ResetStage.Ready, viewModel.uiState.value.stage)
    }

    @Test
    fun `cancel closes the temporary session and reports it (CA-19)`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.cancel()
        advanceUntilIdle()

        assertEquals(1, repository.signOutCalls)
        assertEquals(ResetPasswordEvent.Cancelled, viewModel.events.first())
    }

    @Test
    fun `the code is exchanged only once`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.fill()
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(1, repository.exchangeCalls.size)
    }

    @Test
    fun `no connection while checking the link offers a retry, not an invalid link`() = runTest {
        repository.exchangeResult = AuthResult.Failure(AuthError.Network)
        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(ResetStage.Offline, viewModel.uiState.value.stage)

        repository.exchangeResult = AuthResult.Success
        viewModel.onRetryVerification()
        advanceUntilIdle()

        assertEquals(ResetStage.Ready, viewModel.uiState.value.stage)
        assertEquals(listOf("abc", "abc"), repository.exchangeCalls)
    }

    @Test
    fun `retry does nothing once the link was checked`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onRetryVerification()
        advanceUntilIdle()

        assertEquals(1, repository.exchangeCalls.size)
    }

    @Test
    fun `cancelling while the link is being checked stops the check before signing out (CA-19)`() = runTest {
        repository.gate = CompletableDeferred()
        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(ResetStage.Verifying, viewModel.uiState.value.stage)

        viewModel.cancel()
        advanceUntilIdle()
        repository.gate?.complete(Unit)
        advanceUntilIdle()

        assertEquals(1, repository.signOutCalls)
        assertEquals(ResetPasswordEvent.Cancelled, viewModel.events.first())
        assertEquals(ResetStage.Verifying, viewModel.uiState.value.stage)
    }
}
