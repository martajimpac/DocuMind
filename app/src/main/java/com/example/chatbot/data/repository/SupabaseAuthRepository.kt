package com.example.chatbot.data.repository

import com.example.chatbot.domain.model.AuthError
import com.example.chatbot.domain.model.AuthResult
import com.example.chatbot.domain.model.SessionState
import com.example.chatbot.domain.repository.AuthRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.HttpRequestException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

const val RESET_REDIRECT_URL = "documind://reset-password"

class SupabaseAuthRepository(private val auth: Auth) : AuthRepository {

    override val sessionState: Flow<SessionState> = auth.sessionStatus
        .map { status ->
            when (status) {
                SessionStatus.Initializing -> SessionState.Loading
                is SessionStatus.Authenticated -> SessionState.SignedIn
                // The saved session could not be refreshed (usually no network): keep the user signed in.
                is SessionStatus.RefreshFailure -> SessionState.SignedIn
                is SessionStatus.NotAuthenticated -> SessionState.SignedOut
            }
        }
        .distinctUntilChanged()

    override suspend fun signIn(email: String, password: String): AuthResult = runAuthCall {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signUp(email: String, password: String): AuthResult = runAuthCall {
        // No redirect: the confirmation email keeps going to the web, not to the app.
        auth.signUpWith(Email, redirectUrl = null) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun sendPasswordReset(email: String): AuthResult = runAuthCall {
        auth.resetPasswordForEmail(email = email, redirectUrl = RESET_REDIRECT_URL)
    }

    override suspend fun exchangeRecoveryCode(code: String): AuthResult = try {
        auth.exchangeCodeForSession(code)
        AuthResult.Success
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpRequestException) {
        AuthResult.Failure(AuthError.Network)
    } catch (e: Exception) {
        // Includes the IllegalArgumentException thrown when no code verifier is stored.
        AuthResult.Failure(AuthError.InvalidLink)
    }

    override suspend fun updatePassword(password: String): AuthResult = runAuthCall {
        auth.updateUser { this.password = password }
    }

    override suspend fun signOut() {
        try {
            auth.signOut()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // The server call failed (for example, offline). The local session is cleared below anyway.
        } finally {
            auth.clearSession()
        }
    }

    private suspend fun runAuthCall(call: suspend () -> Unit): AuthResult = try {
        call()
        AuthResult.Success
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        AuthResult.Failure(e.toAuthError())
    }
}

internal fun Throwable.toAuthError(): AuthError = when (this) {
    is AuthWeakPasswordException -> AuthError.WeakPassword
    is AuthRestException -> when (errorCode) {
        AuthErrorCode.InvalidCredentials -> AuthError.InvalidCredentials
        AuthErrorCode.EmailNotConfirmed -> AuthError.EmailNotConfirmed
        AuthErrorCode.WeakPassword -> AuthError.WeakPassword
        AuthErrorCode.SamePassword -> AuthError.SamePassword
        AuthErrorCode.OverEmailSendRateLimit -> AuthError.RateLimited
        AuthErrorCode.OtpExpired,
        AuthErrorCode.FlowStateNotFound,
        AuthErrorCode.FlowStateExpired,
        AuthErrorCode.BadCodeVerifier -> AuthError.InvalidLink
        else -> AuthError.Unknown
    }
    is HttpRequestException -> AuthError.Network
    else -> AuthError.Unknown
}
