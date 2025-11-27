package cash.atto.wallet.datasource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class SeedDataSourceWindows : SeedDataSourceDesktopImpl {

    private val winCred = WinCred()

    private val _seedChannel = Channel<String?>()
    override val seed = _seedChannel.consumeAsFlow()

    init {
        CoroutineScope(Dispatchers.Default).launch {
            getSeed()
        }
    }

    override suspend fun setSeed(seed: String) {
        writeCredential(APP_NAME, seed)
        removeLegacySeed()
        getSeed()
    }

    override suspend fun clearSeed() {
        try {
            winCred.deleteCredential(APP_NAME)
        } catch (primaryException: Exception) {
            val legacyDeleted = try {
                winCred.deleteCredential(LEGACY_APP_NAME)
                true
            } catch (_: Exception) {
                false
            }

            if (!legacyDeleted) {
                throw primaryException
            }
        }

        removeLegacySeed()
        getSeed()
    }

    private suspend fun getSeed() {
        val currentSeed = readCredential(APP_NAME) ?: readCredential(LEGACY_APP_NAME)?.also {
            migrateSeed(it)
        }

        _seedChannel.send(currentSeed)
    }

    private fun readCredential(target: String): String? = try {
        winCred.getCredential(target).ifEmpty { null }
    } catch (ex: Exception) {
        null
    }

    private fun writeCredential(target: String, value: String) {
        winCred.setCredential(
            target = target,
            userName = USERNAME,
            password = value
        )
    }

    private fun migrateSeed(value: String) {
        try {
            writeCredential(APP_NAME, value)
            removeLegacySeed()
        } catch (_: Exception) {
        }
    }

    private fun removeLegacySeed() {
        try {
            winCred.deleteCredential(LEGACY_APP_NAME)
        } catch (_: Exception) {
        }
    }

    companion object {
        private const val APP_NAME = "Atto Cash Wallet"
        private const val LEGACY_APP_NAME = "Atto Wallet"
        private const val USERNAME = "Main Account"
    }
}
