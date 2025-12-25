package expert.vouch.sdk.signals

import android.os.Build
import expert.vouch.sdk.models.SystemSignals
import java.util.Locale
import java.util.TimeZone

/**
 * Collects system and environment signals (Android equivalent of browser signals)
 */
internal object SystemCollector {
    fun collect(): SystemSignals {
        val locale = Locale.getDefault()
        val timeZone = TimeZone.getDefault()

        // OS information
        val osName = "Android"
        val osVersion = Build.VERSION.RELEASE
        val sdkVersion = Build.VERSION.SDK_INT

        // Language and localization
        val language = locale.language
        val languages = listOf(locale.toLanguageTag())
        val localeIdentifier = locale.toString()

        // Timezone information
        val timezone = timeZone.id
        val timezoneOffset = timeZone.rawOffset / 60000 // Convert milliseconds to minutes

        return SystemSignals(
            osVersion = osVersion,
            osName = osName,
            sdkVersion = sdkVersion,
            language = language,
            languages = languages,
            timezone = timezone,
            timezoneOffset = timezoneOffset,
            locale = localeIdentifier
        )
    }
}
