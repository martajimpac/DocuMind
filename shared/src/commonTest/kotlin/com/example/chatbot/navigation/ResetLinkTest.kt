package com.example.chatbot.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ResetLinkTest {

    @Test
    fun `reads the code from a recovery link`() {
        assertEquals(ResetLink(code = "abc-123", errorCode = null), ResetLink.parse("documind://reset-password?code=abc-123"))
    }

    @Test
    fun `reads the error Supabase adds to an expired link`() {
        val link = "documind://reset-password?error=access_denied&error_code=otp_expired&error_description=Email+link+is+invalid"
        assertEquals(ResetLink(code = null, errorCode = "otp_expired"), ResetLink.parse(link))
    }

    @Test
    fun `decodes encoded values`() {
        assertEquals("a b/c", ResetLink.parse("documind://reset-password?code=a%20b%2Fc")?.code)
    }

    @Test
    fun `link without parameters is still a reset link`() {
        assertEquals(ResetLink(code = null, errorCode = null), ResetLink.parse("documind://reset-password"))
    }

    @Test
    fun `other links are ignored`() {
        assertNull(ResetLink.parse("documind://other?code=abc"))
        assertNull(ResetLink.parse("https://reset-password?code=abc"))
        assertNull(ResetLink.parse("not a link"))
    }
}
