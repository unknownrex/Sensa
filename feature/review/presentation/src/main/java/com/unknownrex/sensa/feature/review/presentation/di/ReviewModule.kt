package com.unknownrex.sensa.feature.review.presentation.di

import com.unknownrex.sensa.feature.review.presentation.ReviewViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val reviewModule = module {
    viewModelOf(::ReviewViewModel)
}
