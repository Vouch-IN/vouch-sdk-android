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
    implementation("com.github.Vouch-IN:vouch-sdk-android:v0.1.7")
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

    // Check the validation data
    result.data?.let { data ->
        when (data) {
            is ValidationData.Validation -> {
                println("✅ Email validated: ${result.email}")
                println("Recommendation: ${data.response.recommendation}")
                println("Signals: ${data.response.signals}")
            }
            is ValidationData.Error -> {
                println("❌ Error: ${data.response.error}")
                println("Message: ${data.response.message}")
            }
        }
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
            res.data?.let { data ->
                when (data) {
                    is ValidationData.Validation -> {
                        Column {
                            Text("✓ ${res.email}", color = Color.Green)
                            Text(
                                "Recommendation: ${data.response.recommendation}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    is ValidationData.Error -> {
                        Text("✗ ${data.response.message}", color = Color.Red)
                    }
                }
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

                result.data?.let { data ->
                    when (data) {
                        is ValidationData.Validation -> {
                            val recommendation = data.response.recommendation
                            resultTextView.text = "✓ ${result.email}\nRecommendation: $recommendation"
                            resultTextView.setTextColor(Color.GREEN)
                        }
                        is ValidationData.Error -> {
                            resultTextView.text = "✗ ${data.response.message}"
                            resultTextView.setTextColor(Color.RED)
                        }
                    }
                } ?: run {
                    resultTextView.text = "✗ ${result.error}"
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

The SDK uses Kotlin's `suspend` functions and returns results with three levels of error handling:

### 1. Network/Request Errors (result.error and statusCode)

```kotlin
val result = vouch.validate(email)

if (result.error != null) {
    when (result.statusCode) {
        0 -> println("Network error: ${result.error}")
        else -> println("Request failed: ${result.error}")
    }
}
```

### 2. API Errors (in ValidationData.Error)

```kotlin
result.data?.let { data ->
    when (data) {
        is ValidationData.Error -> {
            // API returned an error (400 status, invalid format, etc.)
            println("Error code: ${data.response.error}")  // e.g., "invalid_email"
            println("Message: ${data.response.message}")  // e.g., "Email format is invalid"
        }
        is ValidationData.Validation -> {
            // Handle successful validation
            println("Recommendation: ${data.response.recommendation}")
        }
    }
}
```

### 3. Handling Recommendations

```kotlin
result.data?.let { data ->
    when (data) {
        is ValidationData.Validation -> {
            when (data.response.recommendation) {
                ValidationResponseData.Recommendation.ALLOW -> {
                    // Email is safe to use
                }
                ValidationResponseData.Recommendation.FLAG -> {
                    // Email flagged for review (e.g., disposable, alias)
                    println("Signals: ${data.response.signals}")
                }
                ValidationResponseData.Recommendation.BLOCK -> {
                    // Email should be blocked
                }
            }
        }
        is ValidationData.Error -> {
            // Handle error
        }
    }
}
```

## License

See the main repository LICENSE file for details.

## Support

For issues and questions, please visit the main repository's issue tracker.
