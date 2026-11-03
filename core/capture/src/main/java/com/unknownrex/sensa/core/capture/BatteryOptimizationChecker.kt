package com.unknownrex.sensa.core.capture

import android.content.Context
import android.os.PowerManager

/** Whether Android may pause Sensa in the background. Device state only; nothing leaves the phone. */
class BatteryOptimizationChecker(private val context: Context) {

    fun isUnrestricted(): Boolean =
        context.getSystemService(PowerManager::class.java)
            .isIgnoringBatteryOptimizations(context.packageName)

    /** Adapter for the two checks onboarding re-evaluates on resume. */
    class Checks(
        private val notificationAccess: NotificationAccessChecker,
        private val battery: BatteryOptimizationChecker,
    ) : CaptureAccessChecks {
        override fun isNotificationAccessEnabled(): Boolean = notificationAccess.isEnabled()
        override fun isUnrestrictedByBatterySaver(): Boolean = battery.isUnrestricted()
    }
}
