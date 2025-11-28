package cash.atto.wallet.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import cash.atto.wallet.datasource.AppDatabase
import cash.atto.wallet.datasource.AppDatabaseIos
import cash.atto.wallet.datasource.PasswordDataSource
import cash.atto.wallet.datasource.SaltDataSource
import cash.atto.wallet.datasource.SeedDataSource
import cash.atto.wallet.interactor.SeedAESInteractor
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSURL
import platform.Foundation.stringByAppendingPathComponent

fun getDatabaseBuilder(): AppDatabase {
    val dbFilePath = NSHomeDirectory().stringByAppendingPathComponent("Documents/atto-wallet.db")
    return Room.databaseBuilder<AppDatabaseIos>(
        name = dbFilePath
    )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

actual val databaseModule = module {
    single<AppDatabase> { getDatabaseBuilder() }
}

actual val dataSourceModule = module {
    includes(databaseModule)
    singleOf(::PasswordDataSource)
    singleOf(::SeedAESInteractor)
    singleOf(::SaltDataSource)
    singleOf(::SeedDataSource)
}