package com.unknownrex.sensa.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.unknownrex.sensa.core.database.entity.CategoryEntity
import com.unknownrex.sensa.core.database.entity.MerchantRuleEntity
import com.unknownrex.sensa.core.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions WHERE timestamp >= :startMillis AND timestamp < :endMillis " +
            "ORDER BY timestamp DESC",
    )
    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE status = :status ORDER BY timestamp DESC")
    fun observeByStatus(status: String): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions WHERE status = :status")
    fun observeCountByStatus(status: String): Flow<Int>

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun observeById(id: String): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE dedupe_key = :dedupeKey LIMIT 1")
    suspend fun findByDedupeKey(dedupeKey: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transaction: TransactionEntity)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sort_order ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: String): CategoryEntity?

    @Upsert
    suspend fun upsert(category: CategoryEntity)
}

@Dao
interface MerchantRuleDao {
    @Query("SELECT * FROM merchant_rules ORDER BY hit_count DESC, created_at DESC")
    fun observeAll(): Flow<List<MerchantRuleEntity>>

    @Query("SELECT * FROM merchant_rules WHERE normalized_merchant = :normalizedMerchant LIMIT 1")
    suspend fun findByMerchant(normalizedMerchant: String): MerchantRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: MerchantRuleEntity)

    @Query("UPDATE merchant_rules SET hit_count = hit_count + 1 WHERE id = :id")
    suspend fun incrementHitCount(id: String)

    @Query("DELETE FROM merchant_rules WHERE id = :id")
    suspend fun deleteById(id: String)
}
