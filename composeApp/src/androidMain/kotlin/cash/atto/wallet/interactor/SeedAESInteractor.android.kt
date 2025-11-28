package cash.atto.wallet.interactor

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.BLOCK_MODE_GCM
import android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE
import android.security.keystore.KeyProperties.KEY_ALGORITHM_AES
import android.security.keystore.KeyProperties.PURPOSE_DECRYPT
import android.security.keystore.KeyProperties.PURPOSE_ENCRYPT
import cash.atto.commons.toHex
import cash.atto.commons.utils.SecureRandom
import cash.atto.wallet.datasource.SaltDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.SecretKeyFactory

actual class SeedAESInteractor(
    private val saltDataSource: SaltDataSource
) {
    companion object {
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 100000
        private const val KEY_LENGTH = 256
        private const val PEPPER = "AttoCashWalletSecure2024"
        private const val IV_SIZE = 96
        private const val TAG_SIZE = 128
    }

    actual suspend fun encryptSeed(seed: String, password: String): String = withContext(Dispatchers.Default) {
        try {
            val salt = saltDataSource.get()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            
            // Derive key from password
            val derivedKey = deriveKeyFromPassword(password, salt)
            
            // Initialize cipher for encryption
            cipher.init(Cipher.ENCRYPT_MODE, derivedKey)
            
            // Encrypt the seed
            val encryptedData = cipher.doFinal(seed.toByteArray(Charsets.UTF_8))
            val iv = cipher.iv
            
            // Combine IV + encrypted data and encode to base64
            val combined = iv + encryptedData
            Base64.getEncoder().encodeToString(combined)
        } catch (e: Exception) {
            throw RuntimeException("Failed to encrypt seed", e)
        }
    }

    actual suspend fun decryptSeed(encryptedSeed: String, password: String): String = withContext(Dispatchers.Default) {
        return try {
            val salt = saltDataSource.get()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            
            // Decode from base64
            val combined = Base64.getDecoder().decode(encryptedSeed)
            
            // Extract IV (first 96 bits = 12 bytes)
            val iv = combined.sliceArray(0 until IV_SIZE / 8)
            val encryptedData = combined.sliceArray(IV_SIZE / 8 until combined.size)
            
            // Derive key from password
            val derivedKey = deriveKeyFromPassword(password, salt)
            
            // Initialize cipher for decryption
            cipher.init(Cipher.DECRYPT_MODE, derivedKey, GCMParameterSpec(TAG_SIZE, iv))
            
            // Decrypt the seed
            val decryptedData = cipher.doFinal(encryptedData)
            String(decryptedData, Charsets.UTF_8)
        } catch (e: Exception) {
            ""
        }
    }

    private fun deriveKeyFromPassword(password: String, salt: String): SecretKey {
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val saltBytes = salt.toByteArray()
        val spec = PBEKeySpec(
            (password + PEPPER).toCharArray(),
            saltBytes,
            ITERATIONS,
            KEY_LENGTH
        )
        val derivedKeyBytes = factory.generateSecret(spec).encoded
        
        // Import the derived key as an AES key
        return javax.crypto.spec.SecretKeySpec(derivedKeyBytes, 0, 32, "AES")
    }
}
