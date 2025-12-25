package expert.vouch.sdk

import expert.vouch.sdk.utils.sha256
import org.junit.Assert.assertEquals
import org.junit.Test

class SignalHasherTest {
    @Test
    fun testSha256Hash() {
        // Test known SHA-256 hash
        val input = "hello"
        val expected = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"
        val result = input.sha256()

        assertEquals(expected, result)
    }

    @Test
    fun testSha256EmptyString() {
        val input = ""
        val expected = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        val result = input.sha256()

        assertEquals(expected, result)
    }

    @Test
    fun testSha256Consistency() {
        val input = "test string"
        val hash1 = input.sha256()
        val hash2 = input.sha256()

        assertEquals("Same input should produce same hash", hash1, hash2)
    }

    @Test
    fun testSha256Length() {
        val input = "any string"
        val result = input.sha256()

        // SHA-256 always produces 64 character hex string
        assertEquals(64, result.length)
    }
}
