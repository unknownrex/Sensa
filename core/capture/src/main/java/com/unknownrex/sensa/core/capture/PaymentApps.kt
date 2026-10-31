package com.unknownrex.sensa.core.capture

import com.unknownrex.sensa.core.domain.model.PaymentSource

/**
 * Package ids of the supported payment apps.
 *
 * These must be verified on a real device with
 * `adb shell pm list packages | findstr gopay` / `findstr bca` before release.
 */
object PaymentApps {
    const val GOPAY = "com.gojek.app"
    const val BCA_MOBILE = "com.bca"

    fun sourceForPackage(packageName: String): PaymentSource? = when (packageName) {
        GOPAY -> PaymentSource.GOPAY
        BCA_MOBILE -> PaymentSource.BCA_MOBILE
        else -> null
    }
}
