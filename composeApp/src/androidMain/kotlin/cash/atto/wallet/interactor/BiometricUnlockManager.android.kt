package cash.atto.wallet.interactor

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import cash.atto.wallet.util.SecurityUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor
import javax.crypto.Cipher

actual class BiometricUnlockManager(
    private val context: Context
) {
    private val securityUtil = SecurityUtil()
    private val securityKeyAlias = "biometric-password"
    private var biometricPrompt: BiometricPrompt? = null
    private var callbacks: Pair<(suspend (String) -> Unit)?, (suspend (String) -> Unit)?>? = null

    actual suspend fun isAvailable(): Boolean = withContext(Dispatchers.Main) {
        return@withContext try {
            val biometricManager = BiometricManager.from(context)
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            ) == BiometricManager.BIOMETRIC_SUCCESS
        } catch (e: Exception) {
            false
        }
    }

    actual suspend fun authenticate(
        onSuccess: suspend (password: String) -> Unit,
        onError: suspend (error: String) -> Unit
    ) = withContext(Dispatchers.Main) {
        callbacks = Pair(onSuccess, onError)
        
        try {
            val activity = (context as? FragmentActivity)
                ?: throw IllegalStateException("Context must be a FragmentActivity")
            
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ByteArray(12)
            java.security.SecureRandom().nextBytes(iv)
            
            cipher.init(Cipher.ENCRYPT_MODE, securityUtil.getSecretKey(securityKeyAlias) 
                ?: securityUtil.generateSecretKey(securityKeyAlias))
            
            val executor = { command: Runnable -> command.run() } as Executor
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            }
            
            biometricPrompt = BiometricPrompt(activity, executor, callback)
            
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Wallet")
                .setSubtitle("Use biometric to unlock")
                .setNegativeButtonText("Cancel")
                .build()
            
            biometricPrompt?.authenticate(promptInfo)
        } catch (e: Exception) {
            onError(e.message ?: "Biometric authentication failed")
        }
    }
}

private fun SecurityUtil.getSecretKey(keyAlias: String): Any? {
    return try {
        val keyStore = java.security.KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        keyStore.getEntry(keyAlias, null) as? java.security.KeyStore.SecretKeyEntry
    } catch (e: Exception) {
        null
    }
}

private fun SecurityUtil.generateSecretKey(keyAlias: String): Any {
    val keyGenerator = javax.crypto.KeyGenerator.getInstance(
        android.security.keystore.KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
    )
    keyGenerator.init(
        android.security.keystore.KeyGenParameterSpec
            .Builder(keyAlias, android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or 
                android.security.keystore.KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
    )
    return keyGenerator.generateKey()
}
