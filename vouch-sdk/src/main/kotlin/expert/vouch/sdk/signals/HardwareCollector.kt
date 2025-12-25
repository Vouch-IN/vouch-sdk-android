package expert.vouch.sdk.signals

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import expert.vouch.sdk.models.HardwareSignals

/**
 * Collects hardware and device signals
 */
internal object HardwareCollector {
    fun collect(context: Context): HardwareSignals {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()

        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        // Screen dimensions (physical pixels)
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val screenScale = displayMetrics.density

        // Color depth (Android always uses 32-bit color)
        val colorDepth = 32

        // CPU cores
        val cpuCores = Runtime.getRuntime().availableProcessors()

        // Device memory (in GB)
        val deviceMemory = getDeviceMemory(context)

        // Max touch points (most Android devices support 10 simultaneous touches)
        val maxTouchPoints = 10

        // Platform
        val platform = "Android"

        // Device model and manufacturer
        val deviceModel = Build.MODEL
        val deviceManufacturer = Build.MANUFACTURER

        return HardwareSignals(
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            screenScale = screenScale,
            colorDepth = colorDepth,
            cpuCores = cpuCores,
            deviceMemory = deviceMemory,
            maxTouchPoints = maxTouchPoints,
            platform = platform,
            deviceModel = deviceModel,
            deviceManufacturer = deviceManufacturer
        )
    }

    private fun getDeviceMemory(context: Context): Double? {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            // Convert bytes to GB
            memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
        } catch (e: Exception) {
            null
        }
    }
}
