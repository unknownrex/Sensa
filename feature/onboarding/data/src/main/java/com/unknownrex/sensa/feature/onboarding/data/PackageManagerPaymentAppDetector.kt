package com.unknownrex.sensa.feature.onboarding.data

import android.content.Context
import android.content.pm.PackageManager
import com.unknownrex.sensa.core.capture.PaymentApps
import com.unknownrex.sensa.core.domain.model.PaymentSource
import com.unknownrex.sensa.feature.onboarding.domain.InstalledPaymentApp
import com.unknownrex.sensa.feature.onboarding.domain.PaymentAppDetector

/**
 * Detects which supported payment apps are installed, so onboarding can tell the user exactly
 * which notifications Sensa will read. No network, no analytics.
 */
class PackageManagerPaymentAppDetector(
    private val context: Context,
) : PaymentAppDetector {

    override fun detectInstalledApps(): List<InstalledPaymentApp> = supported.mapNotNull { (packageName, source) ->
        if (isInstalled(packageName)) {
            InstalledPaymentApp(
                packageName = packageName,
                source = source,
                label = labelFor(source),
            )
        } else {
            null
        }
    }

    private fun isInstalled(packageName: String): Boolean = try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    private fun labelFor(source: PaymentSource): String = when (source) {
        PaymentSource.GOPAY -> "GoPay"
        PaymentSource.BCA_MOBILE -> "BCA mobile"
        else -> source.id
    }

    private companion object {
        val supported = listOf(
            PaymentApps.GOPAY to PaymentSource.GOPAY,
            PaymentApps.BCA_MOBILE to PaymentSource.BCA_MOBILE,
        )
    }
}
