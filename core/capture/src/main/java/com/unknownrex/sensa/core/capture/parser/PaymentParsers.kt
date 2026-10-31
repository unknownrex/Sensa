package com.unknownrex.sensa.core.capture.parser

import com.unknownrex.sensa.core.capture.PaymentApps
import com.unknownrex.sensa.core.domain.capture.NotificationParser
import com.unknownrex.sensa.core.domain.capture.NotificationSnapshot
import com.unknownrex.sensa.core.domain.capture.ParsedTransaction
import com.unknownrex.sensa.core.domain.model.PaymentSource

/**
 * Parses GoPay payment notifications.
 *
 * PROVISIONAL: the patterns below cover the known "Pembayaran berhasil / QRIS / transfer"
 * shapes. They must be validated against real redacted samples before release — see
 * `.agents/samples/gopay/`.
 */
class GoPayParser : NotificationParser {

    override val source: PaymentSource = PaymentSource.GOPAY

    override fun canParse(packageName: String): Boolean = packageName == PaymentApps.GOPAY

    override fun parse(notification: NotificationSnapshot): ParsedTransaction? {
        if (!canParse(notification.packageName)) return null
        val text = notification.fullText
        if (!isPayment(text)) return null

        val amount = RupiahMatcher.amountOrNull(text) ?: return null
        val merchant = RupiahMatcher.merchantOrNull(text) ?: return null

        return ParsedTransaction(
            amountRupiah = amount,
            merchant = merchant,
            source = source,
            timestampMillis = notification.postedAtMillis,
            isTransfer = TransferKeywords.any { text.contains(it, ignoreCase = true) },
        )
    }

    private fun isPayment(text: String): Boolean =
        PaymentKeywords.any { text.contains(it, ignoreCase = true) }

    private companion object {
        val PaymentKeywords = listOf(
            "pembayaran berhasil",
            "pembayaran qris",
            "kamu berhasil bayar",
            "berhasil bayar",
            "berhasil transfer",
            "transaksi berhasil",
        )
        val TransferKeywords = listOf("transfer", "kirim dana", "kirim uang")
    }
}

/**
 * Parses BCA mobile transaction notifications.
 *
 * PROVISIONAL: same caveat as [GoPayParser] — needs verification with real samples under
 * `.agents/samples/bca/`.
 */
class BcaMobileParser : NotificationParser {

    override val source: PaymentSource = PaymentSource.BCA_MOBILE

    override fun canParse(packageName: String): Boolean = packageName == PaymentApps.BCA_MOBILE

    override fun parse(notification: NotificationSnapshot): ParsedTransaction? {
        if (!canParse(notification.packageName)) return null
        val text = notification.fullText
        if (!isPayment(text)) return null

        val amount = RupiahMatcher.amountOrNull(text) ?: return null
        val merchant = RupiahMatcher.merchantOrNull(text) ?: return null

        return ParsedTransaction(
            amountRupiah = amount,
            merchant = merchant,
            source = source,
            timestampMillis = notification.postedAtMillis,
            isTransfer = TransferKeywords.any { text.contains(it, ignoreCase = true) },
        )
    }

    private fun isPayment(text: String): Boolean =
        PaymentKeywords.any { text.contains(it, ignoreCase = true) }

    private companion object {
        val PaymentKeywords = listOf(
            "transaksi berhasil",
            "pembayaran berhasil",
            "transfer berhasil",
            "transaksi qris",
            "debit",
        )
        val TransferKeywords = listOf("transfer", "kirim dana", "kirim uang")
    }
}
