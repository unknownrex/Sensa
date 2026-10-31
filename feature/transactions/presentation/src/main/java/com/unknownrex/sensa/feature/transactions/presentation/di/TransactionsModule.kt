package com.unknownrex.sensa.feature.transactions.presentation.di

import com.unknownrex.sensa.feature.transactions.presentation.TransactionEditorViewModel
import com.unknownrex.sensa.feature.transactions.presentation.TransactionsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val transactionsModule = module {
    viewModelOf(::TransactionsViewModel)
    viewModelOf(::TransactionEditorViewModel)
}
