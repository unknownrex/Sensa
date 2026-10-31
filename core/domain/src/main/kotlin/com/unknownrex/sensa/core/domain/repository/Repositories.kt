package com.unknownrex.sensa.core.domain.repository

import com.unknownrex.sensa.core.domain.DataError
import com.unknownrex.sensa.core.domain.EmptyResult
import com.unknownrex.sensa.core.domain.Result
import com.unknownrex.sensa.core.domain.model.Category
import com.unknownrex.sensa.core.domain.model.MerchantRule
import com.unknownrex.sensa.core.domain.model.SensaSettings
import com.unknownrex.sensa.core.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeRecent(limit: Int): Flow<List<Transaction>>

    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<Transaction>>

    fun observeNeedsReview(): Flow<List<Transaction>>

    fun observeNeedsReviewCount(): Flow<Int>

    fun observeById(id: String): Flow<Transaction?>

    suspend fun getById(id: String): Result<Transaction, DataError.Local>

    suspend fun add(transaction: Transaction): EmptyResult<DataError.Local>

    suspend fun update(transaction: Transaction): EmptyResult<DataError.Local>

    suspend fun delete(id: String): EmptyResult<DataError.Local>

    /** Null when the notification is new; the row when it is a repeat within the dedupe window. */
    suspend fun findByDedupeKey(dedupeKey: String): Transaction?
}

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>

    suspend fun getCategory(id: String): Category?

    suspend fun updateCategory(category: Category): EmptyResult<DataError.Local>
}

interface MerchantRuleRepository {
    fun observeRules(): Flow<List<MerchantRule>>

    suspend fun findRule(normalizedMerchant: String): MerchantRule?

    suspend fun upsert(rule: MerchantRule): EmptyResult<DataError.Local>

    suspend fun delete(id: String): EmptyResult<DataError.Local>
}

interface SettingsRepository {
    fun observeSettings(): Flow<SensaSettings>

    suspend fun setOnboardingCompleted(completed: Boolean)

    suspend fun setDynamicColor(enabled: Boolean)

    suspend fun setReduceTransparency(enabled: Boolean)

    suspend fun setReduceMotion(enabled: Boolean)
}
