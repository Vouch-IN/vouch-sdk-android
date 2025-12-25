package expert.vouch.sdk.utils

/**
 * Email validation utility matching iOS/JS SDK validation logic
 */
internal object EmailValidator {
    // Email regex pattern matching JS SDK:
    // ^(?!\.)[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$
    private val EMAIL_PATTERN = Regex(
        pattern = "^(?!\\.)[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
        options = setOf(RegexOption.IGNORE_CASE)
    )

    /**
     * Validate email format locally
     * Returns normalized email if valid, null if invalid
     */
    fun validate(email: String): String? {
        // Trim and lowercase
        val normalized = email.trim().lowercase()

        // Check length: 5-254 characters
        if (normalized.length < 5 || normalized.length > 254) {
            return null
        }

        // Check pattern
        if (!EMAIL_PATTERN.matches(normalized)) {
            return null
        }

        return normalized
    }
}
