package cash.atto.wallet.interactor

import cash.atto.wallet.datasource.SaltDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CommonCrypto.*
import platform.Darwin.size_t
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import kotlin.math.min

actual class SeedAESInteractor(
    private val saltDataSource: SaltDataSource
) {
    companion object {
        private const val PEPPER = "AttoCashWalletSecure2024"
        private const val ITERATIONS = 100000
        private const val KEY_LENGTH = 32
        private const val IV_LENGTH = 12
        private const val TAG_LENGTH = 16
    }

    actual suspend fun encryptSeed(seed: String, password: String): String = withContext(Dispatchers.Default) {
        try {
            val salt = saltDataSource.get()
            val passwordData = (password + PEPPER).encodeToByteArray()
            val seedData = seed.encodeToByteArray()
            
            // Derive key
            val keyBytes = ByteArray(KEY_LENGTH)
            val derivationResult = CCKeyDerivationPBKDF(
                algorithm = kCCPBKDF2,
                password = passwordData.toCValues(),
                passwordLen = passwordData.size.toULong(),
                salt = salt.encodeToByteArray().toCValues(),
                saltLen = salt.length.toULong(),
                prf = kCCPRFHmacSHA256,
                rounds = ITERATIONS.toUInt(),
                derivedKey = keyBytes.toCValues(),
                derivedKeyLen = KEY_LENGTH.toULong()
            )
            
            if (derivationResult != kCCSuccess) {
                throw RuntimeException("Key derivation failed")
            }
            
            // Generate IV
            val iv = ByteArray(IV_LENGTH)
            if (SecRandomCopyBytes(null, iv.size, iv.toCValues()) != 0) {
                throw RuntimeException("Failed to generate IV")
            }
            
            // Encrypt
            val cipherTextLen = seedData.size + TAG_LENGTH
            val cipherText = ByteArray(cipherTextLen)
            var cipherTextLen_out = size_t(cipherTextLen)
            
            val encryptResult = CCCryptorGCM(
                op = kCCEncrypt,
                alg = kCCAlgorithmAES,
                key = keyBytes.toCValues(),
                keyLength = KEY_LENGTH.toULong(),
                iv = iv.toCValues(),
                ivLen = IV_LENGTH.toULong(),
                aData = null,
                aDataLen = 0u,
                dataIn = seedData.toCValues(),
                dataInLen = seedData.size.toULong(),
                dataOut = cipherText.toCValues(),
                dataOutLen = cipherTextLen_out.ptr,
                tag = null,
                tagLen = 0u
            )
            
            if (encryptResult != kCCSuccess) {
                throw RuntimeException("Encryption failed")
            }
            
            // Combine IV + ciphertext
            val combined = iv + cipherText.sliceArray(0 until cipherTextLen)
            encodeBase64(combined)
        } catch (e: Exception) {
            throw RuntimeException("Failed to encrypt seed", e)
        }
    }

    actual suspend fun decryptSeed(encryptedSeed: String, password: String): String = withContext(Dispatchers.Default) {
        return try {
            val salt = saltDataSource.get()
            val passwordData = (password + PEPPER).encodeToByteArray()
            
            // Decode from base64
            val combined = decodeBase64(encryptedSeed)
            
            // Extract IV and ciphertext
            val iv = combined.sliceArray(0 until IV_LENGTH)
            val cipherText = combined.sliceArray(IV_LENGTH until combined.size)
            
            // Derive key
            val keyBytes = ByteArray(KEY_LENGTH)
            val derivationResult = CCKeyDerivationPBKDF(
                algorithm = kCCPBKDF2,
                password = passwordData.toCValues(),
                passwordLen = passwordData.size.toULong(),
                salt = salt.encodeToByteArray().toCValues(),
                saltLen = salt.length.toULong(),
                prf = kCCPRFHmacSHA256,
                rounds = ITERATIONS.toUInt(),
                derivedKey = keyBytes.toCValues(),
                derivedKeyLen = KEY_LENGTH.toULong()
            )
            
            if (derivationResult != kCCSuccess) {
                throw RuntimeException("Key derivation failed")
            }
            
            // Decrypt
            val plainText = ByteArray(cipherText.size)
            var plainTextLen = size_t(plainText.size)
            
            val decryptResult = CCCryptorGCM(
                op = kCCDecrypt,
                alg = kCCAlgorithmAES,
                key = keyBytes.toCValues(),
                keyLength = KEY_LENGTH.toULong(),
                iv = iv.toCValues(),
                ivLen = IV_LENGTH.toULong(),
                aData = null,
                aDataLen = 0u,
                dataIn = cipherText.toCValues(),
                dataInLen = cipherText.size.toULong(),
                dataOut = plainText.toCValues(),
                dataOutLen = plainTextLen.ptr,
                tag = null,
                tagLen = 0u
            )
            
            if (decryptResult != kCCSuccess) {
                throw RuntimeException("Decryption failed")
            }
            
            plainText.sliceArray(0 until plainTextLen.toLong().toInt()).decodeToString()
        } catch (e: Exception) {
            ""
        }
    }

    private fun encodeBase64(data: ByteArray): String {
        val base64String = NSData.create(bytes = data.toCValues(), length = data.size)
            .base64EncodedStringWithOptions(0u)
        return base64String
    }

    private fun decodeBase64(encoded: String): ByteArray {
        val nsData = NSData(base64EncodedString = encoded, options = 0u)
        val bytes = ByteArray(nsData.length.toInt())
        nsData.getBytes(bytes.toCValues(), length = nsData.length)
        return bytes
    }
}
