package com.unknownrex.sensa.feature.onboarding.domain

import com.unknownrex.sensa.core.domain.model.PaymentSource

/** A supported payment app that is installed on this device. */
data class InstalledPaymentApp(
    val packageName: String,
    val source: PaymentSource,
    val label: String,
)

interface PaymentAppDetector {
    fun detectInstalledApps(): List<InstalledPaymentApp>
}
