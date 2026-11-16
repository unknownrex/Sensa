package com.unknownrex.sensa.core.capture

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Keeps the capture process in the foreground so the system (and OEM task killers) are far less
 * likely to stop [SensaNotificationListenerService] when the user clears recent apps.
 *
 * Does no work of its own: the listener owns capture. Started only once notification access is
 * granted, so users who never grant it see no persistent notification.
 */
class SensaCaptureService : Service(), KoinComponent {

    private val notifier: CaptureNotifier by inject()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(CaptureNotifier.KEEP_ALIVE_ID, notifier.keepAliveNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    companion object {
        fun start(context: Context) {
            runCatching {
                context.startForegroundService(Intent(context, SensaCaptureService::class.java))
            }
        }
    }
}
