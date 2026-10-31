package com.unknownrex.sensa.core.capture

import com.unknownrex.sensa.core.capture.parser.NotificationParserRegistry
import com.unknownrex.sensa.core.domain.DataError
import com.unknownrex.sensa.core.domain.Result
import com.unknownrex.sensa.core.domain.capture.DedupeInput
import com.unknownrex.sensa.core.domain.capture.NotificationSnapshot
import com.unknownrex.sensa.core.domain.capture.dedupeKey
import com.unknownrex.sensa.core.domain.categorization.AUTO_SAVE_CONFIDENCE_THRESHOLD
import com.unknownrex.sensa.core.domain.categorization.Categorizer
import com.unknownrex.sensa.core.domain.model.DefaultCategories
import com.unknownrex.sensa.core.domain.model.Transaction
import com.unknownrex.sensa.core.domain.model.TransactionStatus
import com.unknownrex.sensa.core.domain.newId
import com.unknownrex.sensa.core.domain.repository.TransactionRepository

sealed interface CaptureOutcome {
    /** Not a payment notification for a supported app. */
    data object Ignored : CaptureOutcome

    /** Same source + amount + merchant inside the dedupe window. */
    data object Duplicate : CaptureOutcome

    data class Saved(val transaction: Transaction) : CaptureOutcome

    data class NeedsReview(val transaction: Transaction) : CaptureOutcome

    data class Failed(val error: DataError.Local) : CaptureOutcome
}

/**
 * Turns a posted notification into a transaction: parse, dedupe, categorize, then save or queue
 * for review. Pure Kotlin apart from the repository, so it is fully unit-testable.
 */
class CaptureProcessor(
    private val parserRegistry: NotificationParserRegistry,
    private val categorizer: Categorizer,
    private val transactionRepository: TransactionRepository,
    private val clock: () -> Long = System::currentTimeMillis,
) {

    suspend fun process(snapshot: NotificationSnapshot): CaptureOutcome {
        val parser = parserRegistry.parserFor(snapshot.packageName) ?: return CaptureOutcome.Ignored
        val parsed = parser.parse(snapshot) ?: return CaptureOutcome.Ignored

        val key = dedupeKey(
            DedupeInput(
                source = parsed.source,
                amountRupiah = parsed.amountRupiah,
                merchant = parsed.merchant,
                timestampMillis = parsed.timestampMillis,
            ),
        )
        if (transactionRepository.findByDedupeKey(key) != null) return CaptureOutcome.Duplicate

        val categorization = categorizer.categorize(parsed.merchant, parsed.source)
        val uncategorized = categorization.categoryId == DefaultCategories.UNCATEGORIZED_ID
        val autoSave = !parsed.isTransfer &&
            !uncategorized &&
            categorization.confidence >= AUTO_SAVE_CONFIDENCE_THRESHOLD

        val now = clock()
        val transaction = Transaction(
            id = newId(),
            amountRupiah = parsed.amountRupiah,
            merchant = parsed.merchant,
            categoryId = if (uncategorized) null else categorization.categoryId,
            source = parsed.source,
            timestamp = parsed.timestampMillis,
            status = if (autoSave) TransactionStatus.AUTO else TransactionStatus.NEEDS_REVIEW,
            confidence = categorization.confidence,
            dedupeKey = key,
            createdAt = now,
        )

        return when (val result = transactionRepository.add(transaction)) {
            is Result.Success -> if (autoSave) {
                CaptureOutcome.Saved(transaction)
            } else {
                CaptureOutcome.NeedsReview(transaction)
            }

            is Result.Error -> CaptureOutcome.Failed(result.error)
        }
    }
}
