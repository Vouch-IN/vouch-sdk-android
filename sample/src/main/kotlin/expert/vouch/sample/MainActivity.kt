package expert.vouch.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import expert.vouch.sdk.Vouch
import expert.vouch.sdk.models.VouchOptions
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Vouch SDK
        val vouch = Vouch(
            context = applicationContext,
            projectId = "your-project-id",
            apiKey = "your-api-key",
            options = VouchOptions()
        )

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EmailValidationScreen(vouch)
                }
            }
        }
    }
}

@Composable
fun EmailValidationScreen(vouch: Vouch) {
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var isValidating by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Vouch SDK Demo",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isValidating
        )

        Button(
            onClick = {
                scope.launch {
                    isValidating = true
                    result = null

                    try {
                        val validationResult = vouch.validate(email.text)

                        if (validationResult.success && validationResult.valid == true) {
                            isSuccess = true
                            result = "✓ Valid: ${validationResult.email}"
                        } else {
                            isSuccess = false
                            result = "✗ Invalid: ${validationResult.error ?: "Unknown error"}"
                        }
                    } catch (e: Exception) {
                        isSuccess = false
                        result = "✗ Error: ${e.message}"
                    } finally {
                        isValidating = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isValidating && email.text.isNotEmpty()
        ) {
            if (isValidating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isValidating) "Validating..." else "Validate Email")
        }

        result?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSuccess) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(16.dp),
                    color = if (isSuccess) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Powered by Vouch SDK v2.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
