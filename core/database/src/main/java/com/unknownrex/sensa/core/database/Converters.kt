package com.unknownrex.sensa.core.database

import androidx.room.TypeConverter
import com.unknownrex.sensa.core.domain.model.PaymentSource
import com.unknownrex.sensa.core.domain.model.TransactionStatus

class Converters {
    @TypeConverter
    fun fromPaymentSource(source: PaymentSource): String = source.id

    @TypeConverter
    fun toPaymentSource(id: String): PaymentSource = PaymentSource.fromId(id)

    @TypeConverter
    fun fromTransactionStatus(status: TransactionStatus): String = status.name

    @TypeConverter
    fun toTransactionStatus(name: String): TransactionStatus =
        TransactionStatus.entries.firstOrNull { it.name == name } ?: TransactionStatus.NEEDS_REVIEW
}
