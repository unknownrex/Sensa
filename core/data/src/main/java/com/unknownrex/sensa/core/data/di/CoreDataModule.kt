package com.unknownrex.sensa.core.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.unknownrex.sensa.core.data.categorization.DefaultCategorizer
import com.unknownrex.sensa.core.data.repository.RoomCategoryRepository
import com.unknownrex.sensa.core.data.repository.RoomMerchantRuleRepository
import com.unknownrex.sensa.core.data.repository.RoomTransactionRepository
import com.unknownrex.sensa.core.data.settings.DataStoreSettingsRepository
import com.unknownrex.sensa.core.data.settings.provideSettingsDataStore
import com.unknownrex.sensa.core.database.SensaDatabase
import com.unknownrex.sensa.core.domain.categorization.Categorizer
import com.unknownrex.sensa.core.domain.repository.CategoryRepository
import com.unknownrex.sensa.core.domain.repository.MerchantRuleRepository
import com.unknownrex.sensa.core.domain.repository.SettingsRepository
import com.unknownrex.sensa.core.domain.repository.TransactionRepository
import com.unknownrex.sensa.core.domain.usecase.ConfirmTransactionCategory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreDataModule = module {
    single { SensaDatabase.build(androidContext()) }
    single { get<SensaDatabase>().transactionDao() }
    single { get<SensaDatabase>().categoryDao() }
    single { get<SensaDatabase>().merchantRuleDao() }

    single<TransactionRepository> { RoomTransactionRepository(get()) }
    single<CategoryRepository> { RoomCategoryRepository(get()) }
    single<MerchantRuleRepository> { RoomMerchantRuleRepository(get()) }
    single<Categorizer> { DefaultCategorizer(get()) }

    single {
        ConfirmTransactionCategory(
            transactionRepository = get(),
            merchantRuleRepository = get(),
            categoryRepository = get(),
        )
    }

    single<DataStore<Preferences>> { provideSettingsDataStore(androidContext()) }
    single<SettingsRepository> { DataStoreSettingsRepository(get()) }
}
