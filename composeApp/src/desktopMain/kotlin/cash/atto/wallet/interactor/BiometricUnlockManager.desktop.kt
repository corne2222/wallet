package cash.atto.wallet.interactor

actual class BiometricUnlockManager {
    actual suspend fun isAvailable(): Boolean {
        return false
    }

    actual suspend fun authenticate(
        onSuccess: suspend (password: String) -> Unit,
        onError: suspend (error: String) -> Unit
    ) {
        onError("Biometric authentication not supported on desktop")
    }
}
