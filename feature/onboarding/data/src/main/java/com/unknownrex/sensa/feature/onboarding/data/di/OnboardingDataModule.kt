package com.unknownrex.sensa.feature.onboarding.data.di

import com.unknownrex.sensa.feature.onboarding.data.PackageManagerPaymentAppDetector
import com.unknownrex.sensa.feature.onboarding.domain.PaymentAppDetector
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val onboardingDataModule = module {
    single<PaymentAppDetector> { PackageManagerPaymentAppDetector(androidContext()) }
}
