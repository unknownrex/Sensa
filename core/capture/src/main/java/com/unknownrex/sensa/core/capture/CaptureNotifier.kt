package com.unknownrex.sensa.core.capture

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

    private companion object {
        const val CHANNEL_ID = "capture"
    }
}
