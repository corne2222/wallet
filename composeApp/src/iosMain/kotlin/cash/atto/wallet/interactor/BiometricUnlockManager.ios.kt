package cash.atto.wallet.interactor

import cash.atto.wallet.security.BiometricAvailability
import cash.atto.wallet.security.LocalAuthenticationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Security.*

actual class BiometricUnlockManager {
    private val localAuthenticationHelper = LocalAuthenticationHelper()
    private val keychainKey = "atto_wallet_biometric_password"

    actual suspend fun isAvailable(): Boolean = withContext(Dispatchers.Default) {
        val availability = localAuthenticationHelper.isBiometricAvailable()
        return@withContext availability is BiometricAvailability.Available
    }

    actual suspend fun authenticate(
        onSuccess: suspend (password: String) -> Unit,
        onError: suspend (error: String) -> Unit
    ) = withContext(Dispatchers.Default) {
        try {
            val result = localAuthenticationHelper.authenticate(
                reason = "Unlock your Atto Cash Wallet",
                fallbackToPasscode = false
            )

            when (result) {
                is cash.atto.wallet.security.AuthenticationResult.Success -> {
                    val password = retrieveStoredPassword()
                    if (password != null) {
                        onSuccess(password)
                    } else {
                        onError("No stored password found")
                    }
                }
                else -> {
                    onError("Authentication failed")
                }
            }
        } catch (e: Exception) {
            onError(e.message ?: "Authentication error")
        }
    }

    private fun storePassword(password: String) {
        val passwordData = password.encodeToByteArray().toNSData()
        
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to keychainKey,
            kSecValueData to passwordData,
            kSecAttrAccessible to kSecAttrAccessibleWhenUnlockedThisDeviceOnly
        )
        
        SecItemDelete(query.toCFDictionary())
        SecItemAdd(query.toCFDictionary(), null)
    }

    private fun retrieveStoredPassword(): String? {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to keychainKey,
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne
        )
        
        var result: CFTypeRef? = null
        val status = SecItemCopyMatching(query.toCFDictionary(), result.ptr)
        
        return if (status == errSecSuccess) {
            val data = result as? NSData
            data?.let { 
                String(it.toByteArray())
            }
        } else null
    }
}

private fun ByteArray.toNSData(): NSData {
    return NSData.create(bytes = this.toCValues(), length = this.size)
}

private fun NSData.toByteArray(): ByteArray {
    val bytes = ByteArray(this.length.toInt())
    this.getBytes(bytes.toCValues(), length = this.length)
    return bytes
}
