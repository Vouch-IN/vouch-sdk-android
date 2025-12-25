package expert.vouch.sdk.signals

import android.graphics.Typeface
import android.os.Build
import androidx.annotation.RequiresApi
import expert.vouch.sdk.models.FontSignals
import expert.vouch.sdk.utils.sha256

/**
 * Collects font availability signals
 */
internal object FontCollector {
    fun collect(): FontSignals {
        val fonts = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ has native API
            getSystemFontsApi29()
        } else {
            // Fallback for older Android versions
            getSystemFontsFallback()
        }

        // Generate SHA-256 hash of the sorted font list
        val sortedFonts = fonts.sorted()
        val fontsString = sortedFonts.joinToString(",")
        val hash = fontsString.sha256()

        return FontSignals(fonts = sortedFonts, hash = hash)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @Suppress("NewApi")
    private fun getSystemFontsApi29(): List<String> {
        return try {
            val fontFamilies = Typeface::class.java
                .getMethod("getSystemFontFamilies")
                .invoke(null) as? Set<*>

            fontFamilies?.mapNotNull { it as? String } ?: emptyList()
        } catch (e: Exception) {
            // Fallback to static list if reflection fails
            getSystemFontsFallback()
        }
    }

    private fun getSystemFontsFallback(): List<String> {
        // Common Android system fonts (static list for API < 29)
        return listOf(
            "sans-serif",
            "sans-serif-condensed",
            "sans-serif-light",
            "sans-serif-thin",
            "sans-serif-medium",
            "sans-serif-black",
            "sans-serif-smallcaps",
            "serif",
            "monospace",
            "cursive",
            "casual"
        )
    }
}
