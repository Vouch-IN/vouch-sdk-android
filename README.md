# Vouch Android SDK

Kotlin SDK for email validation and device fingerprinting on Android.

## Requirements

- Android 8.0 (API 26)+
- Kotlin 1.9+
- Android Gradle Plugin 8.2+

## Installation

### JitPack

Add JitPack repository to your project's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.Vouch-IN:vouch-sdk-android:vouch-sdk-android-v0.1.12")
}
```

## Usage

### Basic Usage

```kotlin
import expert.vouch.sdk.Vouch
import expert.vouch.sdk.models.VouchOptions

// Initialize the SDK
val vouch = Vouch(
    context = applicationContext,
    projectId = "your-project-id",
    apiKey = "your-api-key"
)

// Validate an email
lifecycleScope.launch {
    val result = vouch.validate("user@example.com")

    if (result.isAllowed) {
        println("Valid: ${result.email}")
    } else {
        println("Error: ${result.errorMessage}")
    }
}
```

### Custom Configuration

```kotlin
val options = VouchOptions(
    endpoint = "https://custom.api.com",
    version = ApiVersion.Version(1)
)

val vouch = Vouch(
    context = applicationContext,
    projectId = "your-project-id",
    apiKey = "your-api-key",
    options = options
)
```

### Direct Fingerprint Access

```kotlin
// Get device fingerprint directly
val fingerprint = vouch.generateFingerprint()

println("Device model: ${fingerprint.hardware.deviceModel}")
println("Screen size: ${fingerprint.hardware.screenWidth} x ${fingerprint.hardware.screenHeight}")
println("OS version: ${fingerprint.system.osVersion}")
```

### Jetpack Compose Integration

```kotlin
@Composable
fun EmailValidationScreen() {
    val vouch = remember {
        Vouch(
            context = LocalContext.current.applicationContext,
            projectId = "your-project-id",
            apiKey = "your-api-key"
        )
    }

    var email by remember { mutableStateOf("") }
    var isValidating by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<ValidationResult?>(null) }

    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                scope.launch {
                    isValidating = true
                    result = vouch.validate(email)
                    isValidating = false
                }
            },
            enabled = !isValidating
        ) {
            Text(if (isValidating) "Validating..." else "Validate")
        }

        result?.let { res ->
            if (res.isAllowed) {
                Text("Valid: ${res.email}", color = Color.Green)
            } else {
                Text(res.errorMessage ?: "Validation failed", color = Color.Red)
            }
        }
    }
}
```

### Traditional View/Activity Integration

```kotlin
class EmailActivity : AppCompatActivity() {
    private val vouch by lazy {
        Vouch(
            context = applicationContext,
            projectId = "your-project-id",
            apiKey = "your-api-key"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        validateButton.setOnClickListener {
            lifecycleScope.launch {
                val result = vouch.validate(emailEditText.text.toString())

                if (result.isAllowed) {
                    resultTextView.text = "Valid email"
                    resultTextView.setTextColor(Color.GREEN)
                } else {
                    resultTextView.text = result.errorMessage ?: "Invalid email"
                    resultTextView.setTextColor(Color.RED)
                }
            }
        }
    }
}
```

## API Reference

### Vouch

Main SDK class for email validation and fingerprinting.

#### Initialization

```kotlin
Vouch(
    context: Context,
    projectId: String,
    apiKey: String,
    options: VouchOptions = VouchOptions()
)
```

#### Methods

- `suspend fun validate(email: String): ValidationResult` - Validate an email address
- `suspend fun generateFingerprint(): Fingerprint` - Get device fingerprint

### VouchOptions

SDK configuration options.

```kotlin
data class VouchOptions(
    val endpoint: String = "https://api.vouch.expert",
    val version: ApiVersion = ApiVersion.Latest
)
```

### ValidationAction

Validation action enum used in toggle configuration.

```kotlin
enum class ValidationAction {
    ALLOW,
    BLOCK,
    FLAG
}
```

### ValidationResult

Email validation response structure with convenience properties.

```kotlin
data class ValidationResult(
    val email: String?,
    val error: String?,
    val data: ValidationData?,
    val statusCode: Int?
) {
    // Convenience properties
    val isAllowed: Boolean
    val recommendation: String?
    val signals: List<String>?
    val errorMessage: String?
}
```

### ValidationData

Validation response data — sealed class with two cases.

```kotlin
sealed class ValidationData {
    data class Validation(
        val checks: Map<String, CheckResult>,
        val message: String?,
        val metadata: ValidationMetadata,
        val recommendation: String,  // "allow", "block", or "flag"
        val signals: List<String>
    ) : ValidationData()

