package com.unknownrex.sensa.core.capture

import com.unknownrex.sensa.core.domain.model.PaymentSource

/**
 * Package ids of the supported payment apps, verified on-device via `dumpsys notification`.
 * Keep in sync with `<queries>` in this module's AndroidManifest.xml.
 */
object PaymentApps {
    const val GOPAY = "com.gojek.gopay"
    const val BCA_MOBILE = "com.bca.mybca.omni.android"

    fun sourceForPackage(packageName: String): PaymentSource? = when (packageName) {
        GOPAY -> PaymentSource.GOPAY
        BCA_MOBILE -> PaymentSource.BCA_MOBILE
        else -> null
    }
}
