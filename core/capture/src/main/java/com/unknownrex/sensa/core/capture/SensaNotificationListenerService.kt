package com.unknownrex.sensa.core.capture

import android.app.Notification
import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.unknownrex.sensa.core.domain.capture.NotificationSnapshot
import com.unknownrex.sensa.core.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Captures payment notifications from supported apps. Everything stays on device: the snapshot is
 * parsed, deduped and written straight to Room, with no network permission in the app.
 */
class SensaNotificationListenerService : NotificationListenerService(), KoinComponent {

    private val processor: CaptureProcessor by inject()
    private val notifier: CaptureNotifier by inject()
    private val settingsRepository: SettingsRepository by inject()
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

        scope.launch {
            // Log only the outcome type: amounts and merchants must not reach logcat.
            runCatching {
                processor.process(snapshot).also { outcome ->
                    if (settingsRepository.observeSettings().first().notifyOnCapture) notifier.notify(outcome)
                }
            }
                .onSuccess { Log.i(TAG, "${sbn.packageName}: ${it::class.simpleName}") }
                .onFailure { Log.e(TAG, "Capture failed for ${sbn.packageName}", it) }
        }
    }

    override fun onListenerConnected() {
        connected.value = true
        Log.i(TAG, "Listener connected")
    }

    override fun onListenerDisconnected() {
        connected.value = false
        Log.i(TAG, "Listener disconnected, requesting rebind")
        requestRebind(ComponentName(this, SensaNotificationListenerService::class.java))
    }

    internal companion object {
        private const val TAG = "SensaCapture"

        /** True only while the system is actually delivering notifications to this process. */
        val connected = MutableStateFlow(false)
    }

    override fun onDestroy() {
        connected.value = false
        scope.cancel()
        super.onDestroy()
    }
}
