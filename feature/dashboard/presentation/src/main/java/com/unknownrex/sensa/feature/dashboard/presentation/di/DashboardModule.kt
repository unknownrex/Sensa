package com.unknownrex.sensa.feature.dashboard.presentation.di

import com.unknownrex.sensa.feature.dashboard.presentation.DashboardViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dashboardModule = module {
    viewModelOf(::DashboardViewModel)
}