    data class Error(val response: ErrorResponseData) : ValidationData()
}
```

### CheckResult

Individual check result.

```kotlin
data class CheckResult(
    val error: String?,
    val latency: Int,
    val metadata: Map<String, JsonElement>?,
    val pass: Boolean
)
```

### ValidationMetadata

```kotlin
data class ValidationMetadata(
    val fingerprintHash: String?,
    val previousSignups: Int,
    val totalLatency: Int
)
```

### DeviceData

Device fingerprint data returned in validation results.

```kotlin
data class DeviceData(
    val emailsUsed: Int,
    val firstSeen: Int,
    val isKnownDevice: Boolean,
    val isNewEmail: Boolean,
    val lastSeen: Int?,
    val previousSignups: Int
)
```

### IPData

IP address analysis data.

```kotlin
data class IPData(
    val ip: String,
    val isAnonymous: Boolean,  // True if VPN, Tor, or datacenter IP detected
    val isFraud: Boolean
)
```

### ValidationToggles

Toggle configuration for which validations to run.

```kotlin
data class ValidationToggles(
    val alias: ValidationAction?,
    val catchall: ValidationAction?,
    val device: ValidationAction?,
    val disposable: ValidationAction?,
    val ip: ValidationAction?,
    val mx: ValidationAction?,
    val roleEmail: ValidationAction?,
    val smtp: ValidationAction?,
    val syntax: ValidationAction?
)
```

### ErrorResponseData

```kotlin
data class ErrorResponseData(
    val error: String,
    val message: String
)
```

### Signal Types

The SDK collects the following signal categories:

- **HardwareSignals**: Screen dimensions, CPU cores, device memory, device model/manufacturer
- **FontSignals**: List of system fonts, SHA-256 hash
- **SystemSignals**: Android version, SDK version, language, locale, timezone
- **StorageSignals**: SharedPreferences, KeyStore, FileSystem availability

## Privacy

### No Permissions Required

The Vouch Android SDK **does not require any dangerous permissions**. Only `INTERNET` permission is needed for API communication.

**See [PRIVACY.md](./PRIVACY.md) for complete privacy disclosure templates** including:

- Privacy policy language
- Google Play Data Safety form configuration
- GDPR/CCPA compliance notices

## ProGuard/R8

The SDK is ProGuard/R8 compatible. Rules are included in the library automatically.

## Performance

- **Fingerprint Generation**: ~100-500ms (first time)
- **Email Validation**: Local validation is instant; API call depends on network

The SDK starts fingerprint generation immediately when initialized, so the first `validate()` call can reuse the cached fingerprint.

## Error Handling

The SDK never throws from `validate()`. All errors (network failures, invalid format, API errors) are captured in the `ValidationResult`:

```kotlin
val result = vouch.validate(email)

if (result.isAllowed) {
    // Proceed with sign-up
} else {
    // result.errorMessage contains a descriptive error for any failure:
    // - "Invalid email format" (local validation)
    // - "Network error: ..." (connectivity issues)
    // - "Fingerprint generation failed: ..." (device signal error)
    // - "Email blocked: block" (API rejected the email)
    // - API error messages
    showError(result.errorMessage ?: "Validation failed")
}
```

### Accessing Detailed Error Info

For cases where you need more control:

```kotlin
val result = vouch.validate(email)

if (result.data != null) {
    when (val data = result.data) {
        is ValidationData.Validation -> {
            when (data.recommendation) {
                "allow" -> {
                    // Proceed
                }
                "flag" -> {
                    // Show warning but allow
                    showWarning("Please verify your email")
                }
                else -> {
                    // Blocked
                    showError("Email not accepted")
                }
            }
        }
        is ValidationData.Error -> {
            // API returned a structured error
            println("Error code: ${data.response.error}")   // e.g., "invalid_email"
            println("Message: ${data.response.message}")    // e.g., "Email format is invalid"
        }
    }
} else if (result.error != null) {
    // Network or fingerprint error (no API response)
    println("Error: ${result.error}")
    println("Status code: ${result.statusCode ?: 0}")
}
```

## License

See the main repository LICENSE file for details.

## Support

For issues and questions, please visit the main repository's issue tracker.
