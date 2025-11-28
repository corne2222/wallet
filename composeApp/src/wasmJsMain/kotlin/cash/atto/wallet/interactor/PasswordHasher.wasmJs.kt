package cash.atto.wallet.interactor

import cash.atto.commons.toUint8Array
import cash.atto.wallet.interactor.utils.getSubtleCryptoInstance
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array

internal fun pbkdf2Algorithm(salt: Uint8Array): JsAny = js(
    """
    ({ 
        "name": "pbkdf2",
        "hash": "sha-256",
        "salt": salt,
        "iterations": 100000
    })
"""
)

internal fun arrayBufferToBase64(buffer: Uint8Array): String = js(
    """
    {
      var binary = '';
      var bytes = new Uint8Array(buffer);
      var len = bytes.byteLength;
      for (var i = 0; i < len; i++) {
        binary += String.fromCharCode(bytes[i]);
      }
      return window.btoa(binary);
    }
"""
)

actual class PasswordHasher {
    companion object {
        private const val PEPPER = "AttoCashWalletSecure2024"
    }

    actual suspend fun hash(password: String, salt: String): String {
        return try {
            val crypto = getSubtleCryptoInstance()
            val saltBytes = salt.encodeToByteArray().toUint8Array()
            
            val key = crypto.importKey(
                format = "raw",
                keyData = (password + PEPPER).encodeToByteArray().toUint8Array(),
                algorithm = "pbkdf2",
                extractable = false,
                keyUsages = mapOf("deriveBits" to true)
            ).await<JsAny>()

            val derivedBits = crypto.deriveBits(
                algorithm = pbkdf2Algorithm(saltBytes),
                baseKey = key,
                256
            ).await<ArrayBuffer>()

            arrayBufferToBase64(Uint8Array(derivedBits))
        } catch (e: Throwable) {
            throw RuntimeException("Failed to hash password", e)
        }
    }

    actual suspend fun verify(password: String, salt: String, hash: String): Boolean {
        return try {
            val calculatedHash = hash(password, salt)
            calculatedHash == hash
        } catch (e: Exception) {
            false
        }
    }
}
