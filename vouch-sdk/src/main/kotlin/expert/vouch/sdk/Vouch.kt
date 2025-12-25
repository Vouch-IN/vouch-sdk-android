package expert.vouch.sdk

import android.content.Context
import expert.vouch.sdk.models.ErrorResponseData
import expert.vouch.sdk.models.Fingerprint
import expert.vouch.sdk.models.ValidationData
import expert.vouch.sdk.models.ValidationResult
import expert.vouch.sdk.models.VouchOptions
import expert.vouch.sdk.network.ApiClient
import expert.vouch.sdk.network.VouchEndpoint
import expert.vouch.sdk.signals.FontCollector
import expert.vouch.sdk.signals.HardwareCollector
import expert.vouch.sdk.signals.StorageCollector
import expert.vouch.sdk.signals.SystemCollector
import expert.vouch.sdk.utils.EmailValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async

/**
 * Vouch SDK - Simple API for email validation and fingerprinting
 *
 * Example usage:
 * ```kotlin
 * val vouch = Vouch(
 *     context = applicationContext,
 *     projectId = "project-id-123",
 *     apiKey = "api_key_123",
 *     options = VouchOptions(endpoint = "https://api.example.com")
 * )
 *
 * // Validate email against API with device fingerprint
 * val result = vouch.validate("user@example.com")
 * when (result.data) {
 *     is ValidationData.Validation -> {
 *         val recommendation = result.data.response.recommendation
 *         when (recommendation) {
 *             ValidationResponseData.Recommendation.ALLOW -> println("Email allowed")
 *             ValidationResponseData.Recommendation.BLOCK -> println("Email blocked")
 *             ValidationResponseData.Recommendation.FLAG -> println("Email flagged")
 *         }
 *     }
 *     is ValidationData.Error -> println("Error: ${result.error}")
 *     null -> println("No response data")
 * }
 *
 * // Get fingerprint directly
 * val fingerprint = vouch.generateFingerprint()
 * ```
 */
class Vouch(
    context: Context,
    private val projectId: String,
    private val apiKey: String,
    private val options: VouchOptions = VouchOptions()
) {
    private val appContext: Context = context.applicationContext
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val apiClient: ApiClient
    private val fingerprintDeferred: Deferred<Fingerprint>

    init {
        // Create endpoint configuration
        val endpoint = VouchEndpoint(
            baseURL = options.endpoint,
            version = options.version
        )

        // Create API client
        this.apiClient = ApiClient(
            context = appContext,
            endpoint = endpoint,
            apiKey = apiKey,
            projectId = projectId
        )

        // Start fingerprint generation immediately in background
        this.fingerprintDeferred = scope.async {
            collectFingerprint(appContext)
        }
    }

    /**
     * Validate an email address
     *
     * Performs local format validation first, then sends to API with device fingerprint.
     *
     * @param email Email address to validate
     * @return Validation result from API
     */
    suspend fun validate(email: String): ValidationResult {
        // Local validation first
        val normalizedEmail = EmailValidator.validate(email)
        if (normalizedEmail == null) {
            val errorData = ErrorResponseData(
                error = "invalid_email",
                message = "Invalid email format"
            )
            return ValidationResult(
                email = null,
                error = "Invalid email format",
                data = ValidationData.Error(errorData),
                statusCode = 400
            )
        }

        // Wait for fingerprint generation to complete
        val fingerprint = try {
            fingerprintDeferred.await()
        } catch (e: Exception) {
            val errorData = ErrorResponseData(
                error = "fingerprint_error",
                message = "Fingerprint generation failed: ${e.message}"
            )
            return ValidationResult(
                email = null,
                error = "Fingerprint generation failed: ${e.message}",
                data = ValidationData.Error(errorData),
                statusCode = 0
            )
        }

        // Send to API
        return try {
            apiClient.validate(normalizedEmail, fingerprint)
        } catch (e: Exception) {
            val errorData = ErrorResponseData(
                error = "network_error",
                message = "Network error: ${e.message}"
            )
            ValidationResult(
                email = null,
                error = "Network error: ${e.message}",
                data = ValidationData.Error(errorData),
                statusCode = 0
            )
        }
    }

    /**
     * Generate and return device fingerprint
     *
     * Returns the fingerprint that was generated when the SDK was initialized.
     * If generation is still in progress, this will wait for it to complete.
     *
     * @return Complete device fingerprint
     */
    suspend fun generateFingerprint(): Fingerprint {
        return fingerprintDeferred.await()
    }

    companion object {
        /**
         * Collect device fingerprint signals
         */
        private fun collectFingerprint(context: Context): Fingerprint {
            // Collect all signals
            val hardware = HardwareCollector.collect(context)
            val fonts = FontCollector.collect()
            val system = SystemCollector.collect()
            val storage = StorageCollector.collect(context)

            // Create fingerprint with current timestamp
            val timestamp = System.currentTimeMillis()

            return Fingerprint(
                hardware = hardware,
                fonts = fonts,
                system = system,
                storage = storage,
                timestamp = timestamp,
                version = VouchSDKVersion.VERSION
            )
        }
    }
}
