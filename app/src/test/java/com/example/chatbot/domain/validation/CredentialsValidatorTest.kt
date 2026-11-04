package com.example.chatbot.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** RF-03 / CA-04. */
class CredentialsValidatorTest {

    private val validator = CredentialsValidator()

    @Test
    fun `empty email is required`() {
        assertEquals(EmailError.Required, validator.validateEmail("   "))
    }

    @Test
    fun `email without domain or with spaces is invalid`() {
        listOf("ana", "ana@", "ana@mail", "ana @mail.com", "@mail.com").forEach {
            assertEquals("for '$it'", EmailError.Invalid, validator.validateEmail(it))
        }
    }

    @Test
    fun `valid email passes, ignoring surrounding spaces`() {
        assertNull(validator.validateEmail("  ana.lopez+docs@mail.example.com "))
    }

    @Test
    fun `password needs at least eight characters`() {
        assertEquals(PasswordError.Required, validator.validatePassword(""))
        assertEquals(PasswordError.TooShort, validator.validatePassword("1234567"))
        assertNull(validator.validatePassword("12345678"))
    }

    @Test
    fun `passwords must match exactly`() {
        assertTrue(validator.passwordsMatch("secret123", "secret123"))
        assertFalse(validator.passwordsMatch("secret123", "Secret123"))
    }
}
