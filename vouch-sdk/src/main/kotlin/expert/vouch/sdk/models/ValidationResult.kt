package expert.vouch.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

/**
 * Email validation response from API
 * Matches the TypeScript SDK ValidationResult interface
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
)

/**
 * Validation data - can be either a successful validation or an error
 */
@Serializable(with = ValidationDataSerializer::class)
sealed class ValidationData {
    @Serializable
    @SerialName("validation")
    data class Validation(val response: ValidationResponseData) : ValidationData()

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

/**
 * Successful validation response
 */
@Serializable
data class ValidationResponseData(
    val checks: ValidationChecks,
    val metadata: ValidationMetadata,
    val recommendation: Recommendation,
    val signals: List<String>
) {
    @Serializable
    enum class Recommendation {
        @SerialName("allow") ALLOW,
        @SerialName("block") BLOCK,
        @SerialName("flag") FLAG
    }
}

/**
 * Validation checks results
 */
@Serializable
data class ValidationChecks(
    val syntax: CheckResult? = null,
    val alias: CheckResult? = null,
    val disposable: CheckResult? = null,
    val ip: CheckResult? = null,
    val mx: CheckResult? = null
)

/**
 * Individual check result
 */
@Serializable
data class CheckResult(
    val latency: Int,
    val pass: Boolean,
    val error: String? = null
)

/**
 * Validation metadata
 */
@Serializable
data class ValidationMetadata(
    val fingerprintId: String? = null,
    val previousSignups: Int,
    val totalLatency: Int
)

/**
 * Error response
 */
@Serializable
data class ErrorResponseData(
    val error: String,
    val message: String
)
