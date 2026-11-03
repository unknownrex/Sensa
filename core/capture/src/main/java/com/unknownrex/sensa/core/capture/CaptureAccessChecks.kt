package com.unknownrex.sensa.core.capture

/** The two ways background capture can be broken by the system. */
interface CaptureAccessChecks {
    fun isNotificationAccessEnabled(): Boolean
    fun isUnrestrictedByBatterySaver(): Boolean
}
