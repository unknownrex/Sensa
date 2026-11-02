package com.unknownrex.sensa.core.capture

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Reads whether Sensa currently has notification access. This is on-device state only; nothing
 * is sent anywhere.
 */
class NotificationAccessChecker(private val context: Context) {

    /** Permission granted. Does not mean the listener is running; see [connected]. */
    fun isEnabled(): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName)

    /** Whether the system is delivering notifications right now. */
    val connected: StateFlow<Boolean> = SensaNotificationListenerService.connected.asStateFlow()

    /**
     * Asks the system to bind the listener again. Needed after an app update, when Android (and
     * MIUI in particular) keeps the grant but never re-binds the service.
     */
    fun reconnectIfNeeded() {
        if (!isEnabled() || connected.value) return
        // Throws SecurityException if access was revoked between the check and the call.
        runCatching {
            NotificationListenerService.requestRebind(
                ComponentName(context, SensaNotificationListenerService::class.java),
            )
        }
    }

    fun openSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
