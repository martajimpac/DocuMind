package com.example.chatbot.domain.model

/** Whether there is a usable session on this device. */
enum class SessionState {
    /** The saved session is still being read; the app should not decide a screen yet. */
    Loading,
    SignedIn,
    SignedOut,
}

sealed interface AuthResult {
    data object Success : AuthResult
    data class Failure(val error: AuthError) : AuthResult
}

enum class AuthError {
    InvalidCredentials,
    EmailNotConfirmed,
    WeakPassword,
    /** The reset link is expired, already used or was not requested from this device. */
    InvalidLink,
    SamePassword,
    RateLimited,
    Network,
    Unknown,
}
