package cash.atto.wallet.datasource

import cash.atto.wallet.PlatformType
import cash.atto.wallet.getPlatform

actual class SaltDataSource {

    private val dataSourceDesktopImpl = when (getPlatform().type) {
        PlatformType.WINDOWS -> SaltDataSourceWindows()
        PlatformType.LINUX -> SaltDataSourceLinux()
        PlatformType.MACOS -> SaltDataSourceMac()
        else -> throw UnsupportedOperationException("Unsupported platform ${getPlatform()}")
    }

    actual suspend fun get() =
        dataSourceDesktopImpl.get()
}