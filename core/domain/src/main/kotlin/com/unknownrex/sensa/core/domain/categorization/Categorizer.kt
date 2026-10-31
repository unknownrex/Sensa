package com.unknownrex.sensa.core.domain.categorization

import com.unknownrex.sensa.core.domain.model.PaymentSource

/** Where a suggested category came from, so the UI can show a subtle "Suggested" hint. */
enum class SuggestionSource {
    USER_RULE,
    DEFAULT_RULE,
    FALLBACK,
}

data class Categorization(
    val categoryId: String,
    val confidence: Float,
    val suggestedBy: SuggestionSource,
)

/**
 * Confidence at or above this value is auto-saved; below it the transaction goes to review.
 * See prd.md §8 and the open question in §14.
 */
const val AUTO_SAVE_CONFIDENCE_THRESHOLD = 0.8f

interface Categorizer {
    suspend fun categorize(merchant: String, source: PaymentSource): Categorization
}
