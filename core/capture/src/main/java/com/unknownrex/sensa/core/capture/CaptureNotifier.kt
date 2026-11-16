package com.unknownrex.sensa.core.capture

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.text.NumberFormat
import java.util.Locale

/** Posts a local notification when a payment is captured. Silent no-op if notifications are off. */
class CaptureNotifier(private val context: Context) {

    fun notify(outcome: CaptureOutcome) {
        val (transaction, needsReview) = when (outcome) {
            is CaptureOutcome.Saved -> outcome.transaction to false
            is CaptureOutcome.NeedsReview -> outcome.transaction to true
            else -> return
        }
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        ensureChannel()

        val amount = "Rp" + NumberFormat.getIntegerInstance(Locale("id", "ID")).format(transaction.amountRupiah)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            // ponytail: system icon until a monochrome brand asset exists.
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle(if (needsReview) "Needs review: $amount" else "Recorded: $amount")
            .setContentText(transaction.merchant)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(launchIntent())
            .setAutoCancel(true)
            .build()

        // SecurityException if the runtime grant was revoked between the check and the post.
        runCatching { manager.notify(transaction.id.hashCode(), notification) }
    }

    /** Persistent "still capturing" notification that backs [SensaCaptureService]. */
    fun keepAliveNotification(): Notification {
        ensureKeepAliveChannel()
        return NotificationCompat.Builder(context, KEEP_ALIVE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle("Sensa is recording payments")
            .setContentText("Tap to open")
            .setOngoing(true)
            .setSilent(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(launchIntent())
            .build()
    }

    private fun launchIntent() = context.packageManager.getLaunchIntentForPackage(context.packageName)
        ?.let {
            PendingIntent.getActivity(
                context,
                0,
                it,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        }

    private fun ensureChannel() {
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Captured payments", NotificationManager.IMPORTANCE_DEFAULT),
        )
    }

    private fun ensureKeepAliveChannel() {
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(KEEP_ALIVE_CHANNEL_ID, "Background capture", NotificationManager.IMPORTANCE_LOW),
        )
    }

    companion object {
        const val KEEP_ALIVE_ID = 1
        private const val CHANNEL_ID = "capture"
        private const val KEEP_ALIVE_CHANNEL_ID = "capture_keepalive"
    }
}
