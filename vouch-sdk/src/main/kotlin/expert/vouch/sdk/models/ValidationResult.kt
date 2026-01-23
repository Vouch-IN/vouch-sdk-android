package expert.vouch.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

// -- ValidationAction --

/**
 * Validation action enum
 */
@Serializable
enum class ValidationAction {
    @SerialName("allow") ALLOW,
    @SerialName("block") BLOCK,
    @SerialName("flag") FLAG
}

// -- ValidationResult --

/**
 * Email validation response from API
 */
@Serializable
data class ValidationResult(
    /** Normalized email address (if provided) */
    val email: String? = null,

    /** Error message (if validation failed) */
    val error: String? = null,

    /** Response data from server (contains the full API response) */
    val data: ValidationData? = null,

    /** HTTP status code */
    val statusCode: Int? = null
) {
    // -- Convenience Properties --

    /** Whether the validation passed (recommendation is "allow") */
    val isAllowed: Boolean
        get() {
            val validation = data as? ValidationData.Validation ?: return false
            return validation.recommendation == "allow"
        }

    /** The recommendation string from the validation, if available */
    val recommendation: String?
        get() {
            val validation = data as? ValidationData.Validation ?: return null
            return validation.recommendation
        }

    /** The validation signals, if available */
    val signals: List<String>?
        get() {
            val validation = data as? ValidationData.Validation ?: return null
            return validation.signals
        }

    /** A descriptive error message for any failure scenario, null if allowed */
    val errorMessage: String?
        get() {
            if (error != null) return error
            if (data == null) return "Email validation failed"
            return when (data) {
                is ValidationData.Validation -> {
                    if (data.recommendation != "allow") {
                        data.message ?: "Email blocked: ${data.recommendation}"
                    } else {
                        null
                    }
                }
                is ValidationData.Error -> data.response.message
            }
        }
}

// -- ValidationData --

/**
 * Validation data - can be either a successful validation or an error
 */
@Serializable(with = ValidationDataSerializer::class)
sealed class ValidationData {
    @Serializable
    @SerialName("validation")
    data class Validation(
        val checks: Map<String, CheckResult> = emptyMap(),
        val message: String? = null,
        val metadata: ValidationMetadata = ValidationMetadata(),
        val recommendation: String,
        val signals: List<String> = emptyList()
    ) : ValidationData()

    @Serializable
    @SerialName("error")
    data class Error(val response: ErrorResponseData) : ValidationData()
}

/**
 * Custom serializer for ValidationData that determines type based on JSON content
 */
object ValidationDataSerializer : JsonContentPolymorphicSerializer<ValidationData>(ValidationData::class) {
    override fun selectDeserializer(element: JsonElement) = when {
        "error" in element.jsonObject -> ValidationData.Error.serializer()
        else -> ValidationData.Validation.serializer()
    }
}

// -- CheckResult --

/**
 * Individual check result
 */
@Serializable
data class CheckResult(
    val error: String? = null,
    val latency: Int = 0,
    val metadata: Map<String, JsonElement>? = null,
    val pass: Boolean = false
)

// -- DeviceData --

/**
 * Device fingerprint data
 */
@Serializable
data class DeviceData(
    val emailsUsed: Int = 0,
    val firstSeen: Int = 0,
    val isKnownDevice: Boolean = false,
    val isNewEmail: Boolean = false,
    val lastSeen: Int? = null,
    val previousSignups: Int = 0
)

// -- IPData --

/**
 * IP address analysis data
 */
@Serializable
data class IPData(
    val ip: String,
    /** True if VPN, Tor, or datacenter IP detected */
    val isAnonymous: Boolean = false,
    val isFraud: Boolean = false
)

// -- ValidationRequest --

/**
 * Validation request data combining body and header values
 */
@Serializable
data class ValidationRequest(
    val email: String,
    val fingerprintHash: String? = null,
    val ip: String? = null,
    /** Extracted from x-project-id header */
    val projectId: String,
    val sdkVersion: String? = null,
    val userAgent: String? = null,
    val validations: ValidationToggles? = null
)

// -- ValidationResponse --

/**
 * Successful validation response
 */
@Serializable
data class ValidationResponse(
    val checks: Map<String, CheckResult> = emptyMap(),
    val message: String? = null,
    val metadata: ValidationMetadata = ValidationMetadata(),
    val recommendation: String,
    val signals: List<String> = emptyList()
)

// -- ValidationResults --

/**
 * Full validation results including device and IP data
 */
@Serializable
data class ValidationResults(
    val checks: Map<String, CheckResult> = emptyMap(),
    val deviceData: DeviceData? = null,
    val ipData: IPData? = null,
    val signals: List<String> = emptyList()
)

// -- ValidationMetadata --

/**
 * Validation metadata
 */
@Serializable
data class ValidationMetadata(
    val fingerprintHash: String? = null,
    val previousSignups: Int = 0,
    val totalLatency: Int = 0
)

// -- ValidationToggles --

/**
 * Toggle configuration for which validations to run
 */
@Serializable
data class ValidationToggles(
    val alias: ValidationAction? = null,
    val catchall: ValidationAction? = null,
    val device: ValidationAction? = null,
    val disposable: ValidationAction? = null,
    val ip: ValidationAction? = null,
    val mx: ValidationAction? = null,
    val roleEmail: ValidationAction? = null,
    val smtp: ValidationAction? = null,
    val syntax: ValidationAction? = null
)

// -- ErrorResponseData --

/**
 * Error response
 */
@Serializable
data class ErrorResponseData(
    val error: String,
    val message: String
)
