package expert.vouch.sdk.signals

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import expert.vouch.sdk.models.StorageSignals
import java.io.File
import java.security.KeyStore
import javax.crypto.KeyGenerator

/**
 * Collects storage capability signals
 */
internal object StorageCollector {
    fun collect(context: Context): StorageSignals {
        val sharedPreferencesAvailable = checkSharedPreferences(context)
        val keyStoreAvailable = checkKeyStore()
        val fileSystemAvailable = checkFileSystem(context)

        return StorageSignals(
            sharedPreferencesAvailable = sharedPreferencesAvailable,
            keyStoreAvailable = keyStoreAvailable,
            fileSystemAvailable = fileSystemAvailable
        )
    }

    private fun checkSharedPreferences(context: Context): Boolean {
        return try {
            val testKey = "vouch_test_key"
            val prefs = context.getSharedPreferences("vouch_test", Context.MODE_PRIVATE)

            // Try to write
            prefs.edit().putBoolean(testKey, true).apply()

            // Try to read
            val result = prefs.getBoolean(testKey, false)

            // Clean up
            prefs.edit().remove(testKey).apply()

            result
        } catch (e: Exception) {
            false
        }
    }

    private fun checkKeyStore(): Boolean {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val keyAlias = "vouch_test_key"

            // Try to generate a test key
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(false)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()

            // Clean up
            keyStore.deleteEntry(keyAlias)

            true
        } catch (e: Exception) {
            false
        }
    }

    private fun checkFileSystem(context: Context): Boolean {
        return try {
            val testFile = File(context.filesDir, "vouch_test.txt")

            // Try to write
            testFile.writeText("test")

            // Try to read
            val content = testFile.readText()

            // Clean up
            testFile.delete()

            content == "test"
        } catch (e: Exception) {
            false
        }
    }
}
