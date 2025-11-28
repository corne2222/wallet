package cash.atto.wallet.security

import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication
import platform.LocalAuthentication.LAError
import platform.Foundation.NSError

class LocalAuthenticationHelper {
    
    /**
     * Check if biometric authentication is available
     */
    fun isBiometricAvailable(): BiometricAvailability {
        val context = LAContext()
        var error: NSError? = null
        
        val canEvaluate = context.canEvaluatePolicy(
            policy = LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            error = error.ptr
        )
        
        return when {
            canEvaluate -> {
                val biometryType = when (context.biometryType) {
                    LABiometryTypeFaceID -> BiometricType.FACE_ID
                    LABiometryTypeTouchID -> BiometricType.TOUCH_ID
                    LABiometryTypeOpticID -> BiometricType.OPTIC_ID
                    else -> BiometricType.FACE_ID // Default to Face ID for simplicity
                }
                BiometricAvailability.Available(biometryType)
            }
            error != null -> {
                when (error.code) {
                    LAError.biometryNotAvailable.rawValue -> BiometricAvailability.NotAvailable
                    LAError.biometryNotEnrolled.rawValue -> BiometricAvailability.NotEnrolled
                    LAError.passcodeNotSet.rawValue -> BiometricAvailability.PasscodeNotSet
                    else -> BiometricAvailability.NotAvailable
                }
            }
            else -> BiometricAvailability.NotAvailable
        }
    }
    
    /**
     * Authenticate the user using biometrics or device passcode
     */
    suspend fun authenticate(
        reason: String = "Authenticate to access your wallet",
        fallbackToPasscode: Boolean = true
    ): AuthenticationResult {
        val context = LAContext()
        val policy = if (fallbackToPasscode) {
            LAPolicyDeviceOwnerAuthentication
        } else {
            LAPolicyDeviceOwnerAuthenticationWithBiometrics
        }
        
        return try {
            var success = false
            var error: NSError? = null
            
            // Use a semaphore to wait for the async callback
            val semaphore = kotlinx.coroutines.sync.Semaphore(1)
            semaphore.acquire()
            
            context.evaluatePolicy(
                policy = policy,
                localizedReason = reason
            ) { successValue, errorValue ->
                success = successValue
                error = errorValue
                semaphore.release()
            }
            
            semaphore.acquire() // Wait for completion
            
            if (success) {
                AuthenticationResult.Success
            } else {
                when (error?.code) {
                    LAError.userCancel.rawValue -> AuthenticationResult.Cancelled
                    LAError.systemCancel.rawValue -> AuthenticationResult.SystemCancel
                    LAError.passcodeNotSet.rawValue -> AuthenticationResult.PasscodeNotSet
                    LAError.biometryNotAvailable.rawValue -> AuthenticationResult.BiometryNotAvailable
                    LAError.biometryNotEnrolled.rawValue -> AuthenticationResult.BiometryNotEnrolled
                    LAError.biometryLockout.rawValue -> AuthenticationResult.BiometryLockout
                    LAError.userFallback.rawValue -> AuthenticationResult.UserFallback
                    else -> AuthenticationResult.Failed(error?.localizedDescription ?: "Unknown error")
                }
            }
        } catch (e: Exception) {
            AuthenticationResult.Failed(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Get information about the current authentication state
     */
    fun getAuthenticationInfo(): AuthenticationInfo {
        val context = LAContext()
        val biometricAvailability = isBiometricAvailable()
        
        return AuthenticationInfo(
            biometricAvailable = biometricAvailability is BiometricAvailability.Available,
            biometricType = if (biometricAvailability is BiometricAvailability.Available) {
                biometricAvailability.type
            } else null,
            passcodeSet = isPasscodeSet()
        )
    }
    
    private fun isPasscodeSet(): Boolean {
        val context = LAContext()
        var error: NSError? = null
        
        val canEvaluate = context.canEvaluatePolicy(
            policy = LAPolicyDeviceOwnerAuthentication,
            error = error.ptr
        )
        
        return canEvaluate
    }
}

sealed class BiometricAvailability {
    object NotAvailable : BiometricAvailability()
    object NotEnrolled : BiometricAvailability()
    object PasscodeNotSet : BiometricAvailability()
    data class Available(val type: BiometricType) : BiometricAvailability()
}

enum class BiometricType {
    FACE_ID,
    TOUCH_ID,
    OPTIC_ID
}

sealed class AuthenticationResult {
    object Success : AuthenticationResult()
    data class Failed(val message: String) : AuthenticationResult()
    object Cancelled : AuthenticationResult()
    object SystemCancel : AuthenticationResult()
    object PasscodeNotSet : AuthenticationResult()
    object BiometryNotAvailable : AuthenticationResult()
    object BiometryNotEnrolled : AuthenticationResult()
    object BiometryLockout : AuthenticationResult()
    object UserFallback : AuthenticationResult()
}

data class AuthenticationInfo(
    val biometricAvailable: Boolean,
    val biometricType: BiometricType?,
    val passcodeSet: Boolean
)