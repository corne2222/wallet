package cash.atto.wallet.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CommonCrypto.*
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.NSMutableData
import platform.Darwin.size_t
import platform.posix.memcpy
import kotlin.math.min

actual class PasswordHasher {
    companion object {
        private const val PEPPER = "AttoCashWalletSecure2024"
        private const val ITERATIONS = 100000
        private const val KEY_LENGTH = 32
    }

    actual suspend fun hash(password: String, salt: String): String = withContext(Dispatchers.Default) {
        try {
            val passwordData = (password + PEPPER).encodeToByteArray()
            val saltData = salt.toByteArray()
            
            val hashLength = CC_SHA256_DIGEST_LENGTH
            val hash = ByteArray(hashLength)
            
            CCKeyDerivationPBKDF(
                algorithm = kCCPBKDF2,
                password = passwordData.toCValues(),
                passwordLen = passwordData.size.toULong(),
                salt = saltData.toCValues(),
                saltLen = saltData.size.toULong(),
                prf = kCCPRFHmacSHA256,
                rounds = ITERATIONS.toUInt(),
                derivedKey = hash.toCValues(),
                derivedKeyLen = KEY_LENGTH.toULong()
            )
            
            // Convert to hex string
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            throw RuntimeException("Failed to hash password", e)
        }
    }

    actual suspend fun verify(password: String, salt: String, hash: String): Boolean = withContext(Dispatchers.Default) {
        return try {
            val calculatedHash = hash(password, salt)
            calculatedHash == hash
        } catch (e: Exception) {
            false
        }
    }
}
