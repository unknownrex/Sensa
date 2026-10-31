package com.unknownrex.sensa.core.data.categorization

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThan
import assertk.assertions.isTrue
import com.unknownrex.sensa.core.domain.DataError
import com.unknownrex.sensa.core.domain.EmptyResult
import com.unknownrex.sensa.core.domain.Result
import com.unknownrex.sensa.core.domain.capture.normalizeMerchant
import com.unknownrex.sensa.core.domain.categorization.AUTO_SAVE_CONFIDENCE_THRESHOLD
import com.unknownrex.sensa.core.domain.categorization.SuggestionSource
import com.unknownrex.sensa.core.domain.model.DefaultCategories
import com.unknownrex.sensa.core.domain.model.MerchantRule
import com.unknownrex.sensa.core.domain.model.PaymentSource
import com.unknownrex.sensa.core.domain.repository.MerchantRuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DefaultCategorizerTest {

    private val rules = FakeMerchantRuleRepository()
    private val categorizer = DefaultCategorizer(rules)

    @Test
    fun `user learned rule outranks the default keyword map`() = runTest {
        rules.store(
            MerchantRule(
                id = "rule-1",
                normalizedMerchant = "kopi kenangan",
                categoryId = "shopping",
                isUserLearned = true,
                createdAt = 0,
            ),
        )

        val result = categorizer.categorize("KOPI KENANGAN", PaymentSource.GOPAY)

        assertThat(result.categoryId).isEqualTo("shopping")
        assertThat(result.suggestedBy).isEqualTo(SuggestionSource.USER_RULE)
    }

    @Test
    fun `known merchant is matched by keyword`() = runTest {
        val result = categorizer.categorize("Warung Bu Siti", PaymentSource.GOPAY)

        assertThat(result.categoryId).isEqualTo("food")
        assertThat(result.suggestedBy).isEqualTo(SuggestionSource.DEFAULT_RULE)
    }

    @Test
    fun `confident merchant clears the auto-save threshold`() = runTest {
        val result = categorizer.categorize("Indomaret Point", PaymentSource.GOPAY)

        assertThat(result.categoryId).isEqualTo("groceries")
        assertThat(result.confidence >= AUTO_SAVE_CONFIDENCE_THRESHOLD).isTrue()
    }

    @Test
    fun `subscriptions win over generic entertainment`() = runTest {
        val result = categorizer.categorize("Netflix.com", PaymentSource.BCA_MOBILE)

        assertThat(result.categoryId).isEqualTo("subscriptions")
    }

    @Test
    fun `unknown merchant stays uncategorized below the auto-save threshold`() = runTest {
        val result = categorizer.categorize("Toko Bangunan Sejahtera", PaymentSource.GOPAY)

        assertThat(result.categoryId).isEqualTo(DefaultCategories.UNCATEGORIZED_ID)
        assertThat(result.suggestedBy).isEqualTo(SuggestionSource.FALLBACK)
        assertThat(result.confidence).isLessThan(AUTO_SAVE_CONFIDENCE_THRESHOLD)
    }

    @Test
    fun `person transfer text is left uncategorized`() = runTest {
        val result = categorizer.categorize("transfer ke BUDI", PaymentSource.BCA_MOBILE)

        assertThat(result.categoryId).isEqualTo(DefaultCategories.UNCATEGORIZED_ID)
        assertThat(result.suggestedBy).isEqualTo(SuggestionSource.FALLBACK)
    }
}

private class FakeMerchantRuleRepository : MerchantRuleRepository {

    private val rules = mutableListOf<MerchantRule>()

    fun store(rule: MerchantRule) {
        rules += rule
    }

    override fun observeRules(): Flow<List<MerchantRule>> = flowOf(rules)

    override suspend fun findRule(normalizedMerchant: String): MerchantRule? =
        rules.firstOrNull { it.normalizedMerchant == normalizeMerchant(normalizedMerchant) }

    override suspend fun upsert(rule: MerchantRule): EmptyResult<DataError.Local> {
        rules += rule
        return Result.Success(Unit)
    }

    override suspend fun delete(id: String): EmptyResult<DataError.Local> {
        rules.removeAll { it.id == id }
        return Result.Success(Unit)
    }
}
