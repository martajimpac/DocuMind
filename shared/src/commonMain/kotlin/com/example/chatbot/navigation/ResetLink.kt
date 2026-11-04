package com.example.chatbot.navigation

import io.ktor.http.Url

private const val RESET_LINK_SCHEME = "documind"
private const val RESET_LINK_HOST = "reset-password"

/** A documind://reset-password link from the recovery email, with the code or the error Supabase added. */
data class ResetLink(val code: String?, val errorCode: String?) {
    companion object {
        /** Returns null when [link] is not a password reset link. */
        fun parse(link: String): ResetLink? {
            val url = try {
                Url(link)
            } catch (_: Exception) {
                return null
            }
            if (url.protocol.name != RESET_LINK_SCHEME || url.host != RESET_LINK_HOST) return null
            return ResetLink(
                code = url.parameters["code"],
                errorCode = url.parameters["error_code"],
            )
        }
    }
}
