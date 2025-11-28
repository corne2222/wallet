package cash.atto.wallet.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.util.Base64

actual class PasswordHasher {
    companion object {
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 100000
        private const val KEY_LENGTH = 256
        private const val PEPPER = "AttoCashWalletSecure2024"
    }

    actual suspend fun hash(password: String, salt: String): String = withContext(Dispatchers.Default) {
        try {
            val factory = SecretKeyFactory.getInstance(ALGORITHM)
            val saltBytes = salt.toByteArray()
            val spec = PBEKeySpec(
                (password + PEPPER).toCharArray(),
                saltBytes,
                ITERATIONS,
                KEY_LENGTH
            )
            val hash = factory.generateSecret(spec).encoded
            Base64.getEncoder().encodeToString(hash)
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
