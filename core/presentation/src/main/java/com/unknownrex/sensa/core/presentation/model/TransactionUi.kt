package com.unknownrex.sensa.core.presentation.model

import com.unknownrex.sensa.core.domain.model.Category
import com.unknownrex.sensa.core.domain.model.Transaction
import com.unknownrex.sensa.core.presentation.dayLabel
import com.unknownrex.sensa.core.presentation.formatRupiah
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

const val UNCATEGORIZED_LABEL = "Uncategorized"
const val UNCATEGORIZED_COLOR = 0xFF9DB5AE

/**
 * Formatted, display-ready transaction. Money is a string so screens never format amounts
 * themselves, and the category colour travels as ARGB so this module stays free of Compose.
 */
data class TransactionUi(
    val id: String,
    val merchant: String,
    val amountLabel: String,
    val amountRupiah: Long,
    val categoryId: String?,
    val categoryLabel: String,
    val categoryColorArgb: Long,
    val iconKey: String,
    val dayLabel: String,
    val timeLabel: String,
    val note: String?,
    val needsReview: Boolean,
)

data class TransactionGroupUi(
    val label: String,
    val totalLabel: String,
    val transactions: List<TransactionUi>,
)

fun Transaction.toTransactionUi(category: Category?): TransactionUi {
    val zone = ZoneId.systemDefault()
    val time = Instant.ofEpochMilli(timestamp).atZone(zone).toLocalTime()
    return TransactionUi(
        id = id,
        merchant = merchant,
        amountLabel = formatRupiah(amountRupiah),
        amountRupiah = amountRupiah,
        categoryId = categoryId,
        categoryLabel = category?.name ?: UNCATEGORIZED_LABEL,
        categoryColorArgb = category?.colorArgb ?: UNCATEGORIZED_COLOR,
        iconKey = category?.iconKey ?: "uncategorized",
        dayLabel = dayLabel(timestamp),
        timeLabel = time.format(timeFormatter),
        note = note,
        needsReview = status == com.unknownrex.sensa.core.domain.model.TransactionStatus.NEEDS_REVIEW,
    )
}

/** Groups by day for sticky section headers, preserving newest-first order. */
fun List<TransactionUi>.groupByDay(): List<TransactionGroupUi> = groupBy { it.dayLabel }
    .map { (label, items) ->
        TransactionGroupUi(
            label = label,
            totalLabel = formatRupiah(items.sumOf { it.amountRupiah }),
            transactions = items,
        )
    }
