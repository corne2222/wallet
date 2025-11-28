package cash.atto.wallet.interactor

expect class PasswordHasher {
    suspend fun hash(password: String, salt: String): String
    suspend fun verify(password: String, salt: String, hash: String): Boolean
}
