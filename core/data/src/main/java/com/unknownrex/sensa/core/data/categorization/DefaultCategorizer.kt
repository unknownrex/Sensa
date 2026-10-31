package com.unknownrex.sensa.core.data.categorization

import com.unknownrex.sensa.core.domain.categorization.Categorization
import com.unknownrex.sensa.core.domain.categorization.Categorizer
import com.unknownrex.sensa.core.domain.categorization.DefaultMerchantRules
import com.unknownrex.sensa.core.domain.categorization.SuggestionSource
import com.unknownrex.sensa.core.domain.capture.normalizeMerchant
import com.unknownrex.sensa.core.domain.model.DefaultCategories
import com.unknownrex.sensa.core.domain.model.PaymentSource
import com.unknownrex.sensa.core.domain.repository.MerchantRuleRepository

/**
 * User-learned merchant rules outrank the default keyword map (prd.md §8). Transfers to people
 * are deliberately left uncategorized and sent to review.
 */
class DefaultCategorizer(
    private val merchantRuleRepository: MerchantRuleRepository,
) : Categorizer {

    override suspend fun categorize(merchant: String, source: PaymentSource): Categorization {
        val normalized = normalizeMerchant(merchant)

        merchantRuleRepository.findRule(normalized)?.let { rule ->
            return Categorization(
                categoryId = rule.categoryId,
                confidence = USER_RULE_CONFIDENCE,
                suggestedBy = SuggestionSource.USER_RULE,
            )
        }

        if (looksLikePersonTransfer(normalized)) {
            return Categorization(
                categoryId = DefaultCategories.UNCATEGORIZED_ID,
                confidence = 0.2f,
                suggestedBy = SuggestionSource.FALLBACK,
            )
        }

        val match = DefaultMerchantRules.All.firstOrNull { normalized.contains(it.pattern) }
        return if (match != null) {
            Categorization(
                categoryId = match.categoryId,
                confidence = match.confidence,
                suggestedBy = SuggestionSource.DEFAULT_RULE,
            )
        } else {
            Categorization(
                categoryId = DefaultCategories.UNCATEGORIZED_ID,
                confidence = 0.1f,
                suggestedBy = SuggestionSource.FALLBACK,
            )
        }
    }

    private fun looksLikePersonTransfer(normalized: String): Boolean =
        transferMarkers.any { normalized.contains(it) }

    private companion object {
        const val USER_RULE_CONFIDENCE = 0.95f
        val transferMarkers = listOf(
            "transfer ke",
            "transfer kepada",
            "kirim ke",
            "pindah dana ke",
        )
    }
}
