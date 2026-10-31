package com.unknownrex.sensa.core.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.unknownrex.sensa.core.data.mapper.toEntity
import com.unknownrex.sensa.core.data.mapper.toTransaction
import com.unknownrex.sensa.core.database.dao.TransactionDao
import com.unknownrex.sensa.core.domain.DataError
import com.unknownrex.sensa.core.domain.EmptyResult
import com.unknownrex.sensa.core.domain.Result
import com.unknownrex.sensa.core.domain.model.Transaction
import com.unknownrex.sensa.core.domain.model.TransactionStatus
import com.unknownrex.sensa.core.domain.repository.TransactionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTransactionRepository(
    private val transactionDao: TransactionDao,
) : TransactionRepository {

    override fun observeRecent(limit: Int): Flow<List<Transaction>> =
        transactionDao.observeRecent(limit).map { rows -> rows.map { it.toTransaction() } }

    override fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<Transaction>> =
        transactionDao.observeBetween(startMillis, endMillis)
            .map { rows -> rows.map { it.toTransaction() } }

    override fun observeNeedsReview(): Flow<List<Transaction>> =
        transactionDao.observeByStatus(TransactionStatus.NEEDS_REVIEW.name)
            .map { rows -> rows.map { it.toTransaction() } }

    override fun observeNeedsReviewCount(): Flow<Int> =
        transactionDao.observeCountByStatus(TransactionStatus.NEEDS_REVIEW.name)

    override fun observeById(id: String): Flow<Transaction?> =
        transactionDao.observeById(id).map { it?.toTransaction() }

    override suspend fun getById(id: String): Result<Transaction, DataError.Local> = dbCall {
        transactionDao.getById(id)?.toTransaction()
            ?: throw NoSuchElementException("Transaction $id not found")
    }

    override suspend fun add(transaction: Transaction): EmptyResult<DataError.Local> = dbCall {
        transactionDao.insert(transaction.toEntity())
    }

    override suspend fun update(transaction: Transaction): EmptyResult<DataError.Local> = dbCall {
        transactionDao.update(transaction.toEntity())
    }

    override suspend fun delete(id: String): EmptyResult<DataError.Local> = dbCall {
        transactionDao.deleteById(id)
    }

    override suspend fun findByDedupeKey(dedupeKey: String): Transaction? = try {
        transactionDao.findByDedupeKey(dedupeKey)?.toTransaction()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }
}

internal inline fun <T> dbCall(block: () -> T): Result<T, DataError.Local> = try {
    Result.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: SQLiteConstraintException) {
    Result.Error(DataError.Local.DUPLICATE)
} catch (e: NoSuchElementException) {
    Result.Error(DataError.Local.NOT_FOUND)
} catch (e: Exception) {
    Result.Error(DataError.Local.UNKNOWN)
}
