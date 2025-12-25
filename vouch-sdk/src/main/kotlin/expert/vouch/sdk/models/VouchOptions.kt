package expert.vouch.sdk.models

/**
 * Vouch SDK configuration options
 */
data class VouchOptions(
    /**
     * API endpoint URL for email validation
     * Default: "https://api.vouch.expert"
     */
    val endpoint: String = "https://api.vouch.expert",

    /**
     * API version number or "latest" to use unversioned endpoint
     * Default: ApiVersion.Latest
     */
    val version: ApiVersion = ApiVersion.Latest
)

/**
 * API version specification
 */
sealed class ApiVersion {
    /** Use unversioned endpoint (e.g., /validate) */
    object Latest : ApiVersion()

    /** Use versioned endpoint (e.g., /v1/validate) */
    data class Version(val number: Int) : ApiVersion()

    internal fun pathComponent(): String = when (this) {
        is Latest -> ""
        is Version -> "/v$number"
    }
}
