package expert.vouch.sdk

import expert.vouch.sdk.utils.EmailValidator
import org.junit.Assert.*
import org.junit.Test

class EmailValidatorTest {
    @Test
    fun testValidEmails() {
        val validEmails = listOf(
            "user@example.com",
            "test.email@domain.co.uk",
            "name+tag@example.org",
            "user_name@example-domain.com",
            "123@456.com"
        )

        validEmails.forEach { email ->
            val result = EmailValidator.validate(email)
            assertNotNull("Email should be valid: $email", result)
            assertEquals("Email should be normalized", email.lowercase(), result)
        }
    }

    @Test
    fun testInvalidEmails() {
        val invalidEmails = listOf(
            "",
            "not-an-email",
            "@example.com",
            "user@",
            "user@.com",
            ".user@example.com",
            "user@example",
            "user @example.com",
            "user@exam ple.com",
            "a@b.c" // Too short
        )

        invalidEmails.forEach { email ->
            val result = EmailValidator.validate(email)
            assertNull("Email should be invalid: $email", result)
        }
    }

    @Test
    fun testEmailNormalization() {
        val testCases = mapOf(
            "  User@Example.COM  " to "user@example.com",
            "TEST@TEST.COM" to "test@test.com",
            "  whitespace@test.com" to "whitespace@test.com"
        )

        testCases.forEach { (input, expected) ->
            val result = EmailValidator.validate(input)
            assertEquals(expected, result)
        }
    }

    @Test
    fun testEmailLengthLimits() {
        // Too short (less than 5 characters)
        assertNull(EmailValidator.validate("a@b.c"))

        // Minimum valid length (5 characters)
        assertNotNull(EmailValidator.validate("a@b.co"))

        // Too long (more than 254 characters)
        // "@example.com" is 12 chars, so 243 + 12 = 255 (too long)
        val longEmail = "a".repeat(243) + "@example.com"
        assertNull(EmailValidator.validate(longEmail))

        // Maximum valid length (254 characters)
        // 242 + 12 = 254 (exactly at limit)
        val maxEmail = "a".repeat(242) + "@example.com"
        assertNotNull(EmailValidator.validate(maxEmail))
    }
}
