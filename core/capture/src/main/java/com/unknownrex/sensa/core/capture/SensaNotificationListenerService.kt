package com.unknownrex.sensa.core.capture

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.unknownrex.sensa.core.domain.capture.NotificationSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Captures payment notifications from supported apps. Everything stays on device: the snapshot is
 * parsed, deduped and written straight to Room, with no network permission in the app.
 */
class SensaNotificationListenerService : NotificationListenerService(), KoinComponent {

    private val processor: CaptureProcessor by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (PaymentApps.sourceForPackage(sbn.packageName) == null) return

        val extras = sbn.notification.extras
        val snapshot = NotificationSnapshot(
            packageName = sbn.packageName,
            title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
            text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
            bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
                ?: extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString(),
            postedAtMillis = sbn.postTime.takeIf { it > 0L } ?: System.currentTimeMillis(),
        )

        scope.launch { processor.process(snapshot) }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
