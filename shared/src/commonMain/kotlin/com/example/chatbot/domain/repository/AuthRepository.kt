package com.example.chatbot.domain.repository

import com.example.chatbot.domain.model.AuthResult
import com.example.chatbot.domain.model.SessionState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val sessionState: Flow<SessionState>

    suspend fun signIn(email: String, password: String): AuthResult

    /** Creates the account and sends the confirmation email. It does not sign the user in. */
    suspend fun signUp(email: String, password: String): AuthResult

    /** Sends the reset link. Success does not tell whether the email has an account. */
    suspend fun sendPasswordReset(email: String): AuthResult

    /** Turns the code of a reset link into a temporary session. Fails with InvalidLink if it is not usable. */
    suspend fun exchangeRecoveryCode(code: String): AuthResult

    /** Sets a new password for the current (temporary) session. */
    suspend fun updatePassword(password: String): AuthResult

    /** Always removes the local session, even if the server cannot be reached. */
    suspend fun signOut()
}
