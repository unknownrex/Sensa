package com.unknownrex.sensa.core.capture.di

import com.unknownrex.sensa.core.capture.CaptureProcessor
import com.unknownrex.sensa.core.capture.NotificationAccessChecker
import com.unknownrex.sensa.core.capture.parser.NotificationParserRegistry
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val captureModule = module {
    single { NotificationParserRegistry() }
    single { CaptureProcessor(get(), get(), get()) }
    single { NotificationAccessChecker(androidContext()) }
}
