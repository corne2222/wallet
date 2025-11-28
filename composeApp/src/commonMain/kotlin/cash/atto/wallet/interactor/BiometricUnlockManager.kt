package cash.atto.wallet.interactor

expect class BiometricUnlockManager {
    suspend fun isAvailable(): Boolean
    suspend fun authenticate(onSuccess: suspend (password: String) -> Unit, onError: suspend (error: String) -> Unit)
}
