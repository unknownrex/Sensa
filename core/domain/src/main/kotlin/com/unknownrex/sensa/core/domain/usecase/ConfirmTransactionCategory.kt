package com.unknownrex.sensa.core.domain.usecase

import com.unknownrex.sensa.core.domain.DataError
import com.unknownrex.sensa.core.domain.EmptyResult
import com.unknownrex.sensa.core.domain.Result
import com.unknownrex.sensa.core.domain.capture.normalizeMerchant
import com.unknownrex.sensa.core.domain.model.MerchantRule
import com.unknownrex.sensa.core.domain.model.TransactionStatus
import com.unknownrex.sensa.core.domain.newId
import com.unknownrex.sensa.core.domain.repository.CategoryRepository
import com.unknownrex.sensa.core.domain.repository.MerchantRuleRepository
import com.unknownrex.sensa.core.domain.repository.TransactionRepository

/**
 * Confirms a transaction's category. By default it also learns a user merchant rule, so the next
 * payment to the same merchant is categorized automatically (prd.md §8).
 */
class ConfirmTransactionCategory(
    private val transactionRepository: TransactionRepository,
    private val merchantRuleRepository: MerchantRuleRepository,
    private val categoryRepository: CategoryRepository,
    private val clock: () -> Long = System::currentTimeMillis,
) {

    suspend operator fun invoke(
        transactionId: String,
        categoryId: String,
        learnMerchant: Boolean = true,
    ): EmptyResult<DataError.Local> {
        // The category comes from a UI chip row, so a stale or tampered id must not reach Room.
        categoryRepository.getCategory(categoryId)
            ?: return Result.Error(DataError.Local.NOT_FOUND)

        val transaction = when (val result = transactionRepository.getById(transactionId)) {
            is Result.Success -> result.data
            is Result.Error -> return Result.Error(result.error)
        }

        val updated = transaction.copy(
            categoryId = categoryId,
            status = TransactionStatus.CONFIRMED,
            confidence = 1f,
        )
        when (val result = transactionRepository.update(updated)) {
            is Result.Error -> return Result.Error(result.error)
            is Result.Success -> Unit
        }

        if (learnMerchant) {
            val normalized = normalizeMerchant(transaction.merchant)
            if (normalized.isNotBlank()) {
                merchantRuleRepository.upsert(
                    MerchantRule(
                        id = newId(),
                        normalizedMerchant = normalized,
                        categoryId = categoryId,
                        isUserLearned = true,
                        createdAt = clock(),
                    ),
                )
            }
        }

        return Result.Success(Unit)
    }
}
