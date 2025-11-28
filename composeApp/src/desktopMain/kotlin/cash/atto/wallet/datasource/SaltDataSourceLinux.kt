package cash.atto.wallet.datasource

import cash.atto.commons.toHex
import cash.atto.commons.utils.SecureRandom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SaltDataSourceLinux : SaltDataSourceDesktopImpl {
    private val saltFile = getSaltFile()

    private fun getSaltFile(): File {
        val configDir = File(System.getProperty("user.home"), ".atto")
        if (!configDir.exists()) {
            configDir.mkdirs()
        }
        return File(configDir, "wallet_salt")
    }

    override suspend fun get(): String = withContext(Dispatchers.IO) {
        if (saltFile.exists()) {
            saltFile.readText().trim()
        } else {
            val newSalt = SecureRandom.randomByteArray(16u).toHex()
            saltFile.parentFile?.mkdirs()
            saltFile.writeText(newSalt)
            newSalt
        }
    }
}
