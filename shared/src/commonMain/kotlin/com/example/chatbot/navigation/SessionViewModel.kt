package com.example.chatbot.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatbot.domain.model.SessionState
import com.example.chatbot.domain.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

enum class StartDestination { Login, Notebooks }

sealed interface SessionEvent {
    /** The session ended after being active: the user signed out or the session was revoked. */
    data object SignedOut : SessionEvent
}

class SessionViewModel(private val authRepository: AuthRepository) : ViewModel() {

    // Decided once, from the first known session state, so the start screen never flips.
    private val _startDestination = MutableStateFlow<StartDestination?>(null)
    val startDestination: StateFlow<StartDestination?> = _startDestination.asStateFlow()

    private val _events = Channel<SessionEvent>(Channel.BUFFERED)
    val events: Flow<SessionEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            var previous: SessionState? = null
            authRepository.sessionState.collect { state ->
                if (state == SessionState.Loading) return@collect
                if (_startDestination.value == null) {
                    _startDestination.value =
                        if (state == SessionState.SignedIn) StartDestination.Notebooks else StartDestination.Login
                }
                if (previous == SessionState.SignedIn && state == SessionState.SignedOut) {
                    _events.send(SessionEvent.SignedOut)
                }
                previous = state
            }
        }
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}
