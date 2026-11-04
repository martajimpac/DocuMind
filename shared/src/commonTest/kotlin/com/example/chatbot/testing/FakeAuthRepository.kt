package com.example.chatbot.testing

import com.example.chatbot.domain.model.AuthResult
import com.example.chatbot.domain.model.SessionState
import com.example.chatbot.domain.repository.AuthRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory AuthRepository. Set [gate] to hold calls open and check the loading state. */
class FakeAuthRepository(
    initialSession: SessionState = SessionState.SignedOut,
) : AuthRepository {

    override val sessionState = MutableStateFlow(initialSession)

    var nextResult: AuthResult = AuthResult.Success
    var gate: CompletableDeferred<Unit>? = null

    val signInCalls = mutableListOf<Pair<String, String>>()
    val signUpCalls = mutableListOf<Pair<String, String>>()
    val resetRequests = mutableListOf<String>()
    val exchangeCalls = mutableListOf<String>()
    val updatePasswordCalls = mutableListOf<String>()
    var resetResult: AuthResult = AuthResult.Success
    var exchangeResult: AuthResult = AuthResult.Success
    var updateResult: AuthResult = AuthResult.Success
    var signOutCalls = 0

    override suspend fun signIn(email: String, password: String): AuthResult {
        signInCalls += email to password
        gate?.await()
        return nextResult
    }

    override suspend fun signUp(email: String, password: String): AuthResult {
        signUpCalls += email to password
        gate?.await()
        return nextResult
    }

    override suspend fun sendPasswordReset(email: String): AuthResult {
        resetRequests += email
        gate?.await()
        return resetResult
    }

    override suspend fun exchangeRecoveryCode(code: String): AuthResult {
        exchangeCalls += code
        gate?.await()
        return exchangeResult
    }

    override suspend fun updatePassword(password: String): AuthResult {
        updatePasswordCalls += password
        gate?.await()
        return updateResult
    }

    override suspend fun signOut() {
        signOutCalls++
        sessionState.value = SessionState.SignedOut
    }
}
