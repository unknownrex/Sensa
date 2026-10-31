package com.unknownrex.sensa.core.domain.capture

import com.unknownrex.sensa.core.domain.model.PaymentSource
import java.security.MessageDigest

private val whitespace = Regex("\\s+")
private val nonAlphanumeric = Regex("[^a-z0-9 ]")

/**
 * Merchant text is normalized once, at ingestion, and reused by the categorizer, learned rules
 * and dedupe so "Warung Bu Siti", "WARUNG BU SITI" and "warung  bu siti" are the same merchant.
 */
fun normalizeMerchant(raw: String): String = raw
    .lowercase()
    .replace(nonAlphanumeric, " ")
    .replace(whitespace, " ")
    .trim()

/** Notifications for the same payment can repeat; a two minute window collapses them. */
const val DEDUPE_WINDOW_MILLIS = 2 * 60 * 1000L

data class DedupeInput(
    val source: PaymentSource,
    val amountRupiah: Long,
    val merchant: String,
    val timestampMillis: Long,
)

/**
 * Deterministic dedupe key: source + amount + normalized merchant + a 2 minute time bucket.
 * Stored as a unique index so a repeated notification cannot double-count spending.
 */
fun dedupeKey(input: DedupeInput): String {
    val bucket = input.timestampMillis / DEDUPE_WINDOW_MILLIS
    val payload = listOf(
        input.source.id,
        input.amountRupiah.toString(),
        normalizeMerchant(input.merchant),
        bucket.toString(),
    ).joinToString("|")
    return sha256(payload)
}

private fun sha256(value: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
    return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
}
