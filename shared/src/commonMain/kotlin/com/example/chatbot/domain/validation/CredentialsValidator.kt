package com.example.chatbot.domain.validation

enum class EmailError { Required, Invalid }

enum class PasswordError { Required, TooShort }

/** Local checks done before any request is sent (SPEC RF-03). */
class CredentialsValidator {

    fun validateEmail(email: String): EmailError? {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> EmailError.Required
            !EMAIL_PATTERN.matches(trimmed) -> EmailError.Invalid
            else -> null
        }
    }

    fun validatePassword(password: String): PasswordError? = when {
        password.isEmpty() -> PasswordError.Required
        password.length < MIN_PASSWORD_LENGTH -> PasswordError.TooShort
        else -> null
    }

    fun passwordsMatch(password: String, confirmation: String): Boolean = password == confirmation

    companion object {
        const val MIN_PASSWORD_LENGTH = 8

        // Pragmatic format check: something@domain.tld with no spaces. The server has the final word.
        private val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    }
}
