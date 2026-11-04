package com.example.chatbot.navigation

import com.example.chatbot.domain.model.SessionState
import com.example.chatbot.testing.FakeAuthRepository
import com.example.chatbot.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class SessionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `no start screen while the saved session is loading`() = runTest {
        val viewModel = SessionViewModel(FakeAuthRepository(SessionState.Loading))
        advanceUntilIdle()
        assertNull(viewModel.startDestination.value)
    }

    @Test
    fun `saved session opens My Notebooks (CA-07)`() = runTest {
        val repository = FakeAuthRepository(SessionState.Loading)
        val viewModel = SessionViewModel(repository)
        repository.sessionState.value = SessionState.SignedIn
        advanceUntilIdle()
        assertEquals(StartDestination.Notebooks, viewModel.startDestination.value)
    }

    @Test
    fun `no session opens the login`() = runTest {
        val viewModel = SessionViewModel(FakeAuthRepository(SessionState.SignedOut))
        advanceUntilIdle()
        assertEquals(StartDestination.Login, viewModel.startDestination.value)
    }

    @Test
    fun `signing out emits SignedOut and keeps the first start screen (CA-08)`() = runTest {
        val repository = FakeAuthRepository(SessionState.SignedIn)
        val viewModel = SessionViewModel(repository)
        advanceUntilIdle()

        viewModel.signOut()
        advanceUntilIdle()

        assertEquals(1, repository.signOutCalls)
        assertEquals(SessionEvent.SignedOut, viewModel.events.first())
        assertEquals(StartDestination.Notebooks, viewModel.startDestination.value)
    }
}
