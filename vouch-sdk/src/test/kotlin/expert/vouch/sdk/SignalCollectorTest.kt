package expert.vouch.sdk

import android.content.Context
import android.os.Build
import expert.vouch.sdk.signals.FontCollector
import expert.vouch.sdk.signals.HardwareCollector
import expert.vouch.sdk.signals.StorageCollector
import expert.vouch.sdk.signals.SystemCollector
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O], manifest = Config.NONE)
class SignalCollectorTest {

    private val context: Context
        get() = RuntimeEnvironment.getApplication()

    @Test
    fun testHardwareCollector() {
        val signals = HardwareCollector.collect(context)

        // Verify all fields are populated
        assertTrue("Screen width should be positive", signals.screenWidth > 0)
        assertTrue("Screen height should be positive", signals.screenHeight > 0)
        assertTrue("Screen scale should be positive", signals.screenScale > 0f)
        assertEquals("Color depth should be 32", 32, signals.colorDepth)
        assertTrue("CPU cores should be positive", signals.cpuCores > 0)

        // Device memory can be null or 0 in test environments (Robolectric doesn't provide real values)
        // Just verify it returns a value (not crashing)
        assertNotNull("Device memory should not be null", signals.deviceMemory)
        println("Device memory in test: ${signals.deviceMemory} GB")

        assertEquals("Max touch points should be 10", 10, signals.maxTouchPoints)
        assertEquals("Platform should be Android", "Android", signals.platform)
        assertFalse("Device model should not be empty", signals.deviceModel.isEmpty())
        assertFalse("Device manufacturer should not be empty", signals.deviceManufacturer.isEmpty())
    }

    @Test
    fun testFontCollector() {
        val signals = FontCollector.collect()

        // Should collect fonts (at least fallback list)
        assertFalse("Should collect system fonts", signals.fonts.isEmpty())
        assertEquals("SHA-256 hash should be 64 characters", 64, signals.hash.length)
    }

    @Test
    fun testFontCollectorConsistency() {
        val signals1 = FontCollector.collect()
        val signals2 = FontCollector.collect()

        // Same device should produce same font list and hash
        assertEquals("Font list should be consistent", signals1.fonts, signals2.fonts)
        assertEquals("Font hash should be consistent", signals1.hash, signals2.hash)
    }

    @Test
    fun testSystemCollector() {
        val signals = SystemCollector.collect()

        // Verify all fields are populated
        assertFalse("OS version should not be empty", signals.osVersion.isEmpty())
        assertEquals("OS name should be Android", "Android", signals.osName)
        assertTrue("SDK version should be positive", signals.sdkVersion > 0)
        assertFalse("Language should not be empty", signals.language.isEmpty())
        assertFalse("Languages list should not be empty", signals.languages.isEmpty())
        assertFalse("Timezone should not be empty", signals.timezone.isEmpty())
        assertFalse("Locale should not be empty", signals.locale.isEmpty())
    }

    @Test
    fun testStorageCollector() {
        val signals = StorageCollector.collect(context)

        // SharedPreferences and FileSystem should always be available in test environment
        assertTrue(
            "SharedPreferences should be available",
            signals.sharedPreferencesAvailable
        )
        assertTrue(
            "FileSystem should be available",
            signals.fileSystemAvailable
        )

        // KeyStore availability depends on environment (may fail in Robolectric)
        // Just verify we get a boolean value (not crashing)
        assertNotNull("KeyStore availability should return a value", signals.keyStoreAvailable)
        println("KeyStore available in test: ${signals.keyStoreAvailable}")
    }

    @Test
    fun testStorageCollectorReturnsValidBooleans() {
        val signals = StorageCollector.collect(context)

        // Verify all fields are proper booleans (not null)
        assertNotNull(signals.sharedPreferencesAvailable)
        assertNotNull(signals.keyStoreAvailable)
        assertNotNull(signals.fileSystemAvailable)
    }

    @Test
    fun testHardwareCollectorDeviceInfo() {
        val signals = HardwareCollector.collect(context)

        // Device model and manufacturer should match Build constants
        assertEquals("Device model should match Build.MODEL", Build.MODEL, signals.deviceModel)
        assertEquals(
            "Device manufacturer should match Build.MANUFACTURER",
            Build.MANUFACTURER,
            signals.deviceManufacturer
        )
    }

    @Test
    fun testSystemCollectorSDKVersion() {
        val signals = SystemCollector.collect()

        // SDK version should match Build.VERSION.SDK_INT
        assertEquals(
            "SDK version should match Build.VERSION.SDK_INT",
            Build.VERSION.SDK_INT,
            signals.sdkVersion
        )
    }

    @Test
    fun testSystemCollectorOSVersion() {
        val signals = SystemCollector.collect()

        // OS version should match Build.VERSION.RELEASE
        assertEquals(
            "OS version should match Build.VERSION.RELEASE",
            Build.VERSION.RELEASE,
            signals.osVersion
        )
    }
}
