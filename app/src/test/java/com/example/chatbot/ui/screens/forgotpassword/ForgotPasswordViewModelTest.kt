package com.example.chatbot.ui.screens.forgotpassword

import androidx.lifecycle.SavedStateHandle
import com.example.chatbot.domain.model.AuthError
import com.example.chatbot.domain.model.AuthResult
import com.example.chatbot.domain.validation.CredentialsValidator
import com.example.chatbot.domain.validation.EmailError
import com.example.chatbot.testing.FakeAuthRepository
import com.example.chatbot.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ForgotPasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAuthRepository()

    private fun viewModel(email: String? = null) = ForgotPasswordViewModel(
        SavedStateHandle(if (email == null) emptyMap() else mapOf("email" to email)),
        repository,
        CredentialsValidator(),
    )

    @Test
    fun `email is prefilled from the navigation argument (CA-14)`() {
        assertEquals("ana@mail.com", viewModel("ana@mail.com").uiState.value.email)
    }

    @Test
    fun `invalid email sends no request`() = runTest {
        val viewModel = viewModel()
        viewModel.onEmailChange("ana@mail")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(EmailError.Invalid, viewModel.uiState.value.emailError)
        assertTrue(repository.resetRequests.isEmpty())
    }

    @Test
    fun `valid email sends the trimmed address and reports the link as sent (CA-15)`() = runTest {
        val viewModel = viewModel()
        viewModel.onEmailChange(" ana@mail.com ")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(listOf("ana@mail.com"), repository.resetRequests)
        assertEquals(ForgotPasswordEvent.LinkSent, viewModel.events.first())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `request error is shown`() = runTest {
        repository.resetResult = AuthResult.Failure(AuthError.RateLimited)
        val viewModel = viewModel("ana@mail.com")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(AuthError.RateLimited, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `repeated taps while loading send a single request`() = runTest {
        repository.gate = CompletableDeferred()
        val viewModel = viewModel("ana@mail.com")
        viewModel.onSubmit()
        advanceUntilIdle()
        viewModel.onSubmit()
        repository.gate?.complete(Unit)
        advanceUntilIdle()

        assertEquals(1, repository.resetRequests.size)
    }
}
