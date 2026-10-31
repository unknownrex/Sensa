package com.unknownrex.sensa

import android.app.Application
import com.unknownrex.sensa.core.capture.di.captureModule
import com.unknownrex.sensa.core.data.di.coreDataModule
import com.unknownrex.sensa.feature.dashboard.presentation.di.dashboardModule
import com.unknownrex.sensa.feature.onboarding.data.di.onboardingDataModule
import com.unknownrex.sensa.feature.onboarding.presentation.di.onboardingPresentationModule
import com.unknownrex.sensa.feature.review.presentation.di.reviewModule
import com.unknownrex.sensa.feature.settings.presentation.di.settingsModule
import com.unknownrex.sensa.feature.transactions.presentation.di.transactionsModule
import com.unknownrex.sensa.ui.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class SensaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@SensaApplication)
            modules(
                coreDataModule,
                captureModule,
                appModule,
                onboardingDataModule,
                onboardingPresentationModule,
                dashboardModule,
                transactionsModule,
                reviewModule,
                settingsModule,
            )
        }
    }
}
