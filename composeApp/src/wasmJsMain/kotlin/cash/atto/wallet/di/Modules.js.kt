package cash.atto.wallet.di

import cash.atto.wallet.datasource.AppDatabase
import cash.atto.wallet.datasource.AppDatabaseWeb
import cash.atto.wallet.datasource.PasswordDataSource
import cash.atto.wallet.datasource.SaltDataSource
import cash.atto.wallet.datasource.SeedDataSource
import cash.atto.wallet.interactor.SeedAESInteractor
import cash.atto.wallet.interactor.BiometricUnlockManager
import cash.atto.wallet.interactor.PasswordHasher
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val databaseModule =  module {
    single<AppDatabase> { AppDatabaseWeb() }
}

actual val dataSourceModule = module {
    includes(databaseModule)
    singleOf(::PasswordDataSource)
    singleOf(::SeedAESInteractor)
    singleOf(::SaltDataSource)
    singleOf(::SeedDataSource)
    singleOf(::BiometricUnlockManager)
    singleOf(::PasswordHasher)
}