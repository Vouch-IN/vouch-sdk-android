package expert.vouch.sdk.models

import kotlinx.serialization.Serializable

/**
 * Complete device fingerprint data
 */
@Serializable
data class Fingerprint(
    val hardware: HardwareSignals,
    val fonts: FontSignals,
    val system: SystemSignals,
    val storage: StorageSignals,
    val timestamp: Long,
    val version: String
)

/**
 * Hardware and device signals
 */
@Serializable
data class HardwareSignals(
    val screenWidth: Int,
    val screenHeight: Int,
    val screenScale: Float,
    val colorDepth: Int,
    val cpuCores: Int,
    val deviceMemory: Double?,
    val maxTouchPoints: Int,
    val platform: String,
    val deviceModel: String,
    val deviceManufacturer: String
)

/**
 * Font detection signals
 */
@Serializable
data class FontSignals(
    val fonts: List<String>,
    val hash: String
)

/**
 * System and environment signals (Android equivalent of browser signals)
 */
@Serializable
data class SystemSignals(
    val osVersion: String,
    val osName: String,
    val sdkVersion: Int,
    val language: String,
    val languages: List<String>,
    val timezone: String,
    val timezoneOffset: Int,
    val locale: String
)

/**
 * Storage capabilities signals
 */
@Serializable
data class StorageSignals(
    val sharedPreferencesAvailable: Boolean,
    val keyStoreAvailable: Boolean,
    val fileSystemAvailable: Boolean
)
