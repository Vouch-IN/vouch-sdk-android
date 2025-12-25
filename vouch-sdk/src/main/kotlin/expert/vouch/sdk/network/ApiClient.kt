package expert.vouch.sdk.network

import android.content.Context
import expert.vouch.sdk.models.Fingerprint
import expert.vouch.sdk.models.ValidationData
import expert.vouch.sdk.models.ValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * API client for Vouch backend communication
 */
internal class ApiClient(
    private val context: Context,
    private val endpoint: VouchEndpoint,
    private val apiKey: String,
    private val projectId: String
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Validate email with device fingerprint
     */
    suspend fun validate(email: String, fingerprint: Fingerprint): ValidationResult =
        withContext(Dispatchers.IO) {
            try {
                // Create request payload
                val payload = ValidationRequest(
                    email = email,
                    deviceSignals = fingerprint,
                    timestamp = fingerprint.timestamp,
                    sdkVersion = fingerprint.version
                )

                val jsonBody = json.encodeToString(payload)
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = jsonBody.toRequestBody(mediaType)

                // Create HTTP request
                val request = Request.Builder()
                    .url(endpoint.validateURL())
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("X-Project-Id", projectId)
                    .apply {
                        // Add Origin header with app package name in URL format
                        // Required by the API for client key validation
                        val packageName = context.packageName
                        addHeader("Origin", "app://$packageName")
                    }
                    .build()

                // Execute request
                val response = client.newCall(request).execute()
                val statusCode = response.code
                val responseBody = response.body?.string() ?: ""

                // Decode response
                if (responseBody.isEmpty()) {
                    return@withContext ValidationResult(
                        email = null,
                        error = "Empty response from API",
                        data = null,
                        statusCode = statusCode
                    )
                }

                // Try to decode as ValidationData (handles both success and error responses)
                val validationData = try {
                    json.decodeFromString<ValidationData>(responseBody)
                } catch (e: Exception) {
                    return@withContext ValidationResult(
                        email = null,
                        error = "Failed to decode API response: ${e.message}",
                        data = null,
                        statusCode = statusCode
                    )
                }

                // Extract error message if it's an error response
                val errorMessage = when (validationData) {
                    is ValidationData.Error -> validationData.response.message
                    is ValidationData.Validation -> null
                }

                ValidationResult(
                    email = email,
                    error = errorMessage,
                    data = validationData,
                    statusCode = statusCode
                )
            } catch (e: Exception) {
                ValidationResult(
                    email = null,
                    error = "Network error: ${e.message}",
                    data = null,
                    statusCode = 0
                )
            }
        }
}

/**
 * Request payload for email validation
 */
@Serializable
private data class ValidationRequest(
    val email: String,
    val deviceSignals: Fingerprint,
    val timestamp: Long,
    val sdkVersion: String
)
