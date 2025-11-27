package cash.atto.wallet.datasource

class PasswordDataSourceWindows : PasswordDataSourceDesktopImpl {

    private val winCred = WinCred()

    override suspend fun getPassword(seed: String): String? {
        readCredential(seed, APP_NAME)?.let { return it }

        val legacyPassword = readCredential(seed, LEGACY_APP_NAME) ?: return null
        migrateCredential(seed, legacyPassword)
        return legacyPassword
    }

    override suspend fun setPassword(seed: String, password: String) {
        writeCredential(seed, password)
        removeLegacyCredential(seed)
    }

    private fun readCredential(seed: String, appName: String): String? = try {
        winCred
            .getCredential(credentialKey(seed, appName))
            .ifEmpty { null }
    } catch (ex: Exception) {
        null
    }

    private fun writeCredential(seed: String, password: String) {
        val key = credentialKey(seed, APP_NAME)
        winCred.setCredential(
            target = key,
            userName = key,
            password = password
        )
    }

    private fun migrateCredential(seed: String, password: String) {
        try {
            writeCredential(seed, password)
            removeLegacyCredential(seed)
        } catch (_: Exception) {
        }
    }

    private fun removeLegacyCredential(seed: String) {
        try {
            winCred.deleteCredential(credentialKey(seed, LEGACY_APP_NAME))
        } catch (_: Exception) {
        }
    }

    private fun credentialKey(seed: String, appName: String) = "$appName${seed.hashCode()}"

    companion object {
        private const val APP_NAME = "Atto Cash Wallet"
        private const val LEGACY_APP_NAME = "Atto Wallet"
    }
}
