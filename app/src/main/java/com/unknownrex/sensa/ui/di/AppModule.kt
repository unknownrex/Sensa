package com.unknownrex.sensa.ui.di

import com.unknownrex.sensa.ui.AppViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::AppViewModel)
}
