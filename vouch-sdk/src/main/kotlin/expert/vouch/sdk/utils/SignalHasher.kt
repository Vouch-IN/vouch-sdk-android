package expert.vouch.sdk.utils

import java.security.MessageDigest

/**
 * Extension function to generate SHA-256 hash of a string
 */
internal fun String.sha256(): String {
    val bytes = this.toByteArray()
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(bytes)
    return hash.joinToString("") { "%02x".format(it) }
}

/**
 * Generate SHA-256 hash of byte array
 */
internal fun ByteArray.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(this)
    return hash.joinToString("") { "%02x".format(it) }
}
