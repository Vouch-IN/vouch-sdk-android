package expert.vouch.sdk

import expert.vouch.sdk.models.ApiVersion
import expert.vouch.sdk.models.VouchOptions
import org.junit.Assert.assertEquals
import org.junit.Test

class VouchOptionsTest {
    @Test
    fun testDefaultOptions() {
        val options = VouchOptions()

        assertEquals("https://api.vouch.expert", options.endpoint)
        assertEquals(ApiVersion.Latest, options.version)
    }

    @Test
    fun testCustomOptions() {
        val options = VouchOptions(
            endpoint = "https://custom.api.com",
            version = ApiVersion.Version(1)
        )

        assertEquals("https://custom.api.com", options.endpoint)
        assertEquals(ApiVersion.Version(1), options.version)
    }

    @Test
    fun testApiVersionPathComponent() {
        assertEquals("", ApiVersion.Latest.pathComponent())
        assertEquals("/v1", ApiVersion.Version(1).pathComponent())
        assertEquals("/v2", ApiVersion.Version(2).pathComponent())
    }
}
