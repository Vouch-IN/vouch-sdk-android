package expert.vouch.sdk.network

import expert.vouch.sdk.models.ApiVersion

/**
 * Vouch API endpoint configuration
 */
internal class VouchEndpoint(
    private val baseURL: String,
    private val version: ApiVersion
) {
    /**
     * Get the full URL for email validation endpoint
     */
    fun validateURL(): String {
        // Ensure baseURL doesn't end with slash for consistent URL construction
        val base = baseURL.trimEnd('/')
        return "$base${version.pathComponent()}/validate"
    }
}
