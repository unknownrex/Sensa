package com.unknownrex.sensa.core.domain.capture

import com.unknownrex.sensa.core.domain.model.PaymentSource

/**
 * Platform-neutral view of a posted notification. The listener builds this; parsers never touch
 * Android types, which keeps them unit-testable with plain text.
 */
data class NotificationSnapshot(
    val packageName: String,
    val title: String?,
    val text: String?,
    val bigText: String?,
    val postedAtMillis: Long,
) {
    /** Every non-blank line, de-duplicated, so parsers can match across title/text/bigText. */
    val lines: List<String>
        get() = listOfNotNull(title, text, bigText)
            .flatMap { it.split('\n') }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

    val fullText: String get() = lines.joinToString(separator = " ")
}

data class ParsedTransaction(
    val amountRupiah: Long,
    val merchant: String,
    val source: PaymentSource,
    val timestampMillis: Long,
    /** Person-to-person transfer: always routed to review, never auto-categorized. */
    val isTransfer: Boolean = false,
)

/**
 * One implementation per supported app. Parsers must fail softly: return null for anything that
 * is not a payment, never throw (prd.md §10).
 */
interface NotificationParser {
    val source: PaymentSource

    fun canParse(packageName: String): Boolean

    fun parse(notification: NotificationSnapshot): ParsedTransaction?
}
