package cash.atto.wallet.datasource

interface SaltDataSourceDesktopImpl {
    suspend fun get(): String
}
